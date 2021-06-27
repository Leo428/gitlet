package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * CommandLineTools.
 * @author Zheyuan Hu
 */
public class CommandLineTools implements Dumpable {
    /** serial ID. */
    private static final long serialVersionUID = -2343050904013249351L;
    /** repo. */
    private Repository _repo;
    /** map String to file location. */
    private final HashMap<String, Remote> _remoteMap;
    /** CWD. */
    public static final String CWD = System.getProperty("user.dir");
    /** CWD Path. */
    public static final File CWD_DIR = new File(CWD);
    /** Gitlet folder. */
    public static final File REPO_DIR = new File(CWD_DIR, ".gitlet");
    /** command file. */
    public static final File CLI_FILE = new File(REPO_DIR, "cliConf");
    /** commit folder. */
    public static final File COMMIT_DIR = Utils.join(REPO_DIR, "commits");
    /** staging area folder. */
    public static final File STAGE_DIR = Utils.join(REPO_DIR, "stage");
    /** untracked files folder. */
    public static final File UNTRACKED_DIR = Utils.join(REPO_DIR, "untracked");

    /**
     * Constructor. Initializes vars.
     * @throws IOException
     */
    public CommandLineTools() {
        if (CLI_FILE.exists()) {
            CommandLineTools temp =
                Utils.readObject(CLI_FILE, CommandLineTools.class);
            _repo = temp._repo;
            _remoteMap = temp._remoteMap;
        } else {
            _repo = new Repository();
            _remoteMap = new HashMap<>();
        }
    }

    /**
     * executes the command according to the args.
     * @param operands list of operands.
     * @throws IOException
     */
    public void execute(ArrayList<String> operands) throws IOException {
        if (operands.size() == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String commandStr = operands.remove(0);
        switch (commandStr) {
        case "init":
            initialize();
            break;
        case "add":
            _repo.add(operands.remove(0));
            break;
        case "rm":
            _repo.remove(operands.remove(0));
            break;
        case "commit":
            commit(operands);
            break;
        case "log":
            _repo.log();
            break;
        case "global-log":
            _repo.globalLog();
            break;
        case "find":
            _repo.find(operands.remove(0));
            break;
        case "status":
            _repo.status();
            break;
        case "checkout":
            checkout(operands);
            break;
        case "branch":
            _repo.createBranch(operands.remove(0));
            break;
        case "rm-branch":
            _repo.removeBranch(operands.remove(0));
            break;
        case "reset":
            _repo.reset(operands.remove(0));
            break;
        case "merge":
            _repo.merge(operands.remove(0));
            break;
        default:
            operands.add(0, commandStr);
            executeRemote(operands);
            break;
        }
    }

    /**
     * executes the command according to the args.
     * @param operands list of operands.
     * @throws IOException
     */
    public void executeRemote(ArrayList<String> operands) throws IOException {
        String commandStr = operands.remove(0);
        switch (commandStr) {
        case "add-remote":
            addRemote(operands.remove(0), operands.remove(0));
            break;
        case "rm-remote":
            removeRemote(operands.remove(0));
            break;
        case "push":
            push(operands.remove(0), operands.remove(0));
            break;
        case "fetch":
            fetch(operands.remove(0), operands.remove(0));
            break;
        case "pull":
            pull(operands.remove(0), operands.remove(0));
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
    }

    /**
     * initialize the command line tools.
     * @throws IOException
     */
    public void initialize() throws IOException {
        if (REPO_DIR.exists()) {
            System.out.println("A Gitlet version-control "
                + "system already exists in the current directory.");
        } else {
            REPO_DIR.mkdirs();
            COMMIT_DIR.mkdir();
            STAGE_DIR.mkdir();
            UNTRACKED_DIR.mkdir();
            _repo.initialize();
        }
    }

    /**
     * make commit using args.
     * @param operands args
     */
    public void commit(ArrayList<String> operands) {
        if (_repo.noChanges()) {
            System.out.println("No changes added to the commit.");
        } else if (operands.size() == 0 || operands.get(0).length() < 1) {
            System.out.println("Please enter a commit message.");
        } else {
            _repo.commit(operands.remove(0));
        }
    }

    /**
     * @param operands args
     */
    public void checkout(ArrayList<String> operands) {
        switch (operands.size()) {
        case 1:
            _repo.checkoutByBranch(operands.remove(0));
            break;
        case 2:
            if (operands.remove(0).equals("--")) {
                _repo.checkoutFileInCurrent(operands.remove(0));
            } else {
                exitWithMessage("Incorrect operands.");
            }
            break;
        case 3:
            String commitID = operands.remove(0);
            if (!operands.remove(0).equals("--")) {
                exitWithMessage("Incorrect operands.");
            } else {
                _repo.checkoutFileByID(operands.remove(0), commitID);
            }
            break;
        default:
            exitWithMessage("Incorrect operands.");
            break;
        }
    }

    /**
     * refresh the repo's untracked set.
     */
    public void refreshUntracked() {
        _repo.updateUntracked();
    }

    /**
     * get repo.
     * @return repo
     */
    public Repository getRepo() {
        return _repo;
    }

    /**
     * add remote.
     * @param name name of the remote.
     * @param folder folder location.
     */
    public void addRemote(String name, String folder) {
        if (_remoteMap.keySet().contains(name)) {
            exitWithMessage("A remote with that name already exists.");
        }
        File dir = new File(folder);
        Remote remote = new Remote(dir);
        if (dir.exists()) {
            remote.getCLI().getRepo().setRepoDir(new File(folder));
        }
        _remoteMap.put(name, remote);
    }

    /**
     * remove remote with the given name.
     * @param name remote name.
     */
    public void removeRemote(String name) {
        if (!_remoteMap.keySet().contains(name)) {
            exitWithMessage("A remote with that name does not exist.");
        }
        _remoteMap.remove(name);
    }

    /**
     * push.
     * @param remote remote name.
     * @param branch branch name.
     */
    public void push(String remote, String branch) {
        if (!_remoteMap.keySet().contains(remote)
            || !_remoteMap.get(remote).remoteExist()) {
            exitWithMessage("Remote directory not found.");
        }
        var rr = _remoteMap.get(remote);
        var r = rr.getCLI().getRepo();
        if (!r.getBranches().containsKey(branch)) {
            exitWithMessage("That remote does not have that branch.");
        }
        var rCommitPath = rr.getPath("commits");
        var rHeadID = r.getHeadCommit().getID();
        if (!_repo.hasCommit(rHeadID)) {
            exitWithMessage("Please pull down remote changes before pushing.");
        }
        _repo.getCommits().stream().filter(id -> {
            Commit c = _repo.getCommit(id);
            return _repo.getHeadBranch().getName().equals(c.getBranch())
                    && !r.getCommits().contains(id);
        }).forEach(id -> {
            r.copyCommit(_repo.getCommit(id), rCommitPath);
        });
        r.resetRemote(_repo.getHeadCommit().getID(), rCommitPath);
        saveRemoteCLI();
    }

    /**
     * fetch.
     * @param remote remote name.
     * @param branch branch name.
     */
    public void fetch(String remote, String branch) {
        if (!_remoteMap.keySet().contains(remote)
            || !_remoteMap.get(remote).remoteExist()) {
            exitWithMessage("Remote directory not found.");
        }
        var rr = _remoteMap.get(remote);
        var r = rr.getCLI().getRepo();
        if (!r.getBranches().containsKey(branch)) {
            exitWithMessage("That remote does not have that branch.");
        }
        var rCommitPath = rr.getPath("commits");
        r.getCommits().forEach(id -> {
            if (!_repo.hasCommit(id)) {
                var commit = r.getCommit(id);
                commit.setBranchName(remote + "/" + branch);
                _repo.copyCommit(commit, CommandLineTools.COMMIT_DIR);
            }
        });
        _repo.getBranches().get(remote + "/" + branch)
                .updateHead(r.getHeadCommit().getID());
    }

    /**
     * pull.
     * @param remote remote name.
     * @param branch branch name.
     */
    public void pull(String remote, String branch) {
        var rr = _remoteMap.get(remote);
        rr.loadCLI();
        var r = rr.getCLI().getRepo();
        fetch(remote, branch);
        _repo.merge(remote + "/" + branch);
    }

    /**
     * print out the message and exit with code 0.
     * @param msg messages
     */
    public void exitWithMessage(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    /**
     * save all clis when exit.
     */
    public void saveRemoteCLI() {
        _remoteMap.forEach((name, r) -> {
            if (r != null && r.getRepoPath().exists()) {
                Utils.writeObject(r.getPath("cliConf"), r.getCLI());
            }
        });
    }

    @Override
    public void dump() {
        System.out.println(_repo.getCommits().size());
    }
}
