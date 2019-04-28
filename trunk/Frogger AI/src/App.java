import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sawyerprecious on 2019-04-11.
 */
public class App {
    private JPanel menuPanel;
    private JButton buttonEnter;
    private JTextField difficultyIndex1100TextField;
    private JTextField numberOfGenerationsTextField;

    private Game game;

    public int numGens;
    public int diffIndex;

    public App() {
        buttonEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    numGens = Integer.parseInt(numberOfGenerationsTextField.getText());
                    diffIndex = Integer.parseInt(difficultyIndex1100TextField.getText());

                    if (diffIndex < 1 || diffIndex > 100) {
                        throw new Exception("Enter difficulty between 1 and 100");
                    }

                    JOptionPane.showMessageDialog(null, "setting up with " + numGens +
                            " generations and difficulty = " + diffIndex);

                    setup();

                } catch (Exception error) {

                    JOptionPane.showMessageDialog(null, "Error: " + error);
                }
            }
        });


    }

    void setup() {

        Container parent = menuPanel.getParent();

        game = new Game(parent.getHeight() / 10, diffIndex);

        JPanel card2 = game.gamePanel;

        parent.add(card2, "Game");

        CardLayout cards = (CardLayout) parent.getLayout();

        cards.next(parent);

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        JPanel cards = new JPanel(new CardLayout());
        JPanel card1 = new App().menuPanel;
        cards.add(card1, "Menu");
        frame.setContentPane(cards);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        frame.setSize(800, 800);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);

        frame.setVisible(true);
    }
}
