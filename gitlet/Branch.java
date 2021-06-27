package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;

/**
 * @author Zheyuan Hu
 */
public class Branch implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = -2464501943803134589L;
    /** SHA-1 head ID. */
    private String _headID;
    /** A set of tracked files.**/
    private HashSet<String> _trackedFiles = new HashSet<>();
    /** branch name. **/
    private String _name;

    /**
     * constructor.
     * @param name branch name.
     */
    public Branch(String name) {
        _name = name;
    }

    /**
     * update branch's head to the given commit ID.
     * @param id
     */
    public void updateHead(String id) {
        _headID = id;
        File file = Utils.join(CommandLineTools.COMMIT_DIR, id);
        Commit head = Utils.readObject(file, Commit.class);
        _trackedFiles.addAll(head.getBlobs().keySet());
    }

    /**
     * @param fileName file name.
     * @return tracked or not.
     */
    public boolean hasTracked(String fileName) {
        return _trackedFiles.contains(fileName);
    }

    /**
     * @return name.
     */
    public String getName() {
        return _name;
    }

    /**
     * set branch name.
     * @param name name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return head commit id
     */
    public String getHeadID() {
        return _headID;
    }

    /**
     * @return tracked files.
     */
    public HashSet<String> getTrackedFiles() {
        return _trackedFiles;
    }
}
