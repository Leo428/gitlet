package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Zheyuan Hu
 */
public class Repository implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = -8322997645750190301L;
    /** A set of all the commits existing in this gitlet repo. **/
    private HashSet<String> _commits;
    /** The current branch of this repo. **/
    private Branch _head;
    /** A runtime map mapping hash to branch. **/
    private HashMap<String, Branch> _branches = new HashMap<>();
    /** The staging area. **/
    private Stage _stage;
    /** names of the untracked files. need to update every command execution.**/
    private HashSet<String> _untrackedFiles;
    /** Removed files since last commit.Need to clear it for every new commit.*/
    private HashSet<String> _removedFiles = new HashSet<>();
    /** Whether the repo has been initialized. **/
    private boolean _initialized = false;
    /** merge conflict. */
    private boolean _conflicts = false;
    /** repo gitlet path. */
    private File _repoDir = CommandLineTools.REPO_DIR;
    /**
     * Constructor.
     */
    public Repository() {
        _stage = new Stage();
        _commits = new HashSet<>();
        _branches.put("master", new Branch("master"));
        _untrackedFiles = new HashSet<>();
    }

    /**
     * init the repo. Create the first commit.
     */
    public void initialize() {
        Commit firstCommit = new Commit("master", "initial commit");
        makeCommit(firstCommit);
        _initialized = true;
    }

    /**
     * @param path path
     */
    public void setRepoDir(File path) {
        _repoDir = path;
    }

    /**
     * add to staging area.
     * @param fileName file name.
     */
    public void add(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Commit headCommit = getHeadCommit();
        if (headCommit.getBlobs().containsKey(fileName)) {
            Blob blob = headCommit.getBlobs().get(fileName);
            byte[] commited = (byte[]) blob.getContents();
            byte[] newContents = Utils.readContents(new File(fileName));
            if (Arrays.equals(commited, newContents)) {
                if (_stage.contains(fileName)) {
                    _stage.remove(fileName);
                }
            } else {
                if (_stage.contains(fileName)) {
                    _stage.updateFile(fileName);
                } else {
                    _stage.add(fileName);
                }
            }
        } else {
            if (_stage.contains(fileName)) {
                _stage.updateFile(fileName);
            } else {
                _stage.add(fileName);
            }
        }
        _removedFiles.remove(fileName);
        _untrackedFiles.remove(fileName);
    }

    /**
     * remove.
     * @param fileName file name.
     */
    public void remove(String fileName) {
        Commit headCommit = getHeadCommit();
        boolean inHeadCommit = headCommit.getBlobs().containsKey(fileName);
        boolean inStage = _stage.contains(fileName);
        if (!inHeadCommit && !inStage) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (headCommit.getBlobs().containsKey(fileName)) {
            _removedFiles.add(fileName);
            File file = new File(fileName);
            file.delete();
        }
        if (_stage.contains(fileName)) {
            _stage.remove(fileName);
        }
    }

    /**
     * get head branch.
     * @return head branch.
     */
    public Branch getHeadBranch() {
        return _head;
    }

    /**
     * @return head commit.
     */
    public Commit getHeadCommit() {
        return getCommit(_head.getHeadID());
    }

    /**
     * @return _commits
     */
    public HashSet<String> getCommits() {
        return _commits;
    }

    /**
     * @return _branches
     */
    public HashMap<String, Branch> getBranches() {
        return _branches;
    }

    /**
     * @param id id.
     * @return commit.
     */
    public Commit getCommit(String id) {
        if (id == null) {
            return null;
        }
        if (id.length() < Utils.UID_LENGTH) {
            for (String commitID : _commits) {
                if (commitID.substring(0, id.length()).equals(id)) {
                    id = commitID;
                    break;
                }
            }
        }
        if (!_commits.contains(id)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File file = Utils.join(Utils.join(_repoDir, "commits"), id);
        return Utils.readObject(file, Commit.class);
    }

    /**
     * @param id commit id
     * @return whether is there such a commit
     */
    public boolean hasCommit(String id) {
        return _commits.contains(id);
    }

    /**
     * add commit to the commit set.
     * It also clears the staging area and the remove set.
     * @param commit commit.
     * @param path path
     */
    public void copyCommit(Commit commit, File path) {
        String id = commit.getID();
        _commits.add(id);
        File file = Utils.join(path, id);
        Utils.writeObject(file, commit);
        if (!_branches.containsKey(commit.getBranch())) {
            Branch newBranch = new Branch(commit.getBranch());
            _branches.put(commit.getBranch(), newBranch);
        }
        _removedFiles.clear();
        _stage.clear();
    }

    /**
     * add commit to the commit set. Then update the current branch's
     * head to this commit. It also clears the staging area and the remove set.
     * @param commit commit.
     */
    public void makeCommit(Commit commit) {
        String id = commit.getID();
        _commits.add(id);
        _head = _branches.get(commit.getBranch());
        File file = Utils.join(Utils.join(_repoDir, "commits"), id);
        Utils.writeObject(file, commit);
        if (_branches.containsKey(commit.getBranch())) {
            _head.updateHead(commit.getID());
        } else {
            Branch newBranch = new Branch(commit.getBranch());
            newBranch.updateHead(commit.getID());
            _branches.put(commit.getBranch(), newBranch);
        }
        _removedFiles.clear();
        _stage.clear();
    }

    /**
     * commit with log.
     * @param log log messages
     */
    public void commit(String log) {
        Commit parent = getHeadCommit();
        String branch = _head.getName();
        Commit commit = new Commit(parent, branch, log, _stage, _removedFiles);
        makeCommit(commit);
    }

    /**
     * generate log.
     */
    public void log() {
        Commit c = getHeadCommit();
        while (c != null) {
            System.out.println("===");
            System.out.println("commit " + c.getID());
            if (c.getMergeFrom() != null) {
                System.out.println(String.format("Merge: %s %s",
                    c.getParent().substring(0, 7),
                    c.getMergeFrom().substring(0, 7)));
            }
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getLog());
            System.out.println();
            c = getCommit(c.getParent());
        }
    }

    /**
     * generate global log.
     */
    public void globalLog() {
        var fileNames = Utils.plainFilenamesIn(CommandLineTools.COMMIT_DIR);
        for (String id : fileNames) {
            Commit c = getCommit(id);
            System.out.println("===");
            System.out.println("commit " + c.getID());
            if (c.getMergeFrom() != null) {
                System.out.println(String.format("Merge: %s %s",
                    c.getParent().substring(0, 7),
                    c.getMergeFrom().substring(0, 7)));
            }
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getLog());
            System.out.println();
        }
    }

    /**
     * find the commit id based on commit log.
     * @param log log.
     */
    public void find(String log) {
        var match = _commits.stream()
            .filter(c -> getCommit(c).getLog().equals(log))
            .toArray();
        for (Object m : match) {
            System.out.println(m);
        }
        if (match.length == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * this is truly unnecessary. But there is a line limit of 60.
     */
    public void preStatus() {
        if (!_initialized) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        System.out.println("=== Branches ===");
    }

    /**
     * generate status.
     */
    public void status() {
        preStatus();
        var branchNames = _branches.keySet().stream().sorted()
            .collect(Collectors.toList());
        branchNames.forEach(name -> {
            if (name.equals(getHeadCommit().getBranch())) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        });
        System.out.println();
        System.out.println("=== Staged Files ===");
        var stage = _stage.getAll().keySet();
        var modified = stage.stream().filter(file -> {
            File staged = Utils.join(CommandLineTools.STAGE_DIR, file);
            File inStage = new File(file);
            return !Utils.readContentsAsString(staged)
                .equals(Utils.readContentsAsString(inStage));
        }).sorted().collect(Collectors.toSet());
        var removed = stage.stream().filter(file -> {
            File inStage = new File(file);
            return !inStage.exists();
        }).sorted().collect(Collectors.toSet());
        stage.removeAll(modified);
        stage.removeAll(removed);
        stage.stream().sorted().forEach(file -> System.out.println(file));
        System.out.println();
        System.out.println("=== Removed Files ===");
        _removedFiles.forEach(file -> System.out.println(file));
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        removed.addAll(getHeadCommit().getBlobs().keySet().
            stream().filter(fileName -> {
                File file = new File(fileName);
                return (!file.exists() && !_removedFiles.contains(fileName));
            }).collect(Collectors.toSet()));
        modified.addAll(getHeadCommit().getBlobs().keySet().
            stream().filter(fileName -> {
                File file = new File(fileName);
                if (!file.exists()) {
                    return false;
                }
                var contents = Utils.readContents(file);
                var b = getHeadCommit().getBlobs().get(fileName).getContents();
                return !Arrays.equals(contents, b)
                    && !modified.contains(fileName);
            }).collect(Collectors.toSet()));
        Stream.concat(modified.stream(), removed.stream()).sorted()
            .forEach(f -> {
                if (removed.contains(f)) {
                    System.out.println(f + " (deleted)");
                } else if (modified.contains(f)) {
                    System.out.println(f + " (modified)");
                }
            });
        System.out.println();
        System.out.println("=== Untracked Files ===");
        _untrackedFiles.stream().sorted().forEach(f -> System.out.println(f));
    }

    /**
     * create a new branch.
     * @param name branch name.
     */
    public void createBranch(String name) {
        if (_branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch branch = new Branch(name);
        branch.updateHead(getHeadCommit().getID());
        _branches.put(name, branch);
    }

    /**
     * remove the branch with the given branch name.
     * @param name branch name
     */
    public void removeBranch(String name) {
        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (_branches.get(name).equals(_head)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        _branches.remove(name);
    }

    /**
     * reset to the commit with commit id.
     * @param id commit id
     */
    public void reset(String id) {
        if (id == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = getCommit(id);
        checkoutByCommit(commit);
        _head.updateHead(id);
    }

    /**
     * reset to the commit with commit id.
     * @param id commit id
     * @param path path
     */
    public void resetRemote(String id, File path) {
        if (id == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = getCommit(id);
        checkoutByCommit(commit);
        _head.updateHead(id);
    }

    /**
     * @param branch branch name.
     */
    public void checkoutByBranch(String branch) {
        if (!_branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
        } else if (_head.getName().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit commit = getCommit(_branches.get(branch).getHeadID());
            checkoutByCommit(commit);
            _head = _branches.get(branch);
        }
    }

    /**
     * @param commit commit.
     */
    public void checkoutByCommit(Commit commit) {
        for (String filename : commit.getBlobs().keySet()) {
            if (_untrackedFiles.contains(filename)) {
                byte[] fileContent = Utils.readContents(new File(filename));
                byte[] commitFileContent =
                    (byte[]) commit.getBlobs().get(filename).getContents();
                if (!Arrays.equals(fileContent, commitFileContent)) {
                    System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        for (String filename : commit.getBlobs().keySet()) {
            Object content = commit.getBlobs().get(filename).getContents();
            Utils.writeContents(new File(filename), content);
        }
        for (String filename : _head.getTrackedFiles()) {
            if (!commit.getBlobs().containsKey(filename)) {
                File f = new File(filename);
                f.delete();
            }
        }
        _stage.clear();
    }

    /**
     * @param fileName file name.
     */
    public void checkoutFileInCurrent(String fileName) {
        Commit head = getHeadCommit();
        if (!head.getBlobs().containsKey(fileName)) {
            throw Utils.error("File does not exist in that commit.");
        } else {
            Blob b = head.getBlobs().get(fileName);
            Utils.writeContents(new File(b.getFileName()), b.getContents());
        }
    }

    /**
     * @param fileName file name.
     * @param id commit id.
     */
    public void checkoutFileByID(String fileName, String id) {
        Commit c = getCommit(id);
        if (c.getBlobs().containsKey(fileName)) {
            Blob b = c.getBlobs().get(fileName);
            Utils.writeContents(new File(b.getFileName()), b.getContents());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * @param dir dir.
     * @return set of all files in the dir.
     */
    public HashSet<String> getAllFiles(File dir) {
        HashSet<String> allFiles = new HashSet<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory() && !f.isHidden()) {
                allFiles.addAll(getAllFiles(f));
            } else if (f.isFile()) {
                allFiles.add(f.getName());
            }
        }
        return allFiles;
    }

    /**
     * update the untracked set.
     */
    public void updateUntracked() {
        _untrackedFiles.clear();
        var allFiles = getAllFiles(CommandLineTools.CWD_DIR);
        var temp = new HashSet<>(allFiles);
        for (String f : temp) {
            if (_head.hasTracked(f) || _stage.contains(f)) {
                allFiles.remove(f);
            }
        }
        _untrackedFiles.addAll(allFiles);
    }

    /**
     * @return true iff there is no change.
     */
    public boolean noChanges() {
        return _stage.isEmpty() && _removedFiles.isEmpty();
    }

    /**
     * @param branch branch name
     */
    public void preMerge(String branch) {
        if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (!noChanges()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (_head.getName().equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * @param headID head commit id
     * @param fromID from's head commit id
     * @return commit ID of the split point
     */
    public Commit splitPoint(String headID, String fromID) {
        Commit head = getCommit(headID);
        Commit from = getCommit(fromID);
        HashMap<String, Integer> head2Num = new HashMap<>();
        HashMap<String, Integer> from2Num = new HashMap<>();
        int i = 0;
        while (head != null) {
            head2Num.put(head.getID(), i);
            if (head.getMergeFrom() != null) {
                head2Num.put(head.getMergeFrom(), -1);
            }
            head = getCommit(head.getParent());
            i++;
        }
        i = 0;
        while (from != null) {
            if (head2Num.containsKey(from.getMergeFrom())) {
                String mergeID = from.getMergeFrom();
                from2Num.put(mergeID, head2Num.get(mergeID));
            } else if (head2Num.containsKey(from.getID())) {
                from2Num.put(from.getID(), i);
            }
            if (from2Num.size() == 2) {
                break;
            }
            from = getCommit(from.getParent());
            i++;
        }
        i = Integer.MAX_VALUE;
        String id = null;
        for (String x : from2Num.keySet()) {
            if (from2Num.get(x) < i) {
                id = x;
                i = from2Num.get(x);
            }
        }
        return getCommit(id);
    }

    /**
     * @param branch branch name
     */
    public void merge(String branch) {
        preMerge(branch);
        _conflicts = false;
        Branch from = _branches.get(branch);
        Commit splitPoint = splitPoint(_head.getHeadID(), from.getHeadID());
        if (splitPoint.getID().equals(from.getHeadID())) {
            System.out.println("Given branch is an ancestor of the "
                + "current branch.");
            System.exit(0);
        } else if (splitPoint.getID().equals(_head.getHeadID())) {
            _head.updateHead(from.getHeadID());
            checkoutByCommit(getCommit(_head.getHeadID()));
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit fromHead = getCommit(from.getHeadID());
        map2Func(splitPoint, fromHead).forEach((fileName, func) -> func.run());
        String log = "Merged " + branch + " into " + _head.getName() + ".";
        Commit commit = new Commit(getHeadCommit(),
            fromHead, _head.getName(), log, _stage, _removedFiles);
        makeCommit(commit);
        if (_conflicts) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * @param fileName file name.
     * @param from from commit.
     */
    public void conflict(String fileName, Commit from) {
        _conflicts = true;
        String merge = "<<<<<<< HEAD\r\n";
        File f = new File(fileName);
        if (f.exists()) {
            merge += Utils.readContentsAsString(f);
        }
        merge += "=======\r\n";
        if (from.getBlobs().containsKey(fileName)) {
            merge += from.getBlobs().get(fileName).getString();
        }
        merge += ">>>>>>>\r\n";
        Utils.writeContents(f, merge);
        add(fileName);
    }

    /**
     * checkout stage.
     * @param fileName file name
     * @param from from commit
     */
    public void checkStage(String fileName, Commit from) {
        File file = new File(fileName);
        var overwrite = from.getBlobs().get(fileName).getContents();
        Utils.writeContents(file, overwrite);
        add(fileName);
    }

    /**
     * @param fileName file name
     */
    public void warnUnchecked(String fileName) {
        if (_untrackedFiles.contains(fileName)) {
            System.out.println("There is an untracked file in the way;"
                + " delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    /**
     * map file names to actions / functions.
     * @param split split point commit
     * @param from from commit.
     * @return the map.
     */
    public HashMap<String, Runnable> map2Func(Commit split, Commit from) {
        HashMap<String, Runnable> funcMap = new HashMap<>();
        HashMap<String, Blob> sBLobs = split.getBlobs();
        HashMap<String, Blob> fBlobs = from.getBlobs();
        for (String fileName : sBLobs.keySet()) {
            File f = new File(fileName);
            if (!fBlobs.containsKey(fileName)) {
                if (f.exists()) {
                    var fContents = Utils.readContents(f);
                    var sContents = sBLobs.get(fileName).getContents();
                    if (Arrays.equals(fContents, sContents)) {
                        funcMap.put(fileName, () -> remove(fileName));
                    } else {
                        funcMap.put(fileName, () -> conflict(fileName, from));
                    }
                    warnUnchecked(fileName);
                }
            } else {
                if (f.exists()) {
                    var contents = Utils.readContents(f);
                    var fContents = fBlobs.get(fileName).getContents();
                    var sContents = sBLobs.get(fileName).getContents();
                    if (!Arrays.equals(fContents, sContents)
                        && Arrays.equals(sContents, contents)) {
                        funcMap.put(fileName, () -> checkStage(fileName, from));
                        warnUnchecked(fileName);
                    } else if (!Arrays.equals(contents, sContents)
                            && !Arrays.equals(fContents, sContents)
                            && !Arrays.equals(contents, fContents)) {
                        funcMap.put(fileName, () -> conflict(fileName, from));
                        warnUnchecked(fileName);
                    }
                } else {
                    var fContents = fBlobs.get(fileName).getContents();
                    var sContents = sBLobs.get(fileName).getContents();
                    if (!Arrays.equals(fContents, sContents)) {
                        funcMap.put(fileName, () -> conflict(fileName, from));
                        warnUnchecked(fileName);
                    }
                }
            }
        }
        for (String fileName : fBlobs.keySet()) {
            if (!sBLobs.containsKey(fileName)) {
                File file = new File(fileName);
                if (!file.exists()) {
                    funcMap.put(fileName, () -> checkStage(fileName, from));
                    warnUnchecked(fileName);
                } else {
                    var contents = Utils.readContents(file);
                    var fContents = fBlobs.get(fileName).getContents();
                    if (!Arrays.equals(contents, fContents)) {
                        funcMap.put(fileName, () -> conflict(fileName, from));
                        warnUnchecked(fileName);
                    }
                }
            }
        }
        return funcMap;
    }
}
