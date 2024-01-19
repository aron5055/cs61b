package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    private final HashMap<String, String> added;
    private final HashSet<String> removed;

    public StagingArea() {
        added = new HashMap<>();
        removed = new HashSet<>();
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty();
    }

    public boolean contains(String name) {
        return added.containsKey(name) || removed.contains(name);
    }

    public void add(String name, String id) {
        added.put(name, id);
    }

    public void rm(String name) {
        added.remove(name);
        removed.add(name);
    }

    public void removeFromAdded(String name) {
        added.remove(name);
    }

    public void applyChangesTo(HashMap<String, String> blobs) {
        blobs.putAll(added);
        for (var name : removed) {
            blobs.remove(name);
        }
    }

    public void clear() {
        added.clear();
        removed.clear();
    }

    public void save() {
        writeObject(Repository.INDEX, this);
    }
}
