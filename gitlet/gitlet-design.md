# Gitlet Design Document

**Name**: Zheyuan Hu

## Classes and Data Structures

### CLI Class
- This class is the instance of the command line tools for Gitlet.
- It stores some information locally on the disk, such as information about the repo and the remote.
- It processes commands and executes functions accordingly. It throws exceptions when required.

### Repo Class
- This class contains information about the repo. Should be initialized when Gitlet is first initialized.
- It contains information about the head branch, all branches, the stage (staging areas for uncommitted files.)

### Stage Class
- This class contains all the staged files under the directory.
- Can add, rewrite, or remove files in this stage area.

### Commit Class
- This class contains: 
    - log messages
    - reference to a branch
    - reference to the parent branch
    - a timestamp
    - a unique Hash code.

### Branch Class
- This class contains information such as:
    - the name of the branch
    - branch head
    - a set of tracked files in this branch

### Blob Class
- This class contains a file's content and its file name.
    - It is referred to using the Hash code of the file.

### GitException
- This class contains all the defined exceptions for Gitlet.
- All the exceptions are according to the specs.

## Algorithms
### The way how a commit works
- compare files in the staged area with the files in the current commit
- additional files or updated files that are staged are first saved as a new blob, then added to the new commit
- same files from the parent commit are just included using their references.
- files that are staged for removal become untracked in the new commit.
- The staging area is cleared after a new commit.
- After each commit, the new commit is added as a new node in the commit tree.
- After each commit, the commit just made becomes the current commit, which is the head.
- The hash ID for each commit references its log message, parent reference, commit timestamp, all of its files.
    - In detail this means create a list of some sort, put all of the above into it, then hash that list.

### The way how rm works
- Unstage a file if it is currently staged for addition.
- Stage it for removal if it is currently in the commit if it has not been done manually.
    - it has to be tracked in the current commit.

### Repo
- The Repo class uses:
    - a HashSet to store all commits.
    - a HashSet to store all untracked files.
    - a HashMap to store all branches.

### Stage
- The stage class uses a HashMap to store staged files. The keys are filenames of the files while the values are the objects representation of each file's contents. 

### Commit
- The commit class uses a HashMap to store all the blobs to represent all the files in this commit.

### Branch
- The branch class uses a HashSet to track all of its tracked files. The HashSet takes in strings.

## Persistence
### Files in .gitlet folder
- blobs
- commits
- branches
