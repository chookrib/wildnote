package cool.done.wildnotesvc.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;

/**
 * 笔记Service
 */
@Service
public class NoteService {

    @Value("${wildnote.path}")
    private String notePath;

    @Autowired
    private IFileRepository fileRepository;

    /**
     * 读取笔记列表
     */
    public ArrayList<NoteIndex> getNotes() throws IOException {
        if (StringUtils.isEmpty(notePath)) {
            throw new ValidationException("未配置笔记文件夹路径");
        }

        ArrayList<NoteIndex> noteIndexes = new ArrayList<>();
        int notePathLevel = notePath.replace("\\", "/").split("/").length;

        ArrayList<File> files = fileRepository.getFiles(notePath);
        for (File file : files) {
            int level = file.getPath().replace("\\", "/").split("/").length - notePathLevel - 1;

            BasicFileAttributes attrs = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            FileTime creationTime = attrs.creationTime();

            if (file.isFile()) {
                noteIndexes.add(new NoteIndex(level, file.getPath(), file.getName(), false, creationTime.toMillis(), file.lastModified()));
            }
            if (file.isDirectory()) {
                noteIndexes.add(new NoteIndex(level, file.getPath(), file.getName(), true, creationTime.toMillis(), file.lastModified()));
            }
        }

        return noteIndexes;
    }

    /**
     * 读取笔记
     */
    public String getNote(String path) throws IOException {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止读取笔记文件夹路径以外的文件");
        return fileRepository.getFile(path);
    }

    /**
     * 保存笔记
     */
    public void saveNote(String path, String content) throws IOException {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止保存笔记文件夹路径以外的文件");
        fileRepository.saveFile(path, content);
    }

    /**
     * 创建笔记
     */
    public void createNote(String path) throws IOException {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止在笔记文件夹路径以外创建文件");
        fileRepository.createFile(path);
    }

    /**
     * 删除笔记
     */
    public void deleteNote(String path) throws IOException {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止删除笔记文件夹路径以外的文件");
        fileRepository.deleteFile(path);
    }
}
