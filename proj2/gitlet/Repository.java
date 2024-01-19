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

        writeContents(HEAD, "ref: refs/heads/master\n");
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
        checkRepoExists();
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
            commit = readObject(objPath(parentId), Commit.class);
        }
    }

}
