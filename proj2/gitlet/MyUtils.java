package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

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

    public static String getBranch(String branchName) {
        return readContentsAsString(join(Repository.HEADS_DIR, branchName))
                .trim();
    }

    public static File objPath(String id) {
        var subDir = join(Repository.OBJECTS_DIR, id.substring(0, 2));
        return join(subDir, id.substring(2));
    }

    public static Commit getCurrentCommit() {
        var id = getBranch(readHead());
        return readObject(objPath(id), Commit.class);
    }

    public static void writeBranch(String branchName, String commitId) {
        writeContents(join(Repository.HEADS_DIR, branchName), commitId + "\n");
    }

    public static void main(String[] args) {
    }
}
