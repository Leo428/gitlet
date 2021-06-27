package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * @author Zheyuan Hu
 */
public class Remote implements Serializable {
    /** serial ID. */
    private static final long serialVersionUID = -6507518412566652100L;
    /** GitLet repo directory for remote. */
    private File _repoDir;
    /** commit folder. */
    private File _commitDir;
    /** command file. */
    private File _cliFile;
    /** cli. */
    private CommandLineTools _cli;

    /**
     * constructor.
     * @param dir remote directory.
     */
    public Remote(File dir) {
        _repoDir = dir;
        _commitDir = Utils.join(_repoDir, "commits");
        _cliFile = Utils.join(_repoDir, "cliConf");
    }

    /**
     * @return exist or not.
     */
    public boolean remoteExist() {
        return _repoDir.exists();
    }

    /**
     * return the path in remote repo.
     * @param folder folder
     * @return path.
     */
    public File getPath(String folder) {
        return Utils.join(_repoDir, folder);
    }

    /**
     * @return repo dir
     */
    public File getRepoPath() {
        return _repoDir;
    }

    /**
     * load cli.
     */
    public void loadCLI() {
        _cli = Utils.readObject(_cliFile, CommandLineTools.class);
    }

    /**
     * @return cli.
     */
    public CommandLineTools getCLI() {
        if (_cli == null) {
            loadCLI();
        }
        return _cli;
    }

    /**
     * read a branch from file, give it a new name.
     * @param path path
     * @param name name
     * @return
     */
    public Branch readBranch(File path, String name) {
        var branch = Utils.readObject(path, Branch.class);
        branch.setName(name);
        return branch;
    }
}
