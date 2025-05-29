package cool.done.wildnote.server.domain;

import io.methvin.watcher.DirectoryWatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 笔记Service
 */
@Service
public class NoteService {
    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final String noteRootAbsPath;
    private final String[] noteExtensions;

    private DirectoryWatcher watcher = null;
    private Map<String, NotePath> notePathMap = new ConcurrentHashMap<>();

    private final INoteRemindScheduler remindScheduler;

    public NoteService(@Value("${wildnote.note-root-path}") String noteRootPath,
                       @Value("${wildnote.note-extensions}") String noteExtensions,
                       //@Qualifier("noteRemindTaskScheduler") INoteRemindScheduler remindScheduler,
                       @Qualifier("noteRemindQuartzScheduler") INoteRemindScheduler remindScheduler
    ) {

        if (StringUtils.isEmpty(noteRootPath)) {
            throw new RuntimeException("未配置笔记根路径");
        }
        Path noteRootRealPath;
        try {
            noteRootRealPath = Path.of(noteRootPath).toRealPath();
        } catch (IOException e) {
            throw new RuntimeException(String.format("笔记根路径无效: %s", e.getMessage()));
        }
        //if (!Files.exists(noteRootRealPath)) {
        //    throw new RuntimeException("笔记根路径不存在");
        //}
        if (!Files.isDirectory(noteRootRealPath)) {
            throw new RuntimeException("笔记根路径必须为文件夹");
        }
        this.noteRootAbsPath = noteRootRealPath.toString();
        logger.info("笔记根路径: {}", noteRootAbsPath);

        this.noteExtensions = noteExtensions.split(",");
        this.remindScheduler = remindScheduler;
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
     * 验证笔记文件扩展名
     */
    private boolean isValidExtension(File file) {
        return Arrays.stream(noteExtensions)
                .anyMatch(ext -> file.getName().toLowerCase().endsWith(ext.toLowerCase()));
    }

    /**
     * 验证笔记文件提醒扩展名
     */
    private boolean isValidCronExtension(File file) {
        return Arrays.stream(noteExtensions)
                .anyMatch(ext -> file.getName().toLowerCase().endsWith(".cron" + ext.toLowerCase()));
    }

    /**
     * 重新加载笔记
     */
    public void reloadNote() {

        notePathMap.clear();
        remindScheduler.clearAll();

        ArrayList<File> files = new ArrayList<>();

        //File[] list = new File(path).listFiles();
        //if (list == null)
        //    return files;
        //
        //Arrays.sort(list, (file1, file2) -> {
        //    if (file1.isDirectory() && file2.isFile())
        //        return -1;
        //    if (file1.isFile() && file2.isDirectory())
        //        return 1;
        //    return file1.getName().compareTo(file2.getName());
        //});
        //
        //for (File file : list) {
        //    files.add(file);
        //    if (file.isDirectory())
        //        files.addAll(getFiles(file.getPath()));
        //}

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //files.sort((file1, file2) -> {
        //    if (file1.isDirectory() && file2.isFile())
        //        return -1;
        //    if (file1.isFile() && file2.isDirectory())
        //        return 1;
        //    return file1.getName().compareTo(file2.getName());
        //});

        for (File file : files) {
            addNotePath(file);
            processCron(file);
        }
    }

    /**
     * 添加笔记路径对象
     */
    private void addNotePath(File file) {
        if (file.isFile() && !isValidExtension(file)) {
            return;
        }
        notePathMap.put(file.getPath(), createNotePath(file));
    }

    /**
     * 移除笔记路径对象
     */
    private void removeNotePath(File file) {
        notePathMap.remove(file.getPath());
    }

    /**
     * 创建笔记路径对象
     */
    private NotePath createNotePath(File file) {

        String absPath = file.toPath().normalize().toAbsolutePath().toString();              // D:\xxx\log\log.log
        String relPath = absPath.substring(noteRootAbsPath.length());       // log\log.log

        int level = relPath.split("\\\\|/").length - 2;

        Long creationTime = null;
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            creationTime = attrs.creationTime().toMillis();
        } catch (IOException e) {
            logger.error("获取笔记文件属性失败: {}", absPath);
        }

        return new NotePath(
                level,
                relPath,
                absPath,
                file.getName(),
                file.isDirectory(),
                creationTime,
                file.lastModified()
        );
    }

    /**
     * 处理笔记文件中的提醒
     */
    private void processCron(File file) {

        String path = file.getPath().substring(noteRootAbsPath.length());

        if (!file.isFile() || !isValidCronExtension(file)) {
            return;
        }

        try (var lines = Files.lines(file.toPath())) {
            // cron = lines.findFirst().orElse("");
            int lineNumber = 0;
            for (String line : (Iterable<String>) lines::iterator) {
                lineNumber++;
                if (!line.startsWith("> cron")) {
                    continue;
                }

                String[] parts = line.split("\\|");
                String cron = parts.length >= 1 ? parts[1].trim() : "";
                if (StringUtils.isEmpty(cron)) {
                    logger.error("处理笔记提醒失败: cron表达式空 {} {}", path, lineNumber);
                    continue;
                }

                String message = parts.length >= 2 ? parts[2].trim() : "";
                try {
                    remindScheduler.add(path, lineNumber, cron, message);
                    logger.info("处理笔记提醒成功: {} {} {}", path, lineNumber, cron);
                } catch (Exception e) {
                    logger.error("处理笔记提醒失败: {} {} {} {}", path, lineNumber, cron, e.getMessage());
                    continue;
                }
            }
        } catch (IOException e) {
            logger.error("处理笔记提醒失败: 读取文件异常 {} {}", path, e.getMessage());
        }
    }


    /**
     * 删除提醒计划任务
     */
    private void removeCron(File file) {
        String path = file.getPath().substring(noteRootAbsPath.length());
        remindScheduler.remove(path);
        logger.info("移除笔记提醒成功: {}", path);
    }

    /**
     * 取所有提醒计划任务
     */
    public List<NoteRemindCron> getAllCron() {
        return remindScheduler.getAll();
    }

    /**
     * 开始监控笔记文件变化
     */
    public void startWatch() {
        try {
            watcher = DirectoryWatcher.builder()
                    .path(Paths.get(noteRootAbsPath))
                    .listener(event -> {

                        File file = event.path().toFile();
                        // String path = event.path().normalize().toAbsolutePath().toString();
                        String path = file.getPath();
                        logger.info("监测到笔记文件变化: {} {}", event.eventType(), path);

                        switch (event.eventType()) {
                            case CREATE:
                                addNotePath(file);
                                processCron(file);
                                break;
                            case MODIFY:
                                removeNotePath(file);
                                addNotePath(file);      // 重新添加以更新文件属性
                                removeCron(file);
                                processCron(file);
                                break;
                            case DELETE:
                                removeNotePath(file);
                                removeCron(file);
                                break;
                            default:
                                break;
                        }
                    })
                    //.fileHashing(false)     // 禁用hashing，启用时删除空文件会监控不到
                    .fileHashing(true)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        watcher.watchAsync();
    }

    /**
     * 停止监控笔记文件变化
     */
    public void stopWatch() {
        if (watcher != null) {
            try {
                watcher.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 拼接绝对路径
     */
    private Path combineAbsPath(String relPath) {
        Path path = Path.of(noteRootAbsPath, relPath);
        String absPath = path.normalize().toAbsolutePath().toString();
        if (!absPath.startsWith(noteRootAbsPath)) {
            throw new RuntimeException("禁止访问笔记根路径以外的文件");
        }
        return path;
    }

    /**
     * 取笔记路径对象Map
     */
    public Map<String, NotePath> getNotePathMap() {
        return notePathMap;
    }

    /**
     * 读取笔记
     */
    public String getNote(String relPath) {

        try {
            return Files.readString(combineAbsPath(relPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存笔记
     */
    public void saveNote(String relPath, String content) {
        try {
            Files.writeString(combineAbsPath(relPath), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建笔记
     */
    public void createNote(String relPath) {
        Path path = combineAbsPath(relPath);
        if (Files.exists(path))
            throw new RuntimeException("笔记文件已存在");
        try {
            Files.writeString(path, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除笔记
     */
    public void deleteNote(String relPath) {
        try {
            Files.delete(combineAbsPath(relPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


















