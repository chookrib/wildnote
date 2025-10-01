package cool.done.wildnote.server.application;

import cool.done.wildnote.server.domain.NoteTreeNode;
import cool.done.wildnote.server.utility.ValueUtility;
import io.methvin.watcher.DirectoryWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 笔记资源管理 Service
 */
@Component
public class NoteExploreService {

    private static final Logger logger = LoggerFactory.getLogger(NoteExploreService.class);

    private final String noteRootAbsPath;
    private final String[] noteExtensions;

    private DirectoryWatcher watcher = null;

    // 存储笔记树的 Map，key=笔记文件绝对路径，value=笔记树节点对象
    private Map<String, NoteTreeNode> noteMap = new ConcurrentHashMap<>();

    private final NoteRemindService noteRemindService;
    private final ExtraLogService extraLogService;

    public NoteExploreService(@Value("${wildnote.note-root-path}") String noteRootPath,
                              @Value("${wildnote.note-extensions}") String noteExtensions,
                              NoteRemindService noteRemindService,
                              ExtraLogService extraLogService
    ) {
        if (ValueUtility.isBlank(noteRootPath)) {
            throw new ApplicationException("未配置笔记根路径");
        }
        Path path;
        try {
            // toRealPath 会解析符号链接，并返回规范化后的绝对路径，同时会检查路径是否真实存在。如果路径不存在或无权限，会抛出异常
            path = Path.of(noteRootPath).toRealPath();
        } catch (IOException e) {
            throw new ApplicationException(String.format("笔记根路径非法 %s", e.getMessage()));
        }
        //if (!Files.exists(path)) {
        //    throw new ApplicationException("笔记根路径不存在");
        //}
        if (!Files.isDirectory(path)) {
            throw new ApplicationException("笔记根路径必须为文件夹");
        }
        this.noteRootAbsPath = path.toString();
        this.noteExtensions = noteExtensions.split(",");

        this.noteRemindService = noteRemindService;
        this.extraLogService = extraLogService;
    }

    /**
     * 应用启动后运行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        reloadNote();
        startWatch();
    }

    /**
     * 获取笔记根路径绝对地址
     */
    public String getNoteRootAbsPath() {
        return noteRootAbsPath;
    }

    /**
     * 取笔记路树节点 Map
     */
    public Map<String, NoteTreeNode> getNoteMap() {
        return noteMap;
    }

    /**
     * 根据笔记文件扩展名，判断是否是有效的笔记文件
     */
    private boolean isNoteFile(String fileName) {
        return Arrays.stream(noteExtensions).anyMatch(
                ext -> fileName.toLowerCase().endsWith(ext.toLowerCase())
        );
    }

    /**
     * 根据笔记扩展名，判断是否解析笔记文件中的 cron
     */
    private boolean isCronNoteFile(String fileName) {
        return Arrays.stream(noteExtensions).anyMatch(
                ext -> fileName.toLowerCase().endsWith(".cron" + ext.toLowerCase())
        );
    }

    /**
     * 重新加载所有笔记
     */
    public void reloadNote() {
        noteMap.clear();
        noteRemindService.clear();

        ArrayList<File> files = new ArrayList<>();
        try {
            Files.walkFileTree(Path.of(noteRootAbsPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    files.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(Path.of(noteRootAbsPath))) {    // 排队根路径
                        files.add(dir.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            extraLogService.logNoteInfo(String.format("加载所有笔记成功: %s", noteRootAbsPath), logger);
        } catch (IOException e) {
            extraLogService.logNoteError(String.format("加载所有笔记异常: %s %s", noteRootAbsPath, e.getMessage()), logger);
        }

        //files.sort((file1, file2) -> {
        //    if (file1.isDirectory() && file2.isFile())
        //        return -1;
        //    if (file1.isFile() && file2.isDirectory())
        //        return 1;
        //    return file1.getName().compareTo(file2.getName());
        //});

        for (File file : files) {
            addNote(file);
        }
    }

    /**
     * 添加笔记
     */
    private void addNote(File file) {
        // 如是文件判断是否为笔记文件，如果是目录则直接添加
        if (file.isFile() && !isNoteFile(file.getName())) {
            return;
        }

        String absPath = file.toPath().normalize().toAbsolutePath().toString();     // D:\xxx\log\log.log
        String relPath = absPath.substring(noteRootAbsPath.length());               // log\log.log

        int level = relPath.split("\\\\|/").length - 2;

        Long creationTime = null;
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            creationTime = attrs.creationTime().toMillis();
        } catch (IOException e) {
            extraLogService.logNoteError(String.format("获取笔记文件创建时间属性异常: %s", absPath), logger);
        }

        NoteTreeNode note = NoteTreeNode.create(
                relPath,
                absPath,
                file.getName(),
                file.isDirectory(),
                creationTime,
                file.lastModified(),
                level
        );

        noteMap.put(file.getPath(), note);
        if (!note.isDirectory() && isCronNoteFile(note.getName())) {
            processCron(file);
        }
    }

    /**
     * 处理笔记文件中的提醒
     */
    private void processCron(File file) {
        // 取出相对路径
        String path = file.getPath().substring(noteRootAbsPath.length());
        try (var lines = Files.lines(file.toPath())) {
            // cron = lines.findFirst().orElse("");
            int lineNumber = 0;
            for (String line : (Iterable<String>) lines::iterator) {
                lineNumber++;
                if (!line.startsWith("> cron")) {
                    continue;
                }

                String[] parts = line.split("\\|");
                String cronExpression = parts.length >= 1 ? parts[1].trim() : "";
                String description = parts.length >= 2 ? parts[2].trim() : "";
                noteRemindService.addCron(path, lineNumber, cronExpression, description);
            }
        } catch (IOException e) {
            extraLogService.logNoteError(String.format("解析笔记cron失败: %s %s", path, e.getMessage()), logger);
        }
    }

    /**
     * 移除笔记
     */
    private void dropNote(File file) {
        NoteTreeNode note = noteMap.get(file.getPath());
        if (note == null) {
            return;
        }

        noteMap.remove(file.getPath());

        // 当文件已被删除，file.isFile() 会返回 false，故判断条件中不能用 file.isFile() 来判断
        if (!note.isDirectory() && isCronNoteFile(note.getName())) {
            noteRemindService.dropCron(
                    file.getPath().substring(noteRootAbsPath.length())  // 需要与add时一致
            );
        }
    }

    /**
     * 拼接路径并返回绝对路径
     */
    public Path combineAbsPath(String relPath) {
        Path path = Path.of(noteRootAbsPath, relPath);
        String absPath = path.normalize().toAbsolutePath().toString();
        if (!absPath.startsWith(noteRootAbsPath)) {
            throw new ApplicationException("禁止访问笔记根路径以外的文件");
        }
        return path;
    }

    /**
     * 获取笔记内容
     */
    public String getNote(String relPath) {
        try {
            return Files.readString(combineAbsPath(relPath));
        } catch (IOException e) {
            throw new ApplicationException(String.format("读取笔记内容异常: %s", e.getMessage()));
        }
    }

    /**
     * 保存笔记内容
     */
    public void saveNote(String relPath, String content) {
        try {
            Files.writeString(combineAbsPath(relPath), content);
        } catch (IOException e) {
            throw new ApplicationException(String.format("保存笔记内容异常: %s", e.getMessage()));
        }
    }

    /**
     * 创建笔记
     */
    public void createNote(String relPath) {
        Path path = combineAbsPath(relPath);
        if (Files.exists(path)) {
            throw new ApplicationException("创建笔记异常: 笔记文件已存在");
        }
        try {
            Files.writeString(path, "");
        } catch (IOException e) {
            throw new ApplicationException(String.format("创建笔记异常: %s", e.getMessage()));
        }
    }

    /**
     * 删除笔记
     */
    public void deleteNote(String relPath) {
        try {
            Files.delete(combineAbsPath(relPath));
        } catch (IOException e) {
            throw new ApplicationException(String.format("删除笔记异常: %s", e.getMessage()));
        }
    }

    // =================================================================================================================

    /**
     * 启动监控笔记文件变化
     */
    public void startWatch() {
        try {
            watcher = DirectoryWatcher.builder()
                    .path(Paths.get(noteRootAbsPath))
                    .listener(event -> {

                        File file = event.path().toFile();
                        // String path = event.path().normalize().toAbsolutePath().toString();
                        String path = file.getPath();
                        extraLogService.logNoteInfo(
                                String.format("监测到笔记文件变化: %s %s", event.eventType(), path),
                                logger
                        );

                        switch (event.eventType()) {
                            case CREATE:
                                addNote(file);
                                break;
                            case MODIFY:
                                dropNote(file);
                                addNote(file);
                                break;
                            case DELETE:
                                dropNote(file);
                                break;
                            default:
                                break;
                        }
                    })
                    //.fileHashing(false)     // 禁用 hashing，参数为 true 时删除空文件会监控不到
                    .fileHashing(true)
                    .build();
            watcher.watchAsync();
            extraLogService.logNoteInfo(String.format("启动笔记监控成功: %s", noteRootAbsPath), logger);
        } catch (IOException e) {
            extraLogService.logNoteError(String.format("启动笔记监控异常: %s %s", noteRootAbsPath, e.getMessage()), logger);
        }
    }

    /**
     * 停止监控笔记文件变化
     */
    public void stopWatch() {
        if (watcher != null) {
            try {
                watcher.close();
                extraLogService.logNoteInfo(String.format("停止笔记监控成功"), logger);
            } catch (IOException e) {
                extraLogService.logNoteError(String.format("停止笔记监控异常: %s", e.getMessage()), logger);
            }
        }
    }
}


















