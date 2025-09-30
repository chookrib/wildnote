package cool.done.wildnote.server.domain;

/**
 * 笔记树节点
 */
public class NoteTreeNode {

    private String relPath;
    private String absPath;
    private String name;
    private boolean directory;
    private Long creationTime;
    private long lastModifiedTime;
    private int level;

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

    public Long getCreationTime() { return creationTime; }

    public long getLastModifiedTime() { return lastModifiedTime; }

    public int getLevel() { return level; }

    private NoteTreeNode() { }

    /**
     * 创建
     */
    public static NoteTreeNode create(String relPath, String absPath, String name, boolean directory,
                                      Long creationTime, long lastModifiedTime, int level) {
        NoteTreeNode note = new NoteTreeNode();
        note.relPath = relPath;
        note.absPath = absPath;
        note.name = name;
        note.directory = directory;
        note.creationTime = creationTime;
        note.lastModifiedTime = lastModifiedTime;
        note.level = level;
        return note;
    }
}
