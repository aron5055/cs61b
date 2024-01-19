package gitlet;

// TODO: any imports you need here

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
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


    /* TODO: fill in the rest of this class. */
    public Commit() {
        message = "initial commit";
        time = new Date(0);
        parents = null;
        blobs = null;
        commitId = sha1(message, time.toString());
    }

    public Commit(String message, String parentId, HashMap<String, String> blobs) {
        this.message = message;
        time = new Date();
        parents = new ArrayList<>();
        parents.add(parentId);
        this.blobs = blobs;
        commitId = sha1(message, time.toString(), parents, blobs);
    }

    public void addParent(String parentId) {
        parents.add(parentId);
    }

    public String getBlobId(String name) {
        if (blobs == null) {
            return null;
        }
        return blobs.get(name);
    }

    public Map<String, String> getBlobs() {
        return Collections.unmodifiableMap(blobs);
    }

    public String getCommitId() {
        return commitId;
    }

    public String getFirstParentId() {
        if (parents == null) {
            return null;
        }
        return parents.get(0);
    }

    public void save() {
        saveObjects(Repository.OBJECTS_DIR, commitId, this);
    }

    public String toString() {
        return "===\n"
                + "commit " + this.commitId + "\n"
                + "Date: " + this.time.toString() + "\n"
                + this.message;
    }
}
