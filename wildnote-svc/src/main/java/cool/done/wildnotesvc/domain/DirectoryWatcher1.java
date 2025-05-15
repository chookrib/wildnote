package cool.done.wildnotesvc.domain;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DirectoryWatcher1 {
    private final WatchService watchService;
    private final Map<WatchKey, Path> keyPathMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    public DirectoryWatcher1() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    /**
     * 开始监控指定路径及其所有子目录
     */
    public void startWatching(Path rootPath) throws IOException {
        if (running) return;

        // 递归注册所有目录
        registerAll(rootPath);

        // 启动监控线程
        running = true;
        executor.submit(() -> {
            try {
                watchLoop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 递归注册目录及其所有子目录
     */
    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 注册单个目录
     */
    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        keyPathMap.put(key, dir);
    }

    /**
     * 监控循环
     */
    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            Path dir = keyPathMap.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // 忽略OVERFLOW事件
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // 获取事件关联的文件名
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>) event).context();
                Path child = dir.resolve(name);

                // 打印事件信息
                System.out.printf("事件: %s: %s%n", kind.name(), child);

                // 如果是目录创建事件，递归注册新目录
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 重置key，如果无效则删除映射
            boolean valid = key.reset();
            if (!valid) {
                keyPathMap.remove(key);

                // 如果没有更多的目录要监控，退出
                if (keyPathMap.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * 停止监控
     */
    public void stop() {
        running = false;
        executor.shutdownNow();

        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
