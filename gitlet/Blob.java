package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zheyuan Hu
 */
public class Blob implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = -8000747699547298285L;
    /** file name. */
    private String _fileName;
    /**unique ID. */
    private String _id;
    /** contents. */
    private byte[] _contents;
    /** contents in string. */
    private String _string;
    /**
     * constructor.
     * @param fileName file name.
     */
    public Blob(String fileName) {
        _fileName = fileName;
        _contents = Utils.readContents(new File(_fileName));
        _string = Utils.readContentsAsString(new File(_fileName));
        _id = generateID();
    }

    /**
     * @return SHA-1 ID
     */
    public String generateID() {
        ArrayList<Object> hash = new ArrayList<>(List.of(_fileName, _contents));
        return Utils.sha1(hash);
    }

    /**
     * @return id
     */
    public String getID() {
        return _id;
    }

    /**
     * @return contents
     */
    public byte[] getContents() {
        return _contents;
    }

    /**
     * @return content as string
     */
    public String getString() {
        return _string;
    }

    /**
     * @return file name
     */
    public String getFileName() {
        return _fileName;
    }
}
