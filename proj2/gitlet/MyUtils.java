package gitlet;

import java.io.File;
import java.io.Serializable;

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

    public static void main(String[] args) {
    }
}
