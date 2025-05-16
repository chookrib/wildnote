package cool.done.wildnotesvc.domain;

import io.methvin.watcher.DirectoryWatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 笔记Service
 */
@Service
public class NoteService {
    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final String rootAbsPath;

    private final IReminder reminder;
    private final TaskScheduler taskScheduler;

    private DirectoryWatcher watcher = null;
    private Map<String, NoteIndexNode> noteMap = new ConcurrentHashMap<>();
    private Map<String, ScheduledFuture<?>> schdMap = new ConcurrentHashMap<>();

    public NoteService(@Value("${wildnote.root-path}") String rootPath,
                       IReminder reminder, TaskScheduler taskScheduler) {

        if (StringUtils.isEmpty(rootPath)) {
            throw new ValidationException("未配置笔记根路径");
        }

        //File rootPathFile = new File(rootPath);
        //if (!rootPathFile.exists() || !rootPathFile.isDirectory()) {
        //    throw new ValidationException("笔记根路径不存在");
        //}
        //rootAbsPath = rootPathFile.toPath().normalize().toAbsolutePath().toString();     // 不会处理 ..

        try {
            rootAbsPath = Path.of(rootPath).toRealPath().toString();
        } catch (IOException e) {
            throw new ValidationException(String.format("笔记根路径无效: %s", e.getMessage()));
        }

        logger.info("笔记根路径: {}", rootAbsPath);

        this.reminder = reminder;
        this.taskScheduler = taskScheduler;
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
     * 重新加载笔记
     */
    public void reloadNote() {

        noteMap.clear();

        // 停止所有定时器
        schdMap.values().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        schdMap.clear();

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
            Files.walkFileTree(Path.of(rootAbsPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    files.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    files.add(dir.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        files.sort((file1, file2) -> {
            if (file1.isDirectory() && file2.isFile())
                return -1;
            if (file1.isFile() && file2.isDirectory())
                return 1;
            return file1.getName().compareTo(file2.getName());
        });

        for (File file : files) {
            noteMap.put(file.getPath(), createNoteIndexNode(file));
            processCron(file);
        }
    }

    /**
     * 创建笔记目录节点
     */
    private NoteIndexNode createNoteIndexNode(File file) {

        Path path = file.toPath().normalize();
        String absPath = path.toAbsolutePath().toString();              // D:\xxx\log\log.log
        String relPath = absPath.substring(rootAbsPath.length());       // log\log.log

        int level = relPath.split("\\\\|/").length - 1;

        Long creationTime = null;
        try {
            BasicFileAttributes attrs = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            creationTime = attrs.creationTime().toMillis();
        } catch (IOException e) {
            logger.error("获取笔记文件属性失败: {}", absPath);
        }

        return new NoteIndexNode(
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
        // 判断文件名
        if (!file.getName().endsWith(".cron.md")) {
            return;
        }

        String path = file.getPath();

        try (var lines = Files.lines(file.toPath())) {
            // cron = lines.findFirst().orElse("");
            int lineNumber = 1;
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.startsWith("> cron")) {
                    String[] parts = line.split("\\|");
                    String cron = parts.length > 1 ? parts[1].trim() : "";

                    String schdKey = path + " | " + lineNumber + " | " + cron;
                    if (StringUtils.isEmpty(cron)) {
                        logger.error("笔记提醒Scheduled失败: cron表达式空 {}", schdKey);
                        return;
                    }

                    CronTrigger cronTrigger;
                    try {
                        cronTrigger = new CronTrigger(cron);
                    } catch (IllegalArgumentException e) {
                        logger.error("笔记提醒Scheduled失败: cron表达式不合法 {}", schdKey);
                        return;
                    }

                    String reminderMessage = path + " | " + lineNumber + " | " + line;
                    ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
                        reminder.remind(reminderMessage);
                    }, cronTrigger);

                    schdMap.put(schdKey, scheduledFuture);
                    logger.info("笔记提醒Scheduled成功: {}", schdKey);

                    lineNumber++;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("处理笔记提醒事项失败: 读取文件异常 {} {}", path, e.getMessage());
        }
    }

    /**
     * 取消并删除定时任务
     */
    private void removeCron(String path) {
        schdMap.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(path + " | ")) {
                entry.getValue().cancel(true);
                return true;
            }
            return false;
        });
    }

    /**
     * 获取所有cron定时任务
     */
    public List<String> getAllCron() {
        return schdMap.keySet().stream().toList();
    }

    /**
     * 开始监控笔记文件变化
     */
    public void startWatch() {
        try {
            watcher = DirectoryWatcher.builder()
                    .path(Paths.get(rootAbsPath))
                    .listener(event -> {

                        File file = event.path().toFile();
                        // String path = event.path().normalize().toAbsolutePath().toString();
                        String path = file.getPath();
                        logger.info("监测到笔记文件变化: {} {}", event.eventType(), path);

                        switch (event.eventType()) {
                            case CREATE:
                                noteMap.put(path, createNoteIndexNode(file));
                                processCron(file);
                                break;
                            case MODIFY:
                                noteMap.remove(path);
                                noteMap.put(path, createNoteIndexNode(file));   // 重新添加以更新文件属性
                                removeCron(path);
                                processCron(file);
                                break;
                            case DELETE:
                                noteMap.remove(path);
                                removeCron(path);
                                break;
                            default:
                                break;
                        }
                    })
                    .fileHashing(false)     // 禁用hashing，启用时删除空文件会监控不到
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
    private String combineAbsPath(String relPath) {
        String absPath = Path.of(rootAbsPath, relPath).normalize().toAbsolutePath().toString();
        if (!absPath.startsWith(rootAbsPath)) {
            throw new ValidationException("禁止访问笔记根路径以外的文件");
        }
        return absPath;
    }

    /**
     * 取笔记目录
     */
    public Map<String, NoteIndexNode> getNoteMap() {
        return noteMap;
    }

    /**
     * 读取笔记
     */
    public String getNote(String relPath) {

        try {
            return Files.readString(Path.of(relPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存笔记
     */
    public void saveNote(String relPath, String content) {
        try {
            Files.writeString(Path.of(relPath), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建笔记
     */
    public void createNote(String relPath) {
        Path path = Path.of(relPath);
        if (Files.exists(path))
            throw new ValidationException("笔记文件已存在");
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
            Files.delete(Path.of(relPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


















