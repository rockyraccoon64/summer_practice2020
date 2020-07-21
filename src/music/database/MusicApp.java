package music.database;

import music.database.items.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

public class MusicApp extends JFrame implements WindowListener, ActionListener {

    public static MusicApp mainFrame;
    private static final int APP_WIDTH = 1000;
    private static final int APP_HEIGHT = 750;
    private static final Color BACKGROUND_COLOR = new Color(255, 244, 161);
    private static final int INFO_PANEL_WIDTH = APP_WIDTH / 3;
    private static final int INFO_PANEL_BORDER = 30;

    public MusicApp() {
        setLayout(new BorderLayout());

        setTitle("Музыкальная база данных");
        setSize(APP_WIDTH, APP_HEIGHT);
        getContentPane().setBackground(BACKGROUND_COLOR);

        showBandListPage();
        //showAlbumListPage(1);

        addWindowListener(this);

        pack();
        setVisible(true);
    }

    public void showTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        //topPanel.setPreferredSize(new Dimension(APP_WIDTH, 75));
        topPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton backButton = new JButton("< назад");
        backButton.setPreferredSize(new Dimension(150, 20));
        buttonPanel.add(backButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        JLabel mainLabel = new JLabel("Музыкальная база данных");
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainLabel.setFont(new Font("Arial", Font.BOLD, 30));
        topPanel.add(mainLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        mainLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }
    public static void main(String[] args) {
        DataStorage.initialize();
        DataStorage.refreshData();

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MusicApp();
            }
        });
    }

    public JPanel createListPanel(String label) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 30, 30, 30),
                BorderFactory.createLineBorder(Color.BLACK, 3)));
        listPanel.setOpaque(false);

        JLabel bandLabel = createListLabel(label);
        listPanel.add(bandLabel, BorderLayout.NORTH);

        return listPanel;
    }

    public class ListMouseAdapter extends MouseAdapter {
        private SQLItem m_item;

        public ListMouseAdapter(SQLItem item) {
            m_item = item;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JList list = (JList)e.getSource();
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                Object selectedItem = list.getModel().getElementAt(index);
                switch (m_item) {
                    case BANDS:
                        Band band = (Band)selectedItem;
                        showAlbumListPage(band.getID());
                        revalidate();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class ImageButtonListener implements ActionListener {

        private SQLItem m_item;
        private DataItem m_dataItem;
        private JLabel m_label;

        ImageButtonListener(SQLItem item, DataItem dataItem, JLabel label) {
            m_item = item;
            m_dataItem = dataItem;
            m_label = label;
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(MusicApp.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    DataStorage.insertBlobFromFile(m_item, m_dataItem.getID(), file);
                    JOptionPane.showMessageDialog(MusicApp.this,
                            "Изображение загружено.",
                            "Загрузка изображения",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshImage(m_label, (Band)m_dataItem);
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MusicApp.this,
                            "Не удалось загрузить изображение.",
                            "Загрузка изображения",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void showBandListPage() {
        getContentPane().removeAll();
        showTopPanel();

        DefaultListModel<Band> listModel = new DefaultListModel<>();
        Collection<DataItem> dataItems = DataStorage.getItems(SQLItem.BANDS);
        for (DataItem dataItem : dataItems) {
            Band band = (Band)dataItem;
            listModel.addElement(band);
        }

        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new BandRenderer());
        //m_list.getSelectionModel().addListSelectionListener(MusicApp.this);
        list.addMouseListener(new ListMouseAdapter(SQLItem.BANDS));

        JPanel listPanel = createListPanel("Группы");

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        listPanel.add(scrollPane);

        add(listPanel, BorderLayout.CENTER);
    }

    public void showAlbumListPage(int bandID) {
        getContentPane().removeAll();
        showTopPanel();

        Band thisBand = (Band)DataStorage.getItemByID(SQLItem.BANDS, bandID);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, APP_HEIGHT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, INFO_PANEL_BORDER, INFO_PANEL_BORDER, 0));

        JLabel imageLabel = new JLabel();

        refreshImage(imageLabel, thisBand);

        infoPanel.add(imageLabel);

        TreeSet<Musician> musicians = thisBand.getMusicians();

        for (Musician musician : musicians) {
            infoPanel.add(createMusicianLabel(musician));
        }
        add(infoPanel, BorderLayout.WEST);

        DefaultListModel<Album> listModel = new DefaultListModel<>();
        //Collection<DataItem> albums = DataStorage.getItems(SQLItem.ALBUMS);
        //albums.removeIf(item -> ((Album)item).getBand().getID() == bandID);

        TreeSet<Album> albums = thisBand.getAlbums();

        for (Album album : albums) {
            listModel.addElement(album);
        }

        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new AlbumRenderer());

        JPanel listPanel = createListPanel("Альбомы " + thisBand.getName());

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(APP_WIDTH - INFO_PANEL_WIDTH, APP_HEIGHT));
        listPanel.add(scrollPane);

        JButton imageButton = new JButton("Загрузить изображение");
        infoPanel.add(imageButton);
        imageButton.addActionListener(new ImageButtonListener(SQLItem.BANDS, thisBand, imageLabel));

        add(listPanel, BorderLayout.CENTER);
    }

    public void refreshImage(JLabel component, Band band) {
        byte[] imageByteArray = band.getImage();
        try {
            if (imageByteArray != null) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageByteArray));
                int width = image.getWidth();
                int height = image.getHeight();
                int scaledWidth;
                int scaledHeight;
                if (width > height) {
                    scaledWidth = INFO_PANEL_WIDTH - INFO_PANEL_BORDER;
                    scaledHeight = (int)(height * ((double)scaledWidth / width));
                }
                else {
                    scaledHeight = INFO_PANEL_WIDTH - INFO_PANEL_BORDER;
                    scaledWidth = (int)(width * ((double)scaledHeight / height));
                }
                component.setIcon(new ImageIcon(
                        image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT)
                ));
            }
        }
        catch (IOException ex) {
            System.out.println("Error while loading image");
        }
    }

    public static JLabel createMusicianLabel(Musician musician) {
        StringBuilder sb = new StringBuilder(musician.getName());

        boolean isFirst = true;

        for (Instrument instrument : musician.getInstruments()) {
            if (isFirst) {
                sb.append(" - ");
                isFirst = false;
            }
            else sb.append(", ");

            sb.append(instrument.getName().toLowerCase());
        }

        sb.insert(0, "<html>");
        sb.append("</html>");

        JLabel label = new JLabel(sb.toString());
        label.setFont(new Font("Arial", Font.PLAIN, 15));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return label;
    }

    public static JLabel createListLabel(String text) {
        JLabel bandLabel = new JLabel(text);
        bandLabel.setHorizontalAlignment(JLabel.CENTER);
        bandLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        bandLabel.setBackground(new Color(171, 255, 156));
        bandLabel.setOpaque(true);
        return bandLabel;
    }

    @Override
    public void windowClosing(WindowEvent evt) {
        System.exit(0);  // Terminate the program
    }

    // Not Used, BUT need to provide an empty body to compile.
    @Override public void windowOpened(WindowEvent evt) { }
    @Override public void windowClosed(WindowEvent evt) { }
    // For Debugging
    @Override public void windowIconified(WindowEvent evt) { System.out.println("Window Iconified"); }
    @Override public void windowDeiconified(WindowEvent evt) { System.out.println("Window Deiconified"); }
    @Override public void windowActivated(WindowEvent evt) { System.out.println("Window Activated"); }
    @Override public void windowDeactivated(WindowEvent evt) { System.out.println("Window Deactivated"); }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
