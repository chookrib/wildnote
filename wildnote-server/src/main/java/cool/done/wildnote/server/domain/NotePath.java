package cool.done.wildnote.server.domain;

/**
 * 笔记节点
 */
public class NotePath {

    private int level;
    private String relPath;
    private String absPath;
    private String name;
    private boolean directory;
    private Long creationTime;
    private long lastModifiedTime;

    public int getLevel() { return level; }

    public String getRelPath() {
        return relPath;
    }

    public String getAbsPath() {
        return absPath;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public long getCreationTime() { return creationTime; }

    public long getLastModifiedTime() { return lastModifiedTime; }

    public NotePath(int level, String relPath, String absPath, String name, boolean directory,
                    Long creationTime, long lastModifiedTime) {
        this.level = level;
        this.relPath = relPath;
        this.absPath = absPath;
        this.name = name;
        this.directory = directory;
        this.creationTime = creationTime;
        this.lastModifiedTime = lastModifiedTime;
    }
}
