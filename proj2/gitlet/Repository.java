package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;
import static gitlet.ErrorUtils.*;
import static gitlet.MyUtils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Aron
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory.(store blobs) */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** Points to the current branch. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** Store the staging area. */
    public static final File INDEX = join(GITLET_DIR, "index");

    public static final String MASTER = "master";

    private static final int LONG_ID_LENGTH = 40;


    public static void init() {
        checkRepoExists();
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();

        writeHead(MASTER);
        var stage = new StagingArea();
        stage.save();
        var initialCommit = new Commit();

        writeBranch(MASTER, initialCommit.getCommitId());
        initialCommit.save();
    }

    public static void add(String fileName) {
        checkInitialized();
        checkFileExists(fileName);

        var content = readContentsAsString(join(CWD, fileName));
        var blob = new Blob(fileName, content);

        var commit = getCurrentCommit();

        var blobId = blob.getId();
        var stage = getStagingArea();
        var oldBlobId = commit.getBlobId(fileName);
        if (oldBlobId.equals(blobId) || stage.contains(fileName)) {
            stage.removeFromAdded(fileName);
        } else {
            blob.save();
            stage.add(fileName, blobId);
        }

        stage.save();

    }

    public static void commit(String message) {
        checkInitialized();
        if (message.isEmpty()) {
            exitWithError("Please enter a commit message.");
        }
        var stage = getStagingArea();
        if (stage.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }

        var parCommit = getCurrentCommit();

        var blobs = new HashMap<>(parCommit.getBlobs());
        stage.applyChangesTo(blobs);

        var commit = new Commit(message, parCommit.getCommitId(), blobs);
        writeBranch(readHead(), commit.getCommitId());
        commit.save();
        stage.clear();
        stage.save();
    }

    public static void rm(String fileName) {
        checkInitialized();

        var stage = getStagingArea();
        var commit = getCurrentCommit();
        var blobId = commit.getBlobId(fileName);
        if (blobId == null && !stage.contains(fileName)) {
            exitWithError("No reason to remove the file.");
        } else if (blobId != null) {
            stage.rm(fileName);
            restrictedDelete(join(CWD, fileName));
        } else {
            stage.removeFromAdded(fileName);
        }
        stage.save();
    }

    public static void log() {
        checkInitialized();
        var commit = getCurrentCommit();
        while (commit != null) {
            System.out.println(commit);
            var parentId = commit.getFirstParentId();
            if (parentId == null) {
                break;
            }
            commit = readCommit(parentId);
        }
    }

    public static void globalLog() {
        checkInitialized();
        var ids = plainFilenamesIn(Commit.COMMITS_DIR);
        for (var id : ids) {
            System.out.println(readCommit(id));
        }
    }

    public static void find(String message) {
        checkInitialized();
        var ids = plainFilenamesIn(Commit.COMMITS_DIR);
        var found = false;
        for (var id : ids) {
            var commit = readCommit(id);
            if (commit.getMessage().equals(message)) {
                System.out.println(id);
                found = true;
            }
        }
        if (!found) {
            exitWithError("Found no commit with that message.");
        }
    }

    public static void status() {
        checkInitialized();

        System.out.println("=== Branches ===");
        var branchNames = plainFilenamesIn(HEADS_DIR);
        var head = readHead();
        for (var name : branchNames) {
            if (name.equals(head)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        var stage = getStagingArea();
        var files = stage.getAdded();
        for (var name : files) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        var removed = stage.getRemoved();
        for (var name : removed) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkout(String commitId, String fileName) {
        checkInitialized();

        var cid = commitId;
        if (commitId.length() < LONG_ID_LENGTH) {
            // maybe performance issue
            cid = getLongId(commitId);
        }

        if (join(Commit.COMMITS_DIR, cid).exists()) {
            var commit = readCommit(cid);
            var blobId = commit.getBlobId(fileName);
            if (blobId == null) {
                exitWithError("File does not exist in that commit.");
            }
            var blob = readBlob(blobId);
            writeContents(join(CWD, fileName), blob.getContent());
        } else {
            exitWithError("No commit with that id exists.");
        }
    }

    public static void checkoutBranch(String branchName) {
        checkInitialized();

        var curBranch = readHead();
        if (curBranch.equals(branchName)) {
            exitWithError("No need to checkout the current branch.");
        }
        if (!join(HEADS_DIR, branchName).exists()) {
            exitWithError("No such branch exists.");
        }

        var commit = readCommit(getBranchId(branchName));
        var branchBlobs = commit.getBlobs();

        deleteFilesNotIn(branchBlobs);
        writeBlobsToCWD(branchBlobs);

        var stage = getStagingArea();
        stage.clear();
        stage.save();
        writeHead(branchName);
    }

    public static void branch(String branchName) {
        checkInitialized();
        if (join(HEADS_DIR, branchName).exists()) {
            exitWithError("A branch with that name already exists.");
        }
        var commit = getCurrentCommit();
        writeBranch(branchName, commit.getCommitId());
    }

    public static void rmBranch(String fileName) {
        checkInitialized();
        if (!join(HEADS_DIR, fileName).exists()) {
            exitWithError("A branch with that name does not exist.");
        }
        if (readHead().equals(fileName)) {
            exitWithError("Cannot remove the current branch.");
        }
        join(HEADS_DIR, fileName).delete();
    }

    public static void reset(String commitId) {
        checkInitialized();
        var cid = commitId;
        if (commitId.length() < LONG_ID_LENGTH) {
            cid = getLongId(commitId);
        }
        if (!join(Commit.COMMITS_DIR, cid).exists()) {
            exitWithError("No commit with that id exists.");
        }

        var commit = readCommit(cid);
        var blobs = commit.getBlobs();

        deleteFilesNotIn(blobs);
        writeBlobsToCWD(blobs);

        var stage = getStagingArea();
        stage.clear();
        stage.save();
        writeBranch(readHead(), commitId);
    }

    public static void merge(String branchName) {
        var curBranch = readHead();
        if (!join(HEADS_DIR, branchName).exists()) {
            exitWithError("A branch with that name does not exist.");
        }
        if (curBranch.equals(branchName)) {
            exitWithError("Cannot merge a branch with itself.");
        }

        var curCommit = getCurrentCommit();
        var givenCommit = readCommit(getBranchId(branchName));
        var splitPoint = findSplitPoint(curCommit, givenCommit);
        var splitId = splitPoint.getCommitId();
        var curId = curCommit.getCommitId();
        var givenId = givenCommit.getCommitId();
        if (splitId.equals(givenId)) {
            exitWithError("Given branch is an ancestor of the current branch.");
        }
        if (splitId.equals(curId)) {
            writeBranch(curBranch, givenId);
            exitWithError("Current branch fast-forwarded.");
        }


        var givenBlobs = givenCommit.getBlobs();
        var curBlobs = curCommit.getBlobs();
        var splitBlobs = splitPoint.getBlobs();
        var stage = getStagingArea();
        var conflicted = false;
        for (var name : givenBlobs.keySet()) {
            var givenBlobId = givenBlobs.get(name);
            var splitBlobId = splitBlobs.get(name);
            var curBlobId = curBlobs.get(name);
            if (splitBlobId == null) {
                if (curBlobId == null) {
                    checkout(givenId, name);
                    stage.add(name, givenBlobId);
                } else if (curBlobId.equals(givenBlobId)) {
                    continue;
                } else { // file changed in both but differently
                    conflicted = true;
                    writeConflictedFile(name, curBlobId, givenBlobId);
                }
            } else if (splitBlobId.equals(curBlobId)) {
                if (splitBlobId.equals(givenBlobId)) {
                    continue;
                } else {
                    checkout(givenId, name);
                    stage.add(name, givenBlobId);
                }
            } else {
                if (splitBlobId.equals(givenBlobId)) {
                    continue;
                } else if (curBlobId.equals(givenBlobId)) {
                    continue;
                } else {
                    conflicted = true;
                    writeConflictedFile(name, curBlobId, givenBlobId);
                }
            }
        }

        for (var name : curBlobs.keySet()) {
            var curBlobId = curBlobs.get(name);
            var splitBlobId = splitBlobs.get(name);
            var givenBlobId = givenBlobs.get(name);
            if (splitBlobId == null) {
                if (givenBlobId == null) {
                    continue;
                } else {
                    if (curBlobId == null) {
                        checkout(givenId, name);
                        stage.add(name, givenBlobId);
                    } else if (curBlobId.equals(givenBlobId)) {
                        continue;
                    } else {
                        conflicted = true;
                        writeConflictedFile(name, curBlobId, givenBlobId);
                    }
                }
            } else if (splitBlobId.equals(curBlobId)) {
                if (splitBlobId.equals(givenBlobId)) {
                    continue;
                } else {
                    checkout(givenId, name);
                    stage.add(name, givenBlobId);
                }
            } else {
                if (splitBlobId.equals(givenBlobId)) {
                    continue;
                } else if (curBlobId.equals(givenBlobId)) {
                    continue;
                } else {
                    conflicted = true;
                    writeConflictedFile(name, curBlobId, givenBlobId);
                }
            }
        }

        if (conflicted) {
            exitWithError("Encountered a merge conflict.");
        }

        stage.save();
        commit("Merged " + curBranch + " with " + branchName + ".");

    }
}
