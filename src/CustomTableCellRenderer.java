import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {


    @Override
    public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {

        Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);

        switch ((String) table.getModel().getValueAt(row, column)) {
            case " ":
                cell.setBackground(Color.lightGray);
                break;

            case "0":
                cell.setBackground(Color.black);
                cell.setForeground(Color.black);
                break;

            case "F":
                cell.setBackground(Color.green);
                cell.setForeground(Color.green);
                break;

            case "G":
                cell.setBackground(Color.cyan);
                cell.setForeground(Color.cyan);
                break;

            case "B":
                cell.setBackground(new Color(0, 102, 0));
                cell.setForeground(new Color(0, 102, 0));
                break;

            case "D":
                cell.setBackground(Color.red);
                cell.setForeground(Color.red);
                break;

            case "V":
                cell.setBackground(Color.yellow);
                cell.setForeground(Color.yellow);
                break;

            default:
                cell.setBackground(Color.lightGray);
        }
        if (table.getModel().getValueAt(row, column)  == " ") {
            cell.setBackground(Color.lightGray);
        }
        return cell;
    }

}
