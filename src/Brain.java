import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by sawyerprecious on 2019-04-12.
 */
public class Brain {

    public int moves;
    public int lastMove;
    public int numInRow;

    public float fitness;

    private ArrayList<Boolean> observations;

    private Genome genome;

    private HashMap<Integer, Neuron> neurons;

    private Game game;


    public Brain(Game game, Genome genome) {
        moves = 0;
        lastMove = 0;
        fitness = 0;

        genome.setBrain(this);

        this.game = game;

        this.genome = genome;

        neurons = new HashMap<>();

        setupNeurons();

    }

    public void setupNeurons() {

        for (ConnectionGene cg : genome.getConnections().values()) {
            if (cg.isExpressed()) {

                if (!neurons.containsKey(cg.getInNode())) {

                    neurons.put(cg.getInNode(), new Neuron(new LinkedHashMap<Neuron, Float>(), 0));

                }

                if (neurons.containsKey(cg.getOutNode())) {

                    LinkedHashMap<Neuron, Float> tempIn = neurons.get(cg.getOutNode()).mapInputs;
                    tempIn.put(neurons.get(cg.getInNode()), cg.getWeight());

                    neurons.get(cg.getOutNode()).mapInputs = tempIn;


                    LinkedHashMap<Neuron, Float> tempOut = neurons.get(cg.getInNode()).mapOutputs;
                    tempOut.put(neurons.get(cg.getOutNode()), cg.getWeight());

                    neurons.get(cg.getInNode()).mapOutputs = tempOut;

                } else {

                    LinkedHashMap<Neuron, Float> tempIn = new LinkedHashMap<>();
                    tempIn.put(neurons.get(cg.getInNode()), cg.getWeight());

                    neurons.put(cg.getOutNode(), new Neuron(tempIn));


                    LinkedHashMap<Neuron, Float> tempOut = neurons.get(cg.getInNode()).mapOutputs;
                    tempOut.put(neurons.get(cg.getOutNode()), cg.getWeight());

                    neurons.get(cg.getInNode()).mapOutputs = tempOut;
                }


            }

        }

        updateNeurons();

    }


    public void setupObservations(ArrayList<Boolean> obs) {

        observations = obs;

        for (int i = 0; i < obs.size(); i++) {
            neurons.get(i).val = obs.get(i) ? 1f : 0f;
        }

        updateNeurons();

    }

    private void updateNeurons() {

//        for (int n : neurons.keySet()) {
//            if (genome.getNodes().get(n).getType() != NodeGene.GENETYPE.INPUT) {
//                neurons.get(n).val = 0;
//            }
//        }

        for (ConnectionGene cg : genome.getConnections().values()) {


            if (cg.isExpressed()) {

                Neuron n = neurons.get(cg.getOutNode());

                n.val = n.val + (neurons.get(cg.getInNode()).val * cg.getWeight());

                n.notifyOutputs();

            }

        }

    }


    public void actionPerformed() {

        moves = moves + 1;

        float maxv = neurons.get(6).val;

        int strongestSignal = 0;

        for (int i = 0; i < 4; i++) {
            if (maxv < neurons.get(i + 6).val) {
                strongestSignal = i;
            }
        }

        move(strongestSignal);

    }


    private void move(int action) {

        KeyEvent e;

        switch (action) {
            case 1:
                //left

                if (lastMove == action) {

                    numInRow++;

                } else {

                    numInRow = 0;

                }

                lastMove = action;

                if (numInRow >= 25 || game.loc[0] == 0) {

                    game.dead = true;
                    lastMove = 0;
                    numInRow = 0;

                }


                e = new KeyEvent(game.gamePanel, 1, 0, 1, 37, '¬');
                game.update(e);


                break;

            case 2:
                //right

                if (lastMove == action) {

                    numInRow++;

                } else {

                    numInRow = 0;

                }

                lastMove = action;

                if (numInRow >= 25 || game.loc[0] == game.width - 1) {

                    game.dead = true;
                    lastMove = 0;
                    numInRow = 0;

                }

                e = new KeyEvent(game.gamePanel, 1, 0, 1, 39, '®');
                game.update(e);


                break;


//            case 3:
//               // up
//
//                if (lastMove == action) {
//
//                    numInRow++;
//
//                } else {
//
//                    numInRow = 0;
//
//                }
//
//                lastMove = action;
//
//                if (numInRow >= 25 || game.loc[1] == 0) {
//
//                    game.dead = true;
//                    lastMove = 0;
//                    numInRow = 0;
//
//                }
//
//                e = new KeyEvent(game.gamePanel, 1, 0, 1, 28, 'ü');
//                game.update(e);
//
//
//                break;

            case 3:
                //down

                if (lastMove == action) {

                    numInRow++;

                } else {

                    numInRow = 0;

                }

                lastMove = action;

                if (numInRow >= 25) {

                    game.dead = true;

                }

                e = new KeyEvent(game.gamePanel, 1, 0, 1, 40, '∂');
                game.update(e);

                break;

            default:
                //nothing

                if (lastMove == 0) {

                    numInRow++;

                } else {

                    numInRow = 0;

                }

                lastMove = 0;

                if (numInRow >= 25) {

                    game.dead = true;
                    lastMove = 0;
                    numInRow = 0;

                }

                break;
        }

    }


}
