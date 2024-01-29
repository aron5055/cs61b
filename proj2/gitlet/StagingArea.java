package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    public void delete(String name) {
        added.remove(name);
        removed.remove(name);
    }

    public boolean containsInRemoved(String name) {
        return removed.contains(name);
    }

    public void applyChangesTo(HashMap<String, String> blobs) {
        blobs.putAll(added);
        for (var name : removed) {
            blobs.remove(name);
        }
    }

    public List<String> getAdded() {
        var list =  new ArrayList<>(added.keySet());
        list.sort(String::compareTo);
        return list;
    }

    public List<String> getRemoved() {
        var list = new ArrayList<>(removed);
        list.sort(String::compareTo);
        return list;
    }

    public void clear() {
        added.clear();
        removed.clear();
    }

    public void save() {
        writeObject(Repository.INDEX, this);
    }
}
