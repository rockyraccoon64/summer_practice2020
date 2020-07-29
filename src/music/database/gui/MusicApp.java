package music.database.gui;

import music.database.data.DataStorage;
import music.database.data.items.*;
import music.database.gui.dialogs.*;
import music.database.gui.renderers.AlbumRenderer;
import music.database.gui.renderers.BandRenderer;
import music.database.gui.renderers.SongRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static javax.swing.BoxLayout.Y_AXIS;

public class MusicApp extends JFrame implements WindowListener, ActionListener {

    public static final Color BACKGROUND_COLOR = new Color(255, 244, 161);
    public static final MusicApp MAIN_WINDOW = new MusicApp();
    private static final int APP_WIDTH = 1000;
    private static final int APP_HEIGHT = 750;
    private static final int INFO_PANEL_WIDTH = APP_WIDTH / 3;
    private static final int INFO_PANEL_BORDER = 30;
    private static byte[] IMAGE_PLACEHOLDER;
    private JLabel m_imageLabel;

    private MusicApp() {
        File file = new File("images/placeholder.png");
        try {
            FileInputStream input = new FileInputStream(file);
            IMAGE_PLACEHOLDER = input.readAllBytes();
        }
        catch (IOException ex) {
            System.out.println("Image placeholder not found");
            IMAGE_PLACEHOLDER = null;
        }
    }

    public void showWindow() {
        setLayout(new BorderLayout());

        setTitle("Музыкальная база данных");
        setSize(APP_WIDTH, APP_HEIGHT);
        getContentPane().setBackground(BACKGROUND_COLOR);

        showBandList();

        addWindowListener(this);

        pack();
        setVisible(true);
    }

    private void showTopPanel(DataItem item) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        if (item != null) {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            JButton backButton = new JButton("< назад");
            backButton.setPreferredSize(new Dimension(150, 20));
            buttonPanel.add(backButton);
            topPanel.add(buttonPanel, BorderLayout.SOUTH);
            backButton.addActionListener(new BackButtonListener(item));
        }

        JLabel mainLabel = new JLabel("Музыкальная база данных");
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainLabel.setFont(new Font("Arial", Font.BOLD, 30));
        topPanel.add(mainLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        mainLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    public static void main(String[] args) {
        DataStorage.initialize();
        DataStorage.refreshData();

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MAIN_WINDOW.showWindow();
            }
        });
    }

    private JPanel createListPanel(String label) {
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
                        showBandPage(band.getID());
                        revalidate();
                        break;
                    case ALBUMS:
                        Album album = (Album)selectedItem;
                        showAlbumPage(album.getID());
                        revalidate();
                    default:
                        break;
                }
            }
        }
    }

    private class BackButtonListener implements ActionListener {
        private DataItem m_item;

        BackButtonListener(DataItem item) {
            m_item = item;
        }

        public void actionPerformed(ActionEvent e) {
            switch (m_item.getType()) {
                case BANDS:
                    showBandList();
                    break;
                case ALBUMS:
                    showBandPage(((Album)m_item).getBand().getID());
                    break;
                default:
                    break;
            }
        }
    }

    public void showBandList() {
        getContentPane().removeAll();
        showTopPanel(null);

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

    public void showBandPage(int bandID) {
        getContentPane().removeAll();
        Band thisBand = (Band)DataStorage.getItemByID(SQLItem.BANDS, bandID);
        showTopPanel(thisBand);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, APP_HEIGHT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, INFO_PANEL_BORDER, INFO_PANEL_BORDER, 0));

        m_imageLabel = new JLabel();
        refreshImage(thisBand.getImage(), m_imageLabel, INFO_PANEL_WIDTH - INFO_PANEL_BORDER);
        infoPanel.add(m_imageLabel);

        JLabel bandDateLabel = new JLabel();
        short formYear = thisBand.getFormYear();
        short disbandYear = thisBand.getDisbandYear();
        StringBuilder sb = new StringBuilder("Годы активности: ");
        sb.append(formYear);
        sb.append("-");
        if (disbandYear > 0) {
            sb.append(disbandYear);
        }
        else {
            sb.append("...");
        }
        bandDateLabel.setText(sb.toString());
        setInfoPanelLabelStyle(bandDateLabel);
        infoPanel.add(bandDateLabel);

        Vector<Musician> musicians = thisBand.getMusicians();
        for (Musician musician : musicians) {
            infoPanel.add(createMusicianLabel(musician));
        }
        add(infoPanel, BorderLayout.WEST);

        DefaultListModel<Album> listModel = new DefaultListModel<>();
        Vector<Album> albums = thisBand.getAlbums();
        for (Album album : albums) {
            listModel.addElement(album);
        }

        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new AlbumRenderer());
        list.addMouseListener(new ListMouseAdapter(SQLItem.ALBUMS));

        JPanel listPanel = createListPanel("Альбомы " + thisBand.getName());

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(APP_WIDTH - INFO_PANEL_WIDTH, APP_HEIGHT));
        listPanel.add(scrollPane);

        JButton editButton = new JButton("Редактировать...");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BandEditDialog(bandID);
            }
        });
        infoPanel.add(editButton);

        add(listPanel, BorderLayout.CENTER);
    }

    private void showInstrumentRemovalDialog(JDialog mainDialog) {
        //TODO
    }

    private void showBandAdditionDialog() {
        //TODO
    }

    public void showAlbumPage(int albumID) {
        getContentPane().removeAll();
        Album album = (Album)DataStorage.getItemByID(SQLItem.ALBUMS, albumID);
        Band band = album.getBand();
        showTopPanel(album);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, APP_HEIGHT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, INFO_PANEL_BORDER, INFO_PANEL_BORDER, 0));

        m_imageLabel = new JLabel();
        refreshImage(album.getImage(), m_imageLabel, INFO_PANEL_WIDTH - INFO_PANEL_BORDER);

        infoPanel.add(m_imageLabel);

        Genre genre = album.getGenre();
        if (genre != null) {
            JLabel genreLabel = new JLabel("Жанр: " + album.getGenre().getName());
            setInfoPanelLabelStyle(genreLabel);
            infoPanel.add(genreLabel);
        }

        LocalDate releaseDate = album.getReleaseDate();
        if (releaseDate != null) {
            JLabel releaseDateLabel = new JLabel("Дата выпуска: " + releaseDate.toString());
            setInfoPanelLabelStyle(releaseDateLabel);
            infoPanel.add(releaseDateLabel);
        }

        add(infoPanel, BorderLayout.WEST);

        DefaultListModel<Song> listModel = new DefaultListModel<>();
        Vector<Song> songs = album.getSongs();
        for (Song song : songs) {
            listModel.addElement(song);
        }

        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new SongRenderer());

        JPanel listPanel = createListPanel(band.getName() + " - " + album.getName());

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(APP_WIDTH - INFO_PANEL_WIDTH, APP_HEIGHT));
        listPanel.add(scrollPane);

        JButton editButton = new JButton("Редактировать...");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AlbumEditDialog(albumID);
            }
        });
        infoPanel.add(editButton);

        add(listPanel, BorderLayout.CENTER);
    }

    public static void refreshImage(byte[] bytes, JLabel imageLabel, int maxDimension) {
        byte[] imageByteArray = bytes;
        if (imageByteArray == null) {
            imageByteArray = IMAGE_PLACEHOLDER;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageByteArray));
            int width = image.getWidth();
            int height = image.getHeight();
            int scaledWidth;
            int scaledHeight;
            if (width > height) {
                scaledWidth = maxDimension;
                scaledHeight = (int)(height * ((double)scaledWidth / width));
            }
            else {
                scaledHeight = maxDimension;
                scaledWidth = (int)(width * ((double)scaledHeight / height));
            }
            imageLabel.setIcon(new ImageIcon(
                    image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
            ));
        }
        catch (IOException ex) {
            System.out.println("Error while loading image");
        }
    }

    private static JLabel createMusicianLabel(Musician musician) {
        StringBuilder sb = new StringBuilder(musician.getName());

        LocalDate birthDate = musician.getBirthDate();
        LocalDate deathDate = musician.getDeathDate();

        if (birthDate != null) {
            sb.append(" (");
            sb.append(birthDate.getYear());
            sb.append("-");
            if (deathDate != null) {
                sb.append(deathDate.getYear());
            }
            else {
                sb.append("...");
            }
            sb.append(")");
        }
        else if (deathDate != null) {
            sb.append("(...-");
            sb.append(deathDate.getYear());
            sb.append(")");
        }

        boolean isFirst = true;

        for (Instrument instrument : musician.getInstruments()) {
            if (isFirst) {
                sb.append(" - ");
                isFirst = false;
            }
            else sb.append(", ");

            sb.append(instrument.getName().toLowerCase());
        }

        JLabel label = new JLabel(sb.toString());
        setInfoPanelLabelStyle(label);

        return label;
    }

    private static void setInfoPanelLabelStyle(JLabel label) {
        String text = label.getText();
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(text);
        sb.append("</html>");
        label.setText(sb.toString());
        label.setFont(new Font("Arial", Font.PLAIN, 15));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    }

    private static JLabel createListLabel(String text) {
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