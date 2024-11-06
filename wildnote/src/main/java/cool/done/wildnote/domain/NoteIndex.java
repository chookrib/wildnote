package cool.done.wildnote.domain;

import java.util.Date;

/**
 * 笔记目录
 */
public class NoteIndex {

    private int level;
    private String path;
    private String name;
    private boolean directory;
    private long creationTime;
    private long lastModifiedTime;

    public int getLevel() { return level; }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public long getCreationTime() { return creationTime; }

    public long getLastModifiedTime() { return lastModifiedTime; }

    public NoteIndex(int level, String path, String name, boolean directory, long creationTime, long lastModifiedTime) {
        this.level = level;
        this.path = path;
        this.name = name;
        this.directory = directory;
        this.creationTime = creationTime;
        this.lastModifiedTime = lastModifiedTime;
    }
}
