package cool.done.wildnote.domain;

/**
 * 笔记目录
 */
public class NoteIndex {

    private int level;
    private String path;
    private String name;
    private boolean directory;

    public int getLeval() { return level; }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public NoteIndex(int level, String path, String name, boolean directory) {
        this.level = level;
        this.path = path;
        this.name = name;
        this.directory = directory;
    }
}
