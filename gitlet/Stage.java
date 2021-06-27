package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Zheyuan Hu
 */
public class Stage implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = 2337317767252720374L;
    /**
     * map mapping file name to blob. Contains all staged files.
     */
    private HashMap<String, Blob> _stagingArea;

    /**
     * constructor.
     */
    public Stage() {
        _stagingArea = new HashMap<>();
    }

    /** encapsulate hashmap's get function.
     * @param s key.
     * @return get
    **/
    public Blob get(String s) {
        return _stagingArea.getOrDefault(s, null);
    }

    /**
     * @return the hashmap.
    **/
    public HashMap<String, Blob> getAll() {
        return _stagingArea;
    }

    /** encapsulate hashmap's contains function.
     * @param s key.
     * @return contains
    **/
    public boolean contains(String s) {
        return _stagingArea.containsKey(s);
    }

    /** encapsulate hashmap's containsKey function.
     * @return the hashmap.
    **/
    public boolean isEmpty() {
        return _stagingArea.isEmpty();
    }

    /** add a file to the staging area.
     * @param fileName file name.
    **/
    public void add(String fileName) {
        Blob b = fileToBlob(fileName);
        _stagingArea.put(fileName, b);
    }

    /**
     * convert a file to a blob.
     * @param fileName file name.
     * @return blob.
     */
    public Blob fileToBlob(String fileName) {
        File f = new File(fileName);
        File copy = Utils.join(CommandLineTools.STAGE_DIR, fileName);
        Object content = Utils.readContents(f);
        Utils.writeContents(copy, content);
        Blob blob = new Blob(copy.getName());
        return blob;
    }

    /**
     * update the content of a file.
     * @param fileName file name.
     */
    public void updateFile(String fileName) {
        File f = new File(fileName);
        File staged = Utils.join(CommandLineTools.STAGE_DIR, fileName);
        Object contents = Utils.readContents(f);
        Utils.writeContents(staged, contents);
    }

    /**
     * remove a file from the staged area.
     * @param fileName file name.
     */
    public void remove(String fileName) {
        File removeFile = Utils.join(CommandLineTools.STAGE_DIR, fileName);
        removeFile.delete();
        _stagingArea.remove(fileName);
    }

    /** clear the stage and remove files from the staging dir. **/
    public void clear() {
        for (String fileName : _stagingArea.keySet()) {
            File f = Utils.join(CommandLineTools.STAGE_DIR, fileName);
            f.delete();
        }
        _stagingArea.clear();
    }
}
