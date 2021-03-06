package music.database.data.items;

public abstract class ImageContainer extends DataItem {
    private byte[] m_image;

    ImageContainer(int id, SQLItem type, byte[] image) {
        super(id, type);
        m_image = image;
    }

    public byte[] getImage() {
        return m_image;
    }

    public void setImage(byte[] image) {
        m_image = image;
    }

    public abstract String getName();
}
