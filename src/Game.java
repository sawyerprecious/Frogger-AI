import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.StrictMath.min;

/**
 * Created by sawyerprecious on 2019-04-11.
 */





public class Game implements ActionListener {

    public int numLanes;
    public JPanel gamePanel;

    private JTable board;

    public int width;
    private int diff;
    private int gens;

    public int[] loc;

    public Boolean dead;
    private int framerate;
    private Timer timer;
    private Boolean complete;

    private Brain[] brains;
    private Brain brainInUse;
    private int iterationThisGen;
    private int genNum;

    private int microStep;

    private Evaluator eval;

    public Game(int nl, int diff, int numGens) {
        numLanes = nl;

        width = 25;

        this.diff = diff * 22;

        gens = numGens;

        dead = false;
        complete = false;

        framerate = 25 / diff; // 125 viewable

        board.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                update(e);
            }
        });

        genomeSetup();


    }

    private void createUIComponents() {
        DefaultTableModel model = makeBoardModel();


        board = new JTable(model);

        board.setColumnSelectionAllowed(true);
        board.setRowSelectionAllowed(true);

        board.setRowHeight(10);

        board.getModel().setValueAt("F",0, width / 2);

        board.changeSelection(0, width / 2, true, true);

        loc = new int[2];
        loc[0] = width / 2;
        loc[1] = 0;

        CustomTableCellRenderer renderer = new CustomTableCellRenderer();

        board.setDefaultRenderer(Object.class, renderer);




    }




    private DefaultTableModel makeBoardModel() {
        DefaultTableModel model = new DefaultTableModel();

        for (int i = 0; i < width; i ++) {
            model.addColumn("col");
        }

        // Safe lane to start in
        createLane(0, model);

        // Middle lanes
        for (int i = 1; i < numLanes - 1; i++) {
            createLane(2, model);
        }

        // Safe lane for finish line
        createLane(1, model);

        return model;
    }




    private void createLane(int type, DefaultTableModel model) {
        switch (type) {
            case 0:
                Lane safe = new Lane(width, diff);
                safe.difficulty = 0;
                model.addRow(safe.getLaneContents());

            case 1:
                if (model.getDataVector().size() > 1) {
                    Lane goal = new Lane(width, diff);
                    goal.difficulty = 999;
                    model.addRow(goal.getLaneContents());
                }


                default:
                    Lane lane = new Lane(width, diff);
                    lane.difficulty = diff;
                    model.addRow(lane.getLaneContents());
        }
    }






    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == timer) {
            draw();
        }

    }


    public void update(KeyEvent e) {

        if (!dead && !complete) {

            TableModel model = board.getModel();

            Object lhs;
            if (loc[0] > 0) {
                lhs = model.getValueAt(loc[1], 0);
            } else {
                lhs = model.getValueAt(loc[1], width - 1);
            }

            setModelWithVal(lhs, loc[1], loc[0]);

            //update frog location

            switch (e.getKeyCode()) {
                case 37:
                    //left
                    loc[0] = max(loc[0] - 1, 0);
                    break;

//                case 38:
//                    //up
//                    loc[1] = max(loc[1] - 1, 0);
//                    break;


                case 39:
                    //right
                    loc[0] = min(loc[0] + 1, width - 1);
                    break;


                case 40:
                    //down
                    loc[1] = min(loc[1] + 1, numLanes);
                    break;

                case KeyEvent.VK_K:
                    // My name is Inigo Montoya.  You killed my father.  Prepare to die.
                    dead = true;
                    break;

                default:
                    break;
            }

            if (model.getValueAt(loc[1], loc[0]) == "0" || model.getValueAt(loc[1], loc[0]) == "W") {
                model.setValueAt("D", loc[1], loc[0]);
                dead = true;
            } else {
                if (model.getValueAt(loc[1], loc[0]) == "G") {
                    model.setValueAt("V", loc[1], loc[0]);
                    complete = true;
                } else {
                    model.setValueAt("F", loc[1], loc[0]);
                }
            }




            board.updateUI();
        }
    }



    public void draw() {


        TableModel model = board.getModel();

        // kill if stagnant
        if (brainInUse.moves > 200) {
            dead = true;
        }

        if (!dead && !complete) {


            if (loc[0] == 0) {
                setModelWithVal(model.getValueAt(loc[1], width - 1), loc[1], loc[0]);
            } else {
                setModelWithVal(model.getValueAt(loc[1], 0), loc[1], loc[0]);
            }


            for (int i = 0; i < numLanes; i++) {

                Object lval = model.getValueAt(i, 0);
                Object rval = model.getValueAt(i, width - 1);

                Object prev = null;

                for (int j = 0; j < width; j++) {


                    boolean ltr = i % 2 == 0;

                    if (model.getValueAt(i, j) != "B" && model.getValueAt(i, j) != "G") {

                        if (ltr) {

                            Object curr = model.getValueAt(i, j);


                            if (prev == null) {
                                model.setValueAt(rval, i, j);
                                if ((rval == "0" || rval == "W") && loc[0] == j && loc[1] == i) {
                                    model.setValueAt("D", i, j);
                                    dead = true;
                                }
                            } else {
                                if ((prev == "0" || prev == "W") && loc[0] == j && loc[1] == i) {
                                    model.setValueAt("D", i, j);
                                    dead = true;
                                } else {
                                    model.setValueAt(prev, i, j);
                                }
                            }

                            prev = curr;

                        } else {

                            Object curr = model.getValueAt(i, width - 1 - j);

                            if (prev == null) {
                                model.setValueAt(lval, i, width - 1 - j);
                            } else {


                                if ((lval == "0" || lval == "W") && loc[0] == width - 1 && loc[1] == i) {
                                    model.setValueAt("D", i, width - 1);
                                    dead = true;
                                }


                                if ((prev == "0" || prev == "W") && loc[0] == width - 1 - j && loc[1] == i) {
                                    model.setValueAt("D", i, width - 1 - j);
                                    dead = true;
                                } else {
                                    model.setValueAt(prev, i, width - 1 - j);
                                }
                            }

                            prev = curr;

                        }


                    }
                }
            }

            if (!dead) {
                model.setValueAt("F", loc[1], loc[0]);
            }


            gamePanel.repaint();

            brainInUse.setupObservations(createObservations());

            brainInUse.actionPerformed();

        } else {

            if (microStep < 10) {


                board.setModel(makeBoardModel());

                int objPassed = 0;

                float f = new Float(0);
                for (int y = 0; y < max(0, loc[1] - 1); y++) {
                    for (int x = 0; x < width; x++) {
                        if (model.getValueAt(y, x) == "0" && y < loc[1]) {
                            objPassed++;
                        }
                    }
                }

                f = f + (objPassed * objPassed) + loc[1];

                if (complete) {
                    f = (f * 20);
                }

                if (brainInUse.numInRow != 0) {
                    f = max(f / brainInUse.numInRow, 1);
                }


                f = f / (brainInUse.moves * new Float(0.1));

                if (brainInUse.numInRow >= 25) {

                    f = 0;

                    if (brainInUse.lastMove == 3) {

                        brainInUse.fitness = brainInUse.fitness - 700;
                        brainInUse.lastMove = 0;
                        brainInUse.numInRow = 0;

                    }
                }

                brainInUse.numInRow = 0;


                brainInUse.fitness = brainInUse.fitness + f;

                brainInUse.moves = 0;

                dead = false;
                complete = false;

                loc[0] = width / 2;
                loc[1] = 0;

                board.updateUI();

                microStep++;

            } else {

                microStep = 0;

                board.setModel(makeBoardModel());


                int objPassed = 0;

                float f = new Float(0);
                for (int y = 0; y < max(0, loc[1] - 1); y++) {
                    for (int x = 0; x < width; x++) {
                        if (model.getValueAt(y, x) == "0" && y < loc[1]) {
                            objPassed++;
                        }
                    }
                }

                f = f + (objPassed * objPassed) + loc[1];

                if (complete) {
                    f = (f * 20);
                }

                if (brainInUse.numInRow != 0) {
                    f = max(f / brainInUse.numInRow, 1);
                }

                f = f / (brainInUse.moves * new Float(0.1));

                f = f / 10;

                if (brainInUse.numInRow >= 25) {

                    f = 0;

                    if (brainInUse.lastMove == 3) {

                        brainInUse.fitness = brainInUse.fitness - 700;
                        brainInUse.lastMove = 0;
                        brainInUse.numInRow = 0;

                    }
                }

                brainInUse.numInRow = 0;


                brainInUse.fitness = max(0, brainInUse.fitness + f);
                iterationThisGen++;


                if (brainInUse == brains[brains.length - 1]) {

                    // TODO: handle reproducing

                    eval.evaluate();
                    System.out.print("Generation: "+genNum);
                    System.out.print("\tHighest fitness: "+eval.getHighestFitness());
                    System.out.print("\tAmount of species: "+eval.getSpeciesAmount());
                    System.out.print("\n");

                    if (genNum%10==1) {
                        CreateGenomeImage.createGenomeImage(eval.getFittestGenome(), "/Users/sawyerprecious/Desktop/Projects/Frogger/trunk/output/"+genNum+".png");
                    }

                    if (genNum == gens && gens != 0) {
                        // end game
                        CardLayout c = (CardLayout) gamePanel.getParent().getLayout();
                        c.next(gamePanel.getParent());
                        timer.stop();
                    }

                    initBrains();

                } else {

                    brainInUse = brains[iterationThisGen];

                }

                dead = false;
                complete = false;

                loc[0] = width / 2;
                loc[1] = 0;

                board.updateUI();
            }
        }
    }


    // TODO: set this up to use genomes
    private void initBrains() {
        brains = new Brain[100];
        microStep = 0;
        for (int i = 0; i < 100; i++) {
            Brain b = new Brain(this, eval.getGenomes().get(i));
            b.setupObservations(createObservations());
            brains[i] = b;
        }
        brainInUse = brains[0];

        iterationThisGen = 0;

        genNum++;
    }

//    // TODO: set this up to use genomes (maybe get rid of and do it with initBrains?)
//    private void reproduceBrains() {
//        Brain[] newBrains = new Brain[20];
//
//        microStep = 0;
//
//        float normalize = 0;
//
//        Brain best = null;
//        Brain best2 = null;
//
//        for (Brain b : brains) {
//            normalize = normalize + b.fitness;
//            if (best == null || b.fitness > best.fitness) {
//                best2 = best;
//                best = b;
//            }
//            if (best2 == null || (best2.fitness < b.fitness && best.fitness > b.fitness)) {
//                best2 = b;
//            }
//        }
//
//        Random r = new Random();
//
//        // keep best 2 brains
//
//        newBrains[0] = best;
//        newBrains[1] = best2;
//
//        // asexual
//
//        for (int i = 2; i < (brains.length / 5) * 2; i++) {
//
//            float rand = r.nextFloat() * normalize;
//
//            boolean assigned = false;
//
//            float total = 0;
//
//            if (normalize == 0) {
//
//                Brain rbrain = new Brain(this);
//                rbrain.setupObservations(board.getModel(), loc[1], loc[0]);
//                rbrain.updateInputs();
//
//                newBrains[i] = rbrain;
//
//                assigned = true;
//
//            } else {
//
//                Brain parent = new Brain(this);
//
//                int k = 0;
//
//                for (Brain b : brains) {
//                    total = total + b.fitness;
//                    float chance = total;
//
//
//
//                    if (chance >= rand && !assigned) {
//                        parent = brains[k];
//                        assigned = true;
//                    }
//
//                    k++;
//                }
//
//                Brain nb = new Brain(this);
//                nb.setupObservations(board.getModel(), loc[1], loc[0]);
//                nb.updateInputs();
//                nb.makeTraits(parent);
//
//                newBrains[i] = nb;
//
//            }
//
//        }
//
//        //sexual
//
//        for (int i = (brains.length / 5) * 2; i < (brains.length / 5) * 4; i++) {
//
//            float rand1 = r.nextFloat() * normalize;
//            float rand2 = r.nextFloat() * normalize;
//
//            boolean firstAssigned = false;
//            boolean secondAssigned = false;
//
//            float total = 0;
//
//            if (normalize == 0) {
//
//                newBrains[i] = new Brain(this);
//
//            } else {
//
//                Brain parent1 = new Brain(this);
//                Brain parent2 = new Brain(this);
//
//                int k = 0;
//
//                for (Brain b : brains) {
//                    total = total + b.fitness;
//                    float chance = total;
//
//
//                    if (chance >= rand1 && !firstAssigned) {
//                        parent1 = brains[k];
//                        firstAssigned = true;
//                    }
//
//                    if (chance >= rand2 && !secondAssigned) {
//                        parent2 = brains[k];
//                        secondAssigned = true;
//                    }
//
//                    k++;
//                }
//
//                Brain nb = new Brain(this);
//                nb.setupObservations(board.getModel(), loc[1], loc[0]);
//                nb.updateInputs();
//                nb.makeTraits(parent1, parent2);
//
//                newBrains[i] = nb;
//            }
//        }
//
//        // random for last fifth
//
//        for (int i = (brains.length / 5) * 4; i < brains.length; i++) {
//            Brain nb = new Brain(this);
//            nb.setupObservations(board.getModel(), loc[1], loc[0]);
//            nb.updateInputs();
//
//            newBrains[i] = nb;
//        }
//
//        iterationThisGen = 0;
//
//
//        System.out.println("Average fitness for generation " + genNum + ": " + (normalize / brains.length));
//        System.out.println("Best fitness for generation " + genNum + ": " + best.fitness);
//        System.out.println("New Generation: " + (genNum + 1));
//
//        newBrains[0].fitness = 0;
//        newBrains[1].fitness = 0;
//
//        brains = newBrains;
//
//        brainInUse = brains[0];
//    }






    private ArrayList<Boolean> createObservations() {
        ArrayList<Boolean> toReturn = new ArrayList<>();

        // left
        if (loc[0] - 1 < 0 || loc[0] - 1 >= width) {
            toReturn.add(true);
        } else {
            toReturn.add(board.getModel().getValueAt(loc[1], loc[0] - 1) == "0");
        }

        // right
        if (loc[0] + 1 < 0 || loc[0] + 1 >= width) {
            toReturn.add(true);
        } else {
            toReturn.add(board.getModel().getValueAt(loc[1], loc[0] + 1) == "0");
        }

        // below to left
        if (loc[0] - 1 < 0 || loc[0] - 1 >= width || loc[1] + 1 < 0) {
            toReturn.add(true);
        } else {
            toReturn.add(board.getModel().getValueAt(loc[1] + 1, loc[0] - 1) == "0");
        }

        // directly below
        if (loc[1] + 1 < 0) {
            toReturn.add(true);
        } else {
            toReturn.add(board.getModel().getValueAt(loc[1] + 1, loc[0]) == "0");
        }

        // below to right
        if (loc[0] + 1 < 0 || loc[0] + 1 >= width || loc[1] + 1 < 0) {
            toReturn.add(true);
        } else {
            toReturn.add(board.getModel().getValueAt(loc[1] + 1, loc[0] + 1) == "0");
        }

        // direction
        if (loc[0] % 2 == 0) {
            toReturn.add(true);
        } else {
            toReturn.add(false);
        }

//        for (int x = 0; x < 5; x++) {
//            for (int y = 0; y < 4; y++) {
//                if (loc[1] - 2 + x < 0 || loc[0] - 1 + y < 0) {
//                    toReturn.add(true);
//                } else {
//                    toReturn.add(board.getModel().getValueAt(y, x) == "0");
//                }
//            }
//        }
//
//
//        if (loc[0] % 2 == 0) {
//            toReturn.add(true);
//            toReturn.add(true);
//        } else {
//            toReturn.add(false);
//            toReturn.add(false);
//        }

        return toReturn;
    }





    public void genomeSetup() {


        InnovationCounter nodeInnovation = new InnovationCounter();
        InnovationCounter connectionInnovation = new InnovationCounter();

        Genome genome = new Genome();

        // TODO: clean up adding input nodes with a loop
        int n1 = nodeInnovation.getInnovation();
        int n2 = nodeInnovation.getInnovation();
        int n3 = nodeInnovation.getInnovation();
        int n4 = nodeInnovation.getInnovation();
        int n5 = nodeInnovation.getInnovation();
        int n6 = nodeInnovation.getInnovation();
//        int n7 = nodeInnovation.getInnovation();
//        int n8 = nodeInnovation.getInnovation();
//        int n9 = nodeInnovation.getInnovation();
//        int n10 = nodeInnovation.getInnovation();
//        int n11 = nodeInnovation.getInnovation();
//        int n12 = nodeInnovation.getInnovation();
//        int n13 = nodeInnovation.getInnovation();
//        int n14 = nodeInnovation.getInnovation();
//        int n15 = nodeInnovation.getInnovation();
//        int n16 = nodeInnovation.getInnovation();
//        int n17 = nodeInnovation.getInnovation();
//        int n18 = nodeInnovation.getInnovation();
//        int n19 = nodeInnovation.getInnovation();
//        int n20 = nodeInnovation.getInnovation();
//        int n21 = nodeInnovation.getInnovation();
//        int n22 = nodeInnovation.getInnovation();

        // TODO: use a loop here too
        int n7 = nodeInnovation.getInnovation();
        int n8 = nodeInnovation.getInnovation();
        int n9 = nodeInnovation.getInnovation();
        int n10 = nodeInnovation.getInnovation();
//        int n11 = nodeInnovation.getInnovation();

        // TODO: set types using a loop
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n1));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n2));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n3));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n4));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n5));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.INPUT, n6));

        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.OUTPUT, n7));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.OUTPUT, n8));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.OUTPUT, n9));
        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.OUTPUT, n10));
//        genome.addNodeGene(new NodeGene(NodeGene.GENETYPE.OUTPUT, n11));


        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                int c = connectionInnovation.getInnovation();
                genome.addConnectionGene(new ConnectionGene(genome.getNodes().get(i).getId(), genome.getNodes().get(6 + j).getId(), 0.5f, true, c));
            }
        }




        eval = new Evaluator(100, genome, nodeInnovation, connectionInnovation) {
            @Override
            protected float evaluateGenome(final Genome genome) {

                return max(1, genome.getBrain().fitness);
            }
        };

        timer = new Timer(framerate, this);
        timer.start();


        initBrains();

    }






    private void setModelWithVal(Object val, int i, int j) {

        TableModel model = board.getModel();

        switch ((String) val) {
            case "B":
                model.setValueAt("B", i, j);
                break;

            case "G":
                model.setValueAt("G", i, j);
                break;

            default:
                model.setValueAt(" ", i, j);
                break;
        }
    }


}






