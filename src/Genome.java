import java.util.*;

/**
 * Created by sawyerprecious on 2019-04-21.
 */
public class Genome {

    private static List<Integer> temp1 = new ArrayList<Integer>();
    private static List<Integer> temp2 = new ArrayList<Integer>();

    private Map<Integer, ConnectionGene> connections;
    private Map<Integer, NodeGene> nodes;

    private final float PERTURB_PROB = 0.9f;

    private Brain brain;


    public Genome() {
        nodes = new HashMap<Integer, NodeGene>();
        connections = new HashMap<Integer, ConnectionGene>();
    }

    public Genome(Genome g) {

        nodes = new HashMap<Integer, NodeGene>();
        connections = new HashMap<Integer, ConnectionGene>();

        for (Integer index : g.getNodes().keySet()) {
            nodes.put(index, new NodeGene(g.getNodes().get(index)));
        }

        for (Integer index : g.getConnections().keySet()) {
            connections.put(index, new ConnectionGene(g.getConnections().get(index)));
        }
    }


    public void setBrain(Brain b) {
        brain = b;
    }

    public Brain getBrain() {
        return brain;
    }

    public Map<Integer, ConnectionGene> getConnections() {
        return connections;
    }

    public Map<Integer, NodeGene> getNodes() {
        return nodes;
    }

    public void addNodeGene(NodeGene gene) {
        nodes.put(gene.getId(), gene);
    }

    public void addConnectionGene(ConnectionGene gene) {
        connections.put(gene.getInnovationNo(), gene);
    }


    public void mutation(Random r) {

        for (ConnectionGene cg : connections.values()) {
            if (r.nextFloat() < PERTURB_PROB) {
                cg.setWeight(cg.getWeight() * (r.nextFloat()*4f-2f));
            } else {
                cg.setWeight(r.nextFloat()*4f-2f);
            }
        }

    }



    public void addConnectionMutation(Random r, InnovationCounter innC, int maxAttempts) {

        int tries = 0;
        boolean success = false;

        while (tries < maxAttempts && !success) {
            tries++;

            Integer[] nodeInnovationNumbers = new Integer[nodes.keySet().size()];
            nodes.keySet().toArray(nodeInnovationNumbers);
            Integer keyNode1 = nodeInnovationNumbers[r.nextInt(nodeInnovationNumbers.length)];
            Integer keyNode2 = nodeInnovationNumbers[r.nextInt(nodeInnovationNumbers.length)];

            NodeGene n1 = nodes.get(keyNode1);
            NodeGene n2 = nodes.get(keyNode2);

            float weight = r.nextFloat()*2f-1f;

            boolean reversed = false;

            if (n1.getType() == NodeGene.GENETYPE.HIDDEN && n2.getType() == NodeGene.GENETYPE.INPUT) {

                reversed = true;

            } else if (n1.getType() == NodeGene.GENETYPE.OUTPUT && n2.getType() == NodeGene.GENETYPE.HIDDEN) {

                reversed = true;

            } else if (n1.getType() == NodeGene.GENETYPE.OUTPUT && n2.getType() == NodeGene.GENETYPE.INPUT) {

                reversed = true;

            }

            boolean connectionImpossible = false;

            if (n1.getType() == NodeGene.GENETYPE.INPUT && n2.getType() == NodeGene.GENETYPE.INPUT) {

                connectionImpossible = true;

            } else if (n1.getType() == NodeGene.GENETYPE.OUTPUT && n2.getType() == NodeGene.GENETYPE.OUTPUT) {

                connectionImpossible = true;

            }

            boolean connectionExists = false;

            for (ConnectionGene con : connections.values()) {

                if (con.getInNode() == n1.getId() && con.getOutNode() == n2.getId()) {

                    connectionExists = true;
                    break;

                } else if (con.getInNode() == n2.getId() && con.getOutNode() == n1.getId()) {

                    connectionExists = true;
                    break;

                }
            }

            if (connectionExists || connectionImpossible) {
                continue;
            }

            ConnectionGene newc = new ConnectionGene(reversed ? n2.getId() : n1.getId(), reversed ? n1.getId() : n2.getId(), weight, true, innC.getInnovation());
            connections.put(newc.getInnovationNo(), newc);
            success = true;
        }
        if (success == false) {
            System.out.println("Tried, but could not add more connections");
        }


    }



    public void addNodeMutation(Random r, InnovationCounter innovationConnection, InnovationCounter innovationNode) {

        ConnectionGene cg = (ConnectionGene) connections.values().toArray()[r.nextInt(connections.size())];

        NodeGene in = nodes.get(cg.getInNode());
        NodeGene out = nodes.get(cg.getOutNode());

        cg.disable();

        NodeGene midNode = new NodeGene(NodeGene.GENETYPE.HIDDEN, innovationNode.getInnovation());

        ConnectionGene intoMid = new ConnectionGene(in.getId(), midNode.getId(), 1f, true, innovationConnection.getInnovation());
        ConnectionGene outOfMid = new ConnectionGene(midNode.getId(), out.getId(), cg.getWeight(), true, innovationConnection.getInnovation());

        nodes.put(midNode.getId(), midNode);
        connections.put(intoMid.getInnovationNo(), intoMid);
        connections.put(outOfMid.getInnovationNo(), outOfMid);

    }


    public static Genome breed(Genome p1, Genome p2, Random r) {

        Genome child = new Genome();

        for (NodeGene p1Node : p1.getNodes().values()) {
            child.addNodeGene(new NodeGene(p1Node));
        }

        for (ConnectionGene p1Conn : p1.getConnections().values()) {
            if (p2.getConnections().containsKey(p1Conn.getInnovationNo())) {

                ConnectionGene childCG = r.nextBoolean() ? new ConnectionGene(p1Conn) : new ConnectionGene(p2.getConnections().get(p1Conn.getInnovationNo()));
                child.addConnectionGene(childCG);

            } else {

                ConnectionGene childCG = new ConnectionGene(p1Conn);
                child.addConnectionGene(childCG);

            }
        }



        return child;

    }




    public static float geneticDistance(Genome genome1, Genome genome2, float c1, float c2, float c3) {

        int excessGenes = countExcessGenes(genome1, genome2);
        int disjointGenes = countDisjointGenes(genome1, genome2);
        float avgWeightDiff = averageWeightDiff(genome1, genome2);

        int n = 1; //max(genome1.nodes.size(), genome2.nodes.size(), 1);    // Can be set to 1 if small

        return ((excessGenes * c1) / n) + ((disjointGenes * c2) / n) + (avgWeightDiff * c3);

    }

    // TODO: make countMatchingGenes, countExcessGenes, countDisjointGenes, and averageWeightDiff 1 method


    public static int countMatchingGenes(Genome genome1, Genome genome2) {

        int matchingGenes = 0;

        List<Integer> nodeKeys1 = sortInAscendingOrder(genome1.getNodes().keySet(), temp1);
        List<Integer> nodeKeys2 = sortInAscendingOrder(genome2.getNodes().keySet(), temp2);

        int highestInnovation1 = nodeKeys1.get(nodeKeys1.size()-1);
        int highestInnovation2 = nodeKeys2.get(nodeKeys2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            NodeGene node1 = genome1.getNodes().get(i);
            NodeGene node2 = genome2.getNodes().get(i);

            if (node1 != null && node2 != null) {

                matchingGenes++;

            }
        }

        List<Integer> conKeys1 = sortInAscendingOrder(genome1.getConnections().keySet(), temp1);
        List<Integer> conKeys2 = sortInAscendingOrder(genome2.getConnections().keySet(), temp2);

        highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        indices = Math.max(highestInnovation1, highestInnovation2);
        for (int i = 0; i <= indices; i++) {

            ConnectionGene connection1 = genome1.getConnections().get(i);
            ConnectionGene connection2 = genome2.getConnections().get(i);

            if (connection1 != null && connection2 != null) {

                matchingGenes++;

            }
        }

        return matchingGenes;
    }



    public static int countDisjointGenes(Genome genome1, Genome genome2) {

        int disjointGenes = 0;

        List<Integer> nodeKeys1 = sortInAscendingOrder(genome1.getNodes().keySet(), temp1);
        List<Integer> nodeKeys2 = sortInAscendingOrder(genome2.getNodes().keySet(), temp2);

        int highestInnovation1 = nodeKeys1.get(nodeKeys1.size()-1);
        int highestInnovation2 = nodeKeys2.get(nodeKeys2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            NodeGene node1 = genome1.getNodes().get(i);
            NodeGene node2 = genome2.getNodes().get(i);

            if (node1 == null && highestInnovation1 > i && node2 != null) {

                disjointGenes++;

            } else if (node2 == null && highestInnovation2 > i && node1 != null) {

                disjointGenes++;

            }
        }

        List<Integer> conKeys1 = sortInAscendingOrder(genome1.getConnections().keySet(), temp1);
        List<Integer> conKeys2 = sortInAscendingOrder(genome2.getConnections().keySet(), temp2);

        highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            ConnectionGene connection1 = genome1.getConnections().get(i);
            ConnectionGene connection2 = genome2.getConnections().get(i);

            if (connection1 == null && highestInnovation1 > i && connection2 != null) {

                disjointGenes++;

            } else if (connection2 == null && highestInnovation2 > i && connection1 != null) {

                disjointGenes++;

            }
        }

        return disjointGenes;
    }



    public static int countExcessGenes(Genome genome1, Genome genome2) {

        int excessGenes = 0;

        List<Integer> nodeKeys1 = sortInAscendingOrder(genome1.getNodes().keySet(), temp1);
        List<Integer> nodeKeys2 = sortInAscendingOrder(genome2.getNodes().keySet(), temp2);

        int highestInnovation1 = nodeKeys1.get(nodeKeys1.size()-1);
        int highestInnovation2 = nodeKeys2.get(nodeKeys2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            NodeGene node1 = genome1.getNodes().get(i);
            NodeGene node2 = genome2.getNodes().get(i);

            if (node1 == null && highestInnovation1 < i && node2 != null) {

                excessGenes++;

            } else if (node2 == null && highestInnovation2 < i && node1 != null) {

                excessGenes++;

            }
        }

        List<Integer> conKeys1 = sortInAscendingOrder(genome1.getConnections().keySet(), temp1);
        List<Integer> conKeys2 = sortInAscendingOrder(genome2.getConnections().keySet(), temp2);

        highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            ConnectionGene connection1 = genome1.getConnections().get(i);
            ConnectionGene connection2 = genome2.getConnections().get(i);

            if (connection1 == null && highestInnovation1 < i && connection2 != null) {

                excessGenes++;

            } else if (connection2 == null && highestInnovation2 < i && connection1 != null) {

                excessGenes++;

            }
        }

        return excessGenes;
    }



    public static float averageWeightDiff(Genome genome1, Genome genome2) {

        int matchingGenes = 0;
        float weightDifference = 0;

        List<Integer> conKeys1 = sortInAscendingOrder(genome1.getConnections().keySet(), temp1);
        List<Integer> conKeys2 = sortInAscendingOrder(genome2.getConnections().keySet(), temp2);

        int highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        int highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {

            ConnectionGene connection1 = genome1.getConnections().get(i);
            ConnectionGene connection2 = genome2.getConnections().get(i);

            if (connection1 != null && connection2 != null) {

                matchingGenes++;
                weightDifference += Math.abs(connection1.getWeight()-connection2.getWeight());

            }
        }

        return weightDifference/matchingGenes;

    }



    private static List<Integer> sortInAscendingOrder(Collection<Integer> c, List<Integer> list) {

        list.clear();
        list.addAll(c);
        java.util.Collections.sort(list);
        return list;

    }


}
