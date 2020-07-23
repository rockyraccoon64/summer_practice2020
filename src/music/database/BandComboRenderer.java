package music.database;

import music.database.items.Band;

import javax.swing.*;
import java.awt.*;

class BandComboRenderer extends JLabel implements ListCellRenderer<Band> {

    private final Color BACKGROUND_COLOR = Color.WHITE;

    public BandComboRenderer() {
        setOpaque(false);
        setBackground(BACKGROUND_COLOR);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(200, 20));
    }

    public Component getListCellRendererComponent(
            JList list,
            Band value,
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

        setText(value.getName());

        return this;
    }
}