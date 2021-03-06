package music.database.gui.renderers;

import music.database.data.items.DataItem;

import javax.swing.*;
import java.awt.*;

public class DataItemComboBoxRenderer extends JLabel implements ListCellRenderer<DataItem> {

    public DataItemComboBoxRenderer() {
        setOpaque(true);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(200, 20));
    }

    public Component getListCellRendererComponent(
            JList list,
            DataItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        if (value != null) {
            String name = value.getName();
            if (name != null) {
                setText(name);
            }
        }

        return this;
    }
}