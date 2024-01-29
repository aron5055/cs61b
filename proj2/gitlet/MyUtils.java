package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import static gitlet.ErrorUtils.exitWithError;
import static gitlet.Utils.*;
public class MyUtils {
    public static void saveObjects(File dir, String id, Serializable obj) {
        var subDir = join(dir, id.substring(0, 2));
        var file = join(subDir, id.substring(2));
        if (!subDir.exists()) {
            subDir.mkdir();
        }
        writeObject(file, obj);
    }


    public static String readHead() {
        return readContentsAsString(Repository.HEAD)
                .substring(16)
                .trim();
    }

    public static StagingArea getStagingArea() {
        return readObject(Repository.INDEX, StagingArea.class);
    }

    public static String getBranchId(String branchName) {
        return readContentsAsString(join(Repository.HEADS_DIR, branchName))
                .trim();
    }

    public static Commit readCommit(String id) {
        return readObject(join(Commit.COMMITS_DIR, id), Commit.class);
    }

    public static Commit getCurrentCommit() {
        var id = getBranchId(readHead());
        return readCommit(id);
    }

    public static void writeBranch(String branchName, String commitId) {
        writeContents(join(Repository.HEADS_DIR, branchName), commitId + "\n");
    }

    public static void writeHead(String branchName) {
        writeContents(Repository.HEAD, "ref: refs/heads/" + branchName + "\n");
    }

    public static File objectFile(String id) {
        return join(Repository.OBJECTS_DIR, id.substring(0, 2), id.substring(2));
    }

    public static Blob readBlob(String id) {
        return readObject(objectFile(id), Blob.class);
    }

    public static String getLongId(String shortId) {
        var files = plainFilenamesIn(Repository.OBJECTS_DIR);
        for (var name : files) {
            if (name.startsWith(shortId)) {
                return name;
            }
        }
        // it's never be a valid id
        return "notFound";
    }

    public static void writeBlobsToCWD(Map<String, String> blobs) {
        for (var name : blobs.keySet()) {
            var blobId = blobs.get(name);
            var blob = readBlob(blobId);
            writeContents(join(Repository.CWD, name), blob.getContent());
        }
    }

    public static void deleteFilesNotIn(Map<String, String> blobs) {
        var stage = getStagingArea();
        var curBlobs = getCurrentCommit().getBlobs();
        var curFiles = plainFilenamesIn(Repository.CWD);
        for (var name : curFiles) {
            if (!curBlobs.containsKey(name) && stage.contains(name)) {
                exitWithError("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            }
            if (curBlobs.containsKey(name) && !blobs.containsKey(name)) {
                restrictedDelete(join(Repository.CWD, name));
            }
        }
    }

    public static Commit findSplitPoint(Commit commit1, Commit commit2) {
        var commit1Ancestors = commit1.getAncestors();
        var commit2Ancestors = commit2.getAncestors();
        for (var id : commit1Ancestors) {
            if (commit2Ancestors.contains(id)) {
                return readCommit(id);
            }
        }
        return null;
    }

    public static void writeConflictedFile(String name, String blobId1, String blobId2) {
        var content1 = readBlob(blobId1).getContent();
        var content2 = readBlob(blobId2).getContent();
        var content = "<<<<<<< HEAD\n" + content1 + "=======\n" + content2 + ">>>>>>>\n";
        writeContents(join(Repository.CWD, name), content);

    }

    public static void main(String[] args) {
    }
}
