import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created by sawyerprecious on 2019-04-11.
 */
public class Game {

    public int numLanes;
    public JPanel gamePanel;

    private JTable board;

    private int width;
    private int diff;

    public Game(int nl, int diff) {
        numLanes = nl;

        width = 25;

        this.diff = diff;

    }

    private void createUIComponents() {
        DefaultTableModel model = new DefaultTableModel();

        for (int i = 0; i < width; i ++) {
            model.addColumn("i");
        }

        // Safe lane to start in
        Lane sLane = new Lane(width, diff);
        sLane.difficulty = 0;
        model.addRow(sLane.getLaneContents());

        // Middle lanes
        for (int i = 1; i < numLanes - 1; i++) {
            Lane lane = new Lane(width, diff);
            lane.difficulty = diff;
            model.addRow(lane.getLaneContents());
        }

        // Safe lane for finish line
        Lane fLane = new Lane(width, diff);
        fLane.difficulty = 0;
        model.addRow(fLane.getLaneContents());


        board = new JTable(model);

        board.setRowHeight(10);
    }
}
