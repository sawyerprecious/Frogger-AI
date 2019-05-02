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


                    eval.evaluate();
                    System.out.print("Generation: "+genNum);
                    System.out.print("\tHighest fitness: "+eval.getHighestFitness());
                    System.out.print("\tAmount of species: "+eval.getSpeciesAmount());
                    System.out.print("\n");

                    if (genNum%5==1) {
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
        if (loc[1] % 2 == 0) {
            toReturn.add(true);
        } else {
            toReturn.add(false);
        }

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

        // TODO: use a loop here too
        int n7 = nodeInnovation.getInnovation();
        int n8 = nodeInnovation.getInnovation();
        int n9 = nodeInnovation.getInnovation();
        int n10 = nodeInnovation.getInnovation();

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






