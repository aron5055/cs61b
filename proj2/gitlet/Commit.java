package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Aron
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    private final Date time;
    private final ArrayList<String> parents;
    private final HashMap<String, String> blobs;
    private final String commitId;
    public static final SimpleDateFormat SDF =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.getDefault());

    public static final File COMMITS_DIR = join(Repository.OBJECTS_DIR, "commits");


    public Commit() {
        message = "initial commit";
        time = new Date(0);
        parents = new ArrayList<>();
        blobs = new HashMap<>();
        commitId = sha1(message, time.toString());
    }

    public Commit(String message, String parentId, HashMap<String, String> blobs) {
        this(message, parentId, blobs, null);
    }

    public Commit(String message, String parentId,
                  HashMap<String, String> blobs, String parent2Id) {
        this.message = message;
        time = new Date();
        parents = new ArrayList<>();
        parents.add(parentId);
        if (parent2Id != null) {
            parents.add(parent2Id);
        }
        this.blobs = blobs;
        commitId = sha1(message, time.toString(), serialize(parents), serialize(blobs));
    }

    public void addParent(String parentId) {
        parents.add(parentId);
    }

    public Optional<String> getBlobId(String name) {
        if (blobs == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(blobs.get(name));
    }

    public Map<String, String> getBlobs() {
        return Collections.unmodifiableMap(blobs);
    }

    public String getCommitId() {
        return commitId;
    }

    public String getMessage() {
        return message;
    }

    public String getFirstParentId() {
        if (parents.isEmpty()) {
            return null;
        }
        return parents.get(0);
    }

    public ArrayList<String> getAncestors() {
        var ancestors = new ArrayList<String>();
        var queue = new LinkedList<String>();
        queue.add(commitId);
        while (!queue.isEmpty()) {
            var curId = queue.poll();
            ancestors.add(curId);
            var curCommit = readCommit(curId);
            var parentId = curCommit.getFirstParentId();
            if (parentId != null) {
                queue.add(parentId);
            }
        }
        return ancestors;
    }

    public void save() {
        writeObject(join(COMMITS_DIR, commitId), this);
    }

    public String toString() {
        var s = "";
        s += "===\n";
        s += "commit " + commitId + "\n";
        if (parents.size() == 2) {
            s += "Merge: " + parents.get(0).substring(0, 7) + " "
                    + parents.get(1).substring(0, 7) + "\n";
        }
        s += "Date: " + SDF.format(time) + "\n";
        s += message + "\n";
        return s;
    }
}
