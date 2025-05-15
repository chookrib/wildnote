package cool.done.wildnotesvc.domain;

import io.methvin.watcher.DirectoryWatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

/**
 * 笔记Service
 */
@Service
public class NoteService {
    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    @Value("${wildnote.path}")
    private String notePath;

    @Autowired
    private IFileHandler fileHandler;

    /**
     * 读取笔记列表
     */
    public ArrayList<NoteIndex> getNotes() {
        if (StringUtils.isEmpty(notePath)) {
            throw new ValidationException("未配置笔记文件夹路径");
        }

        ArrayList<NoteIndex> noteIndexes = new ArrayList<>();
        int notePathLevel = notePath.replace("\\", "/").split("/").length;

        ArrayList<File> files = fileHandler.getFiles(notePath);
        for (File file : files) {
            int level = file.getPath().replace("\\", "/").split("/").length - notePathLevel - 1;

            BasicFileAttributes attrs = null;
            try {
                attrs = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    public String getNote(String path) {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止读取笔记文件夹路径以外的文件");
        return fileHandler.getFile(path);
    }

    /**
     * 保存笔记
     */
    public void saveNote(String path, String content) {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止保存笔记文件夹路径以外的文件");
        fileHandler.saveFile(path, content);
    }

    /**
     * 创建笔记
     */
    public void createNote(String path) {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止在笔记文件夹路径以外创建文件");
        fileHandler.createFile(path);
    }

    /**
     * 删除笔记
     */
    public void deleteNote(String path) {
        if (!StringUtils.isEmpty(notePath) && !path.startsWith(notePath))
            throw new ValidationException("禁止删除笔记文件夹路径以外的文件");
        fileHandler.deleteFile(path);
    }

    //private WatchService watchService;
    //private Thread watchThread;
    //private volatile boolean watching = false;

    //@Autowired
    //private DirectoryWatcher1 directoryWatcher;

    /**
     * 监控笔记目录变化
     */
    public void watchNoteChange() {
        if (StringUtils.isEmpty(notePath)) {
            throw new ValidationException("未配置笔记文件夹路径");
        }

        File noteDirectory = new File(notePath);
        if (!noteDirectory.exists() || !noteDirectory.isDirectory()) {
            throw new ValidationException("笔记文件夹路径无效");
        }

        DirectoryWatcher watcher = null;
        try {
            watcher = DirectoryWatcher.builder()
                    .path(Paths.get(notePath))
                    .listener(event -> {
                        System.out.println(event.eventType() + ": " + event.path());
                    })
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        watcher.watchAsync();

        //try {
        //    directoryWatcher.startWatching(Path.of(notePath));
        //    logger.info("文件监控已启动，监控路径: {}", notePath);
        //} catch (IOException e) {
        //    logger.error("启动文件监控失败", e);
        //    throw new RuntimeException("启动文件监控失败", e);
        //}

        /*
        try {
            // 关闭现有的监控
            stopWatching();

            // 创建新的监控服务
            watchService = Path.of(notePath).getFileSystem().newWatchService();
            //Path path = Path.of(notePath);
            //path.register(watchService,
            //        java.nio.file.StandardWatchEventKinds.ENTRY_CREATE,
            //        java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
            //        java.nio.file.StandardWatchEventKinds.ENTRY_DELETE);

            Path rootPath = Path.of(notePath);

            // 创建一个映射来跟踪已注册的WatchKey与对应的路径
            Map<WatchKey, Path> keys = new HashMap<>();

            // 递归注册所有子目录
            //Files.walk(rootPath)
            //        .filter(Files::isDirectory)
            //        .forEach(path -> {
            //            try {
            //                path.register(watchService,
            //                        java.nio.file.StandardWatchEventKinds.ENTRY_CREATE,
            //                        java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
            //                        java.nio.file.StandardWatchEventKinds.ENTRY_DELETE);
            //            } catch (IOException e) {
            //                logger.error("注册目录失败: {}", path, e);
            //            }
            //        });

            // 递归注册所有子目录
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchKey key = dir.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    keys.put(key, dir);
                    logger.info("已注册目录: {}", dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            watching = true;
            watchThread = new Thread(() -> {
                try {
                    while (watching) {

                        WatchKey key;
                        try {
                            key = watchService.take();
                        } catch (InterruptedException e) {
                            return;
                        }

                        // 获取此key对应的目录路径
                        Path dir = keys.get(key);
                        if (dir == null) {
                            logger.warn("收到未知WatchKey的事件");
                            continue;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            // 忽略OVERFLOW事件
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            // 获取文件名并解析完整路径
                            Path name = (Path) event.context();
                            Path child = dir.resolve(name);

                            logger.info("文件变化: {} - {}", kind.name(), child);

                            // 如果创建了新目录，递归注册它
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                try {
                                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                        Files.walkFileTree(child, new SimpleFileVisitor<Path>() {
                                            @Override
                                            public FileVisitResult preVisitDirectory(Path newDir, BasicFileAttributes attrs) throws IOException {
                                                WatchKey newKey = newDir.register(watchService,
                                                        StandardWatchEventKinds.ENTRY_CREATE,
                                                        StandardWatchEventKinds.ENTRY_DELETE,
                                                        StandardWatchEventKinds.ENTRY_MODIFY);
                                                keys.put(newKey, newDir);
                                                logger.info("已注册新目录: {}", newDir);
                                                return FileVisitResult.CONTINUE;
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    logger.error("注册新目录失败", e);
                                }
                            }
                        }

                        // 重置key，如果已无效则删除
                        boolean valid = key.reset();
                        if (!valid) {
                            keys.remove(key);
                        }

                        //try {
                        //    var key = watchService.take();
                        //    for (var event : key.pollEvents()) {
                        //        var kind = event.kind();
                        //        var fileName = event.context();
                        //        logger.info("文件变化: {} - {}", kind.name(), ((Path)fileName).getFileName());
                        //
                        //        // 如果是目录创建事件，递归注册新目录
                        //        if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE) {
                        //            Path createdPath = rootPath.resolve((Path) fileName);
                        //            if (Files.isDirectory(createdPath)) {
                        //                try {
                        //                    createdPath.register(watchService,
                        //                            java.nio.file.StandardWatchEventKinds.ENTRY_CREATE,
                        //                            java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
                        //                            java.nio.file.StandardWatchEventKinds.ENTRY_DELETE);
                        //                    logger.info("新目录已注册: {}", createdPath);
                        //                } catch (IOException e) {
                        //                    logger.error("注册新目录失败: {}", createdPath, e);
                        //                }
                        //            }
                        //        }
                        //    }
                        //    key.reset();
                        //} catch (InterruptedException e) {
                        //    Thread.currentThread().interrupt();
                        //    logger.error("监控线程被中断", e);
                        //    break;
                        //} catch (Exception e) {
                        //    logger.error("文件监控发生错误", e);
                        //}
                    }
                } catch (ClosedWatchServiceException e) {
                    logger.info("WatchService已关闭");
                } catch (Exception e) {
                    logger.error("监控线程异常", e);
                }
            });

            watchThread.setName("FileWatcher-Thread");
            watchThread.setDaemon(true);
            watchThread.start();
            logger.info("文件监控已启动，监控路径: {}", notePath);
        } catch (IOException e) {
            logger.error("初始化 WatchService 失败", e);
            throw new RuntimeException("初始化文件监控失败", e);
        }
        */
    }

    /**
     * 停止文件监控
     */
    public void stopWatching() {
        //if (watching) {
        //    watching = false;
        //    if (watchThread != null) {
        //        watchThread.interrupt();
        //        watchThread = null;
        //    }
        //    if (watchService != null) {
        //        try {
        //            watchService.close();
        //            watchService = null;
        //        } catch (IOException e) {
        //            logger.error("关闭文件监控服务失败", e);
        //        }
        //    }
        //    logger.info("文件监控已停止");
        //}

        //directoryWatcher.stop();
    }
}


















