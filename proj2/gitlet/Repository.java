package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static gitlet.Utils.*;
import static gitlet.ErrorUtils.*;
import static gitlet.MyUtils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory.(store blobs) */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** Points to the current branch. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** Store the staging area. */
    public static final File INDEX = join(GITLET_DIR, "index");

    public static final String MASTER = "master";


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
        if (oldBlobId != null && oldBlobId.equals(blobId)) {
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
            var file = join(CWD, fileName);
            restrictedDelete(file);
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
        if (commitId.length() == 6) {
            // maybe performance issue
            var ids = plainFilenamesIn(Commit.COMMITS_DIR);
            for (var id : ids) {
                if (id.startsWith(commitId)) {
                    cid = id;
                    break;
                }
            }
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

        var stage = getStagingArea();
        var curCommit = getCurrentCommit();
        var curBlobs = curCommit.getBlobs();
        var curFiles = plainFilenamesIn(CWD);
        var commit = readCommit(getBranchId(branchName));
        var branchBlobs = commit.getBlobs();

        for (var name : curFiles) {
            if (!curBlobs.containsKey(name) && stage.contains(name)) {
                exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
            if (curBlobs.containsKey(name) && !branchBlobs.containsKey(name)) {
                restrictedDelete(join(CWD, name));
            }
        }

        for (var name : branchBlobs.keySet()) {
            var blobId = branchBlobs.get(name);
            var blob = readBlob(blobId);
            writeContents(join(CWD, name), blob.getContent());
        }

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
}
