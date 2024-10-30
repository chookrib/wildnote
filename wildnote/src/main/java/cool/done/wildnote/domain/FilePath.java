package cool.done.wildnote.domain;

import java.util.ArrayList;

/**
 * 路径
 */
public class FilePath {

    private String path;
    private String name;
    private boolean directory;
    private ArrayList<FilePath> subPaths;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public ArrayList<FilePath> getSubPaths() {
        return subPaths;
    }

    public FilePath(String path, String name, boolean directory, ArrayList<FilePath> subPaths) {
        this.path = path;
        this.name = name;
        this.directory = directory;
        this.subPaths = subPaths;
    }
}
