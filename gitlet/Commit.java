package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Zheyuan Hu
 */
public class Commit implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = 3263038956317828008L;
    /** log message. **/
    private String _log;
    /** timestamp. **/
    private String _timestamp;
    /** this commit's parent's hashcode. **/
    private String _parent;
    /** branch name. **/
    private String _branch;
    /** SHA-1 code of this commit. **/
    private String _id;
    /** the hashcode of the commit that we are merging from. */
    private String _mergeFrom;
    /** A map mapping file name to blob. **/
    private HashMap<String, Blob> _blobs = new HashMap<>();

    /**
     * constructor.
     * @param branch branch name to refer to.
     * @param log log messages.
     */
    public Commit(String branch, String log) {
        _branch = branch;
        _log = log;
        _parent = null;
        _timestamp = getZonedTime();
        _id = generateID();
    }

    /**
     * constructor.
     * @param parent parent commit.
     * @param branch branch name.
     * @param log log messages.
     * @param stage stage.
     * @param removed removed.
     */
    public Commit(Commit parent, String branch,
            String log, Stage stage, HashSet<String> removed) {
        _parent = parent.getID();
        _branch = branch;
        _log = log;
        _timestamp = getZonedTime();
        HashMap<String, Blob> parentBlobs = parent.getBlobs();
        for (String s : parentBlobs.keySet()) {
            if (!removed.contains(s)) {
                _blobs.put(s, parentBlobs.get(s));
            }
        }
        for (String fileName: stage.getAll().keySet()) {
            if (_blobs.containsKey(fileName)) {
                _blobs.replace(fileName, stage.get(fileName));
            } else {
                _blobs.put(fileName, stage.get(fileName));
            }
        }
        _id = generateID();
    }

    /**
     * constructor for a merge commit.
     * @param parent parent commit.
     * @param mergeFrom merge commit.
     * @param branch branch name.
     * @param log log messages.
     * @param stage stage.
     * @param removed removed.
     */
    Commit(Commit parent, Commit mergeFrom, String branch,
            String log, Stage stage, HashSet<String> removed) {
        this(parent, branch, log, stage, removed);
        _mergeFrom = mergeFrom.getID();
    }

    /**
     * @return the zoned time of this instant.
     * example: Wed Dec 31 16:00:00 1969 -0800
     */
    public String getZonedTime() {
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("EEE " + "MMM d HH:mm:ss yyyy xxxx");
        return now.format(formatter);
    }

    /**
     * return the unique ID.
     * @return ID.
     */
    public String getID() {
        return _id;
    }

    /** Returns the unique hashcode. **/
    public String generateID() {
        ArrayList<Object> hash = new ArrayList<>();
        hash.add(_log);
        hash.add(_branch);
        if (_parent != null) {
            hash.add(_parent);
        }
        for (String s : _blobs.keySet()) {
            hash.add(_blobs.get(s).getID());
        }
        return Utils.sha1(hash);
    }

    /**
     * @return blobs
     */
    public HashMap<String, Blob> getBlobs() {
        return _blobs;
    }

    /**
     * @return getter
     */
    public String getLog() {
        return _log;
    }

    /**
     * @return getter
     */
    public String getTimestamp() {
        return _timestamp;
    }

    /**
     * @return getter
     */
    public String getParent() {
        return _parent;
    }

    /**
     * @return getter
     */
    public String getBranch() {
        return _branch;
    }

    /**
     * @param name name
     */
    public void setBranchName(String name) {
        _branch = name;
    }

    /**
     * @return getter
     */
    public String getMergeFrom() {
        return _mergeFrom;
    }
}
