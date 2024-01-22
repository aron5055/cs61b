package gitlet;

import java.io.Serializable;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

public class Blob implements Serializable {
    private String id;
    private String name;
    private String content;

    public Blob(String name, String content) {
        this.name = name;
        this.content = content;
        id = sha1(name, content);
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void save() {
        saveObjects(Repository.OBJECTS_DIR, id, this);
    }
}
