import java.util.*;

/**
 * Created by sawyerprecious on 2019-04-21.
 */
public abstract class Evaluator {

    private FitnessGenomeComparator fitComp = new FitnessGenomeComparator();

    private InnovationCounter nodeInnovation;
    private InnovationCounter connectionInnovation;

    private Random random = new Random();

    /* Constants for tuning */
    private float C1 = 1.0f;
    private float C2 = 1.0f;
    private float C3 = 0.4f;
    private float DT = 10.0f;
    private float MUTATION_RATE = 0.5f;
    private float ADD_CONNECTION_RATE = 0.1f;
    private float ADD_NODE_RATE = 0.1f;

    private int populationSize;

    private List<Genome> genomes;
    private List<Genome> nextGenGenomes;

    private List<Species> species;

    private Map<Genome, Species> mappedSpecies;
    private Map<Genome, Float> scoreMap;
    private float highestScore;
    private Genome fittestGenome;

    public Evaluator(int populationSize, Genome startingGenome, InnovationCounter nodeInnovation, InnovationCounter connectionInnovation) {

        this.populationSize = populationSize;
        this.nodeInnovation = nodeInnovation;
        this.connectionInnovation = connectionInnovation;
        genomes = new ArrayList<Genome>(populationSize);


        for (int i = 0; i < populationSize; i++) {
            genomes.add(randomizeWeights(startingGenome));
        }

        nextGenGenomes = new ArrayList<Genome>(populationSize);
        mappedSpecies = new HashMap<Genome, Species>();
        scoreMap = new HashMap<Genome, Float>();
        species = new ArrayList<Species>();
    }

    private Genome randomizeWeights(Genome g) {

        Genome newGenome = new Genome();

        for (NodeGene ng : g.getNodes().values()) {
            newGenome.addNodeGene(ng);
        }

        Random r = new Random();
        for (ConnectionGene cg : g.getConnections().values()) {
            ConnectionGene ncg = new ConnectionGene(cg.getInNode(), cg.getOutNode(), r.nextFloat(), cg.isExpressed(), cg.getInnovationNo());
            newGenome.addConnectionGene(ncg);
        }
        return newGenome;
    }

    public List<Genome> getGenomes() {
        return genomes;
    }


    public void evaluate() {

        for (Species s : species) {
            s.reset(random);
        }

        scoreMap.clear();
        mappedSpecies.clear();
        nextGenGenomes.clear();
        highestScore = Float.MIN_VALUE;
        fittestGenome = null;

        for (Genome g : genomes) {

            boolean foundSpecies = false;

            for (Species s : species) {

                if (Genome.geneticDistance(g, s.mascot, C1, C2, C3) < DT) {

                    s.members.add(g);
                    mappedSpecies.put(g, s);
                    foundSpecies = true;
                    break;

                }
            }

            if (!foundSpecies) {

                Species newSpecies = new Species(g);
                species.add(newSpecies);
                mappedSpecies.put(g, newSpecies);

            }
        }

        Iterator<Species> iter = species.iterator();

        while(iter.hasNext()) {
            Species s = iter.next();
            if (s.members.isEmpty()) {
                iter.remove();
            }
        }

        for (Genome g : genomes) {
            Species s = mappedSpecies.get(g);

            float score = evaluateGenome(g);
            float adjustedScore = score / mappedSpecies.get(g).members.size();

            s.addAdjustedFitness(adjustedScore);
            s.fitnessPop.add(new FitnessGenome(g, adjustedScore));
            scoreMap.put(g, adjustedScore);

            if (score > highestScore) {
                highestScore = score;
                fittestGenome = g;
            }
        }


        for (Species s : species) {

            Collections.sort(s.fitnessPop, fitComp);
            Collections.reverse(s.fitnessPop);
            FitnessGenome fittestInSpecies = s.fitnessPop.get(0);
            nextGenGenomes.add(fittestInSpecies.genome);

        }


        while (nextGenGenomes.size() < populationSize) {

            Species s = getRandomSpeciesBiasedAjdustedFitness(random);

            Genome p1 = getRandomGenomeBiasedAdjustedFitness(s, random);
            Genome p2 = getRandomGenomeBiasedAdjustedFitness(s, random);

            Genome child;

            if (scoreMap.get(p1) >= scoreMap.get(p2)) {

                child = Genome.breed(p1, p2, random);

            } else {

                child = Genome.breed(p2, p1, random);

            }

            if (random.nextFloat() < MUTATION_RATE) {

                child.mutation(random);

            }

            if (random.nextFloat() < ADD_CONNECTION_RATE) {

                child.addConnectionMutation(random, connectionInnovation, 10);

            }

            if (random.nextFloat() < ADD_NODE_RATE) {

                child.addNodeMutation(random, connectionInnovation, nodeInnovation);

            }

            nextGenGenomes.add(child);
        }

        genomes = nextGenGenomes;
        nextGenGenomes = new ArrayList<Genome>();
    }


    private Species getRandomSpeciesBiasedAjdustedFitness(Random random) {

        double completeWeight = 0.0;

        for (Species s : species) {

            completeWeight += s.totalAdjustedFitness;

        }

        double r = Math.random() * completeWeight;

        double countWeight = 0.0;

        for (Species s : species) {

            countWeight += s.totalAdjustedFitness;

            if (countWeight >= r) {

                return s;

            }
        }

        throw new RuntimeException("Couldn't find a species... Number is species in total is "+species.size()+", and the total adjusted fitness is "+completeWeight);
    }


    private Genome getRandomGenomeBiasedAdjustedFitness(Species selectFrom, Random random) {

        double completeWeight = 0.0;

        for (FitnessGenome fg : selectFrom.fitnessPop) {

            completeWeight += fg.fitness;

        }

        double r = Math.random() * completeWeight;
        double countWeight = 0.0;

        for (FitnessGenome fg : selectFrom.fitnessPop) {

            countWeight += fg.fitness;

            if (countWeight >= r) {

                return fg.genome;

            }
        }

        throw new RuntimeException("Couldn't find a genome... Number is genomes in selæected species is "+selectFrom.fitnessPop.size()+", and the total adjusted fitness is "+completeWeight);
    }

    public int getSpeciesAmount() {
        return species.size();
    }

    public float getHighestFitness() {
        return highestScore;
    }

    public Genome getFittestGenome() {
        return fittestGenome;
    }

    protected abstract float evaluateGenome(Genome genome);

    public class FitnessGenome {

        float fitness;
        Genome genome;

        public FitnessGenome(Genome genome, float fitness) {

            this.genome = genome;
            this.fitness = fitness;

        }
    }

    public class Species {

        public Genome mascot;
        public List<Genome> members;
        public List<FitnessGenome> fitnessPop;
        public float totalAdjustedFitness = 0f;

        public Species(Genome mascot) {

            this.mascot = mascot;
            this.members = new LinkedList<Genome>();
            this.members.add(mascot);
            this.fitnessPop = new ArrayList<FitnessGenome>();

        }

        public void addAdjustedFitness(float adjustedFitness) {

            this.totalAdjustedFitness += adjustedFitness;

        }


        public void reset(Random r) {

            int newMascotIndex = r.nextInt(members.size());
            this.mascot = members.get(newMascotIndex);
            members.clear();
            fitnessPop.clear();
            totalAdjustedFitness = 0f;

        }
    }

    public class FitnessGenomeComparator implements Comparator<FitnessGenome> {

        @Override
        public int compare(FitnessGenome one, FitnessGenome two) {

            if (one.fitness > two.fitness) {

                return 1;

            } else if (one.fitness < two.fitness) {

                return -1;

            }
            return 0;
        }

    }
}
