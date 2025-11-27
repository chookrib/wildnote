package cool.done.wildnote.server.application;

import cool.done.wildnote.server.domain.NoteTreeNode;
import cool.done.wildnote.server.utility.ValueUtility;

import java.time.LocalDateTime;

/**
 * 笔记树节点 DTO
 */
public class NoteTreeNodeDto {

    // private String relPath;
    // private String absPath;
    private String path;
    private String name;
    private boolean directory;
    private LocalDateTime creationTime;
    private LocalDateTime lastModifiedTime;
    private int level;

    // public String getRelPath() { return relPath; }

    // public String getAbsPath() { return absPath; }

    public String getPath() { return path; }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public LocalDateTime getCreationTime() { return creationTime; }

    public LocalDateTime getLastModifiedTime() { return lastModifiedTime; }

    public int getLevel() { return level; }

    private NoteTreeNodeDto() { }

    /**
     * 创建
     */
    public static NoteTreeNodeDto fromEntity(NoteTreeNode entity) {
        NoteTreeNodeDto dto = new NoteTreeNodeDto();
        //dto.relPath = entity.getRelPath();
        //dto.absPath = entity.getAbsPath();
        dto.path = entity.getRelPath();
        dto.name = entity.getName();
        dto.directory = entity.isDirectory();
        dto.creationTime = ValueUtility.toLocalDateTime(entity.getCreationTime());
        dto.lastModifiedTime = ValueUtility.toLocalDateTime(entity.getLastModifiedTime());
        dto.level = entity.getLevel();
        return dto;
    }
}
