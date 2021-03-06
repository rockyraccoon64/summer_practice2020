package music.database.gui.dialogs;

import music.database.data.DataStorage;
import music.database.data.items.SQLItem;
import music.database.fields.Field;
import music.database.fields.FieldContainer;
import music.database.fields.NStringField;
import music.database.gui.MusicApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

public class GenreAdditionDialog extends JDialog {

    public GenreAdditionDialog(GenreManagementDialog owner) {
        super(owner, "Добавить жанр");
        refresh();
    }

    public void refresh() {
        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(MusicApp.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int currentY = 0;

        JLabel nameLabel = new JLabel("Название:");
        nameLabel.setOpaque(false);
        c.gridx = 0;
        c.gridy = currentY;
        contentPane.add(nameLabel, c);

        JTextField nameText = new JTextField();
        //nameText.setMinimumSize(new Dimension(250, 20));
        nameText.setPreferredSize(new Dimension(250, 20));
        c.gridx = 1;
        c.gridy = currentY;
        contentPane.add(nameText, c);

        currentY++;

        JButton addButton = new JButton("Добавить жанр");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<Field> fields = new Vector<>();
                fields.add(new NStringField("Name", nameText.getText()));

                try {
                    DataStorage.insert(SQLItem.GENRES, new FieldContainer(null, fields));
                    JOptionPane.showMessageDialog(GenreAdditionDialog.this, "Жанр добавлен.",
                            "Добавление успешно", JOptionPane.INFORMATION_MESSAGE);
                    MusicApp.MAIN_WINDOW.showBandList();
                    dispose();
                }
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(GenreAdditionDialog.this, "Обновление не удалось.",
                            "Добавление не удалось", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        c.gridx = 0;
        c.gridy = currentY;
        c.gridwidth = 2;
        contentPane.add(addButton, c);

        setPreferredSize(new Dimension(430, 120));
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
