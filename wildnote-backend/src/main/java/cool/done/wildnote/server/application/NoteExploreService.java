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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 笔记资源管理 Service
 */
@Component
public class NoteExploreService {

    private static final Logger logger = LoggerFactory.getLogger(NoteExploreService.class);

    private final String noteRootAbsPath;
    private final List<String> noteExtensionList;
    private final List<String> notePathPrefixExcludeList;
    private final List<String> cronFilenameFilterList;
    private final String cronExpressionPrefix;
    private final String cronExpressionSeparator;
    private final String settingFileAbsPath;

    private DirectoryWatcher watcher = null;

    // 存储笔记树的 Map，key=笔记文件绝对路径，value=笔记树节点对象
    private Map<String, NoteTreeNode> noteMap = new ConcurrentHashMap<>();

    private final NoteRemindService noteRemindService;
    private final ExtraLogService extraLogService;
    private final NoteSettingService noteSettingService;

    public NoteExploreService(
            @Value("${app.note-root-path}") String noteRootPath,
            @Value("${app.note-extensions}") String noteExtensions,
            @Value("${app.note-path-prefix-excludes}") String notePathPrefixExcludes,
            @Value("${app.cron-filename-filters:}") String cronFilenameFilters,
            @Value("${app.cron-expression-prefix:}") String cronExpressionPrefix,
            @Value("${app.cron-expression-separator:}") String cronExpressionSeparator,
            @Value("${app.setting-file-path:}") String settingFilePath,
            NoteRemindService noteRemindService,
            ExtraLogService extraLogService,
            NoteSettingService noteSettingService
    ) {
        if (ValueUtility.isEmptyString(noteRootPath)) {
            throw new ApplicationException("未配置笔记根路径");
        }
        Path path;
        try {
            // toRealPath 会解析符号链接，并返回规范化后的绝对路径，同时会检查路径是否真实存在。如果路径不存在或无权限，会抛出异常
            path = Path.of(noteRootPath).toRealPath();
        } catch (IOException ex) {
            throw new ApplicationException(String.format("笔记根路径非法 %s", ex.getMessage()), ex);
        }
        // if (!Files.exists(path)) {
        //    throw new ApplicationException("笔记根路径不存在");
        //}
        if (!Files.isDirectory(path)) {
            throw new ApplicationException("笔记根路径必须为文件夹");
        }
        this.noteRootAbsPath = path.toString();
        // this.noteExtensionList = Arrays.stream(noteExtensions.split(",")).toList();
        this.noteExtensionList = Arrays.stream(noteExtensions.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
        this.notePathPrefixExcludeList = Arrays.stream(notePathPrefixExcludes.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
        this.cronFilenameFilterList = Arrays.stream(cronFilenameFilters.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
        this.cronExpressionPrefix = cronExpressionPrefix;
        this.cronExpressionSeparator = cronExpressionSeparator;

        // 由 NoteExploreService 扎口处理笔记配置文件路径转换
        String settingFileAbsPath = "";
        if (!ValueUtility.isEmptyString(settingFilePath)) {
            try {
                settingFileAbsPath = combineAbsPath(settingFilePath).toString();
            } catch (Exception ex) {
                extraLogService.logNoteError(
                        String.format("笔记配置文件路径 %s 异常: %s", settingFilePath, ex.getMessage()), logger
                );
            }
        }
        this.settingFileAbsPath = settingFileAbsPath;

        this.noteRemindService = noteRemindService;
        this.extraLogService = extraLogService;
        this.noteSettingService = noteSettingService;
    }

    /**
     * 应用启动后运行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        loadRoot();
        startDirectoryWatch();
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
     * 从笔记根路径加载所有笔记
     */
    public void loadRoot() {
        // 清除现有数据和提醒
        noteMap.clear();
        noteRemindService.clearCron();

        ArrayList<File> fileAndDirList = new ArrayList<>();
        try {
            Files.walkFileTree(Path.of(noteRootAbsPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    fileAndDirList.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(Path.of(noteRootAbsPath))) {    // 排除根路径
                        fileAndDirList.add(dir.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            extraLogService.logNoteInfo(String.format("加载笔记文件夹成功: %s", noteRootAbsPath), logger);
        } catch (IOException ex) {
            extraLogService.logNoteError(
                    String.format("加载笔记文件夹异常: %s %s", noteRootAbsPath, ex.getMessage()), logger
            );
        }

        // fileAndDirList.sort((f1, f2) -> {
        //    if (f1.isDirectory() && f2.isFile())
        //        return -1;
        //    if (f1.isFile() && f2.isDirectory())
        //        return 1;
        //    return f1.getName().compareTo(f2.getName());
        //});

        for (File item : fileAndDirList) {
            processFile(item);
        }
    }

    /**
     * 笔记文件夹中的文件和文件夹处理
     */
    private void processFile(File fileOrDir) {
        // 需先判断是否存在，当不存在时 fileOrDir.isDirectory() 和 fileOrDir.isFile() 都会返回 false
        if (!fileOrDir.exists()) {
            return;
        }

        // 如果是笔记配置文件，加载配置，需要在判断扩展名之前
        if (fileOrDir.getPath().equals(this.settingFileAbsPath)) {
            noteSettingService.loadSetting(this.settingFileAbsPath);
            return;     // 不处理配置文件
        }

        // 跳过不符合扩展名的文件，仅跳过文件，文件夹仍然需要添加
        if (fileOrDir.isFile()) {
            String ext = fileOrDir.getName().contains(".") ?
                    "." + fileOrDir.getName().substring(fileOrDir.getName().lastIndexOf('.') + 1).toLowerCase() : "";
            if (!this.noteExtensionList.contains(ext)) {
                return;
            }
        }

        // 添加文件或文件夹
        String absPath = fileOrDir.toPath().toAbsolutePath().normalize().toString();     // D:\xxx\log\log.log
        String relPath = absPath.substring(noteRootAbsPath.length());               // log\log.log

        // int level = relPath.split("\\\\|/").length - 2;
        int level = relPath.split(Pattern.quote(File.separator)).length - 2;

        Long creationTime = null;
        try {
            BasicFileAttributes attrs = Files.readAttributes(fileOrDir.toPath(), BasicFileAttributes.class);
            creationTime = attrs.creationTime().toMillis();
        } catch (IOException ex) {
            extraLogService.logNoteError(String.format("获取文件系统创建时间属性异常: %s", absPath), logger);
        }

        NoteTreeNode note = NoteTreeNode.create(
                relPath,
                absPath,
                fileOrDir.getName(),
                fileOrDir.isDirectory(),
                creationTime,
                fileOrDir.lastModified(),
                level
        );

        // 判断是否在排除路径范围内
        if(this.notePathPrefixExcludeList.stream().anyMatch(fileOrDir.getName().toLowerCase()::startsWith)) {
            return;
        }

        noteMap.put(fileOrDir.getPath(), note);
        processCron(fileOrDir);
    }

    /**
     * 处理笔记文件中的提醒
     */
    private void processCron(File file) {
        if(!file.isFile()) {
            return;
        }
        // 判断 cron 文件名
        if(this.cronFilenameFilterList.isEmpty() || this.cronFilenameFilterList.stream().noneMatch(file.getName().toLowerCase()::contains)) {
            return;
        }
        if(ValueUtility.isEmptyString(this.cronExpressionPrefix) || ValueUtility.isEmptyString(this.cronExpressionSeparator)){
            return;
        }

        // 取出相对路径用于注册提醒服务的路径
        String path = file.getPath().substring(noteRootAbsPath.length());
        try (var lines = Files.lines(file.toPath())) {
            // cron = lines.findFirst().orElse("");
            int lineNumber = 0;
            for (String line : (Iterable<String>) lines::iterator) {
                lineNumber++;
                if (!line.startsWith(this.cronExpressionPrefix)) {
                    continue;
                }

                String[] parts = line.split(Pattern.quote(this.cronExpressionSeparator));
                String cronExpression = parts.length >= 1 ? parts[1].trim() : "";
                String description = parts.length >= 2 ? parts[2].trim() : "";
                noteRemindService.scheduleCron(path, lineNumber, cronExpression, description);
            }
        } catch (IOException ex) {
            extraLogService.logNoteError(String.format("解析笔记文件cron失败: %s %s", path, ex.getMessage()), logger);
        }
    }

    /**
     * 笔记文件夹中移除文件和文件夹处理
     */
    private void removeFileAndCron(File file) {
        noteMap.remove(file.getPath());
        noteRemindService.unscheduleCron(
                file.getPath().substring(noteRootAbsPath.length())  // 路径需要与 addCron 时 path 一致
        );
    }

    // =================================================================================================================

    /**
     * 拼接路径并返回绝对路径
     */
    private Path combineAbsPath(String relPath) {
        Path path = Path.of(noteRootAbsPath, relPath);
        String absPath = path.toAbsolutePath().normalize().toString();
        if (!absPath.startsWith(noteRootAbsPath)) {
            throw new ApplicationException("禁止访问笔记根路径以外的文件");
        }
        return path;
    }

    /**
     * 获取笔记文件夹中文件内容
     */
    public String getFileContent(String relPath) {
        try {
            return Files.readString(combineAbsPath(relPath));
        } catch (IOException ex) {
            throw new ApplicationException(String.format("读取笔记内容异常: %s", ex.getMessage()), ex);
        }
    }

    /**
     * 保存笔记文件夹中文件内容
     */
    public void saveFileContent(String relPath, String content) {
        try {
            Files.writeString(combineAbsPath(relPath), content);
            // 后续由 watcher 监控并处理
        } catch (IOException ex) {
            throw new ApplicationException(String.format("保存笔记内容异常: %s", ex.getMessage()), ex);
        }
    }

    /**
     * 追加笔记文件夹中文件内容，追加到文件末尾
     */
    public void appendFileContent(String relPath, String content) {
        try (FileWriter writer = new FileWriter(
                combineAbsPath(relPath).toFile(), StandardCharsets.UTF_8, true)
        ) {
            writer.write(content);
        } catch (IOException ex) {
            throw new ApplicationException(String.format("追加笔记内容异常: %s", ex.getMessage()), ex);
        }
    }

    /**
     * 插入笔记文件夹中文件内容，插入到文件开头
     */
    public void insertFileContent(String relPath, String content) {
        // 写法一
        // try {
        //    Path path = combineAbsPath(relPath);
        //    byte[] originalBytes = Files.readAllBytes(path);
        //    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        //
        //    byte[] newBytes = new byte[originalBytes.length + contentBytes.length];
        //    System.arraycopy(originalBytes, 0, newBytes, 0, (int) originalBytes.length);
        //    System.arraycopy(contentBytes, 0, newBytes, originalBytes.length, contentBytes.length);
        //    Files.write(path, newBytes);
        //} catch (IOException ex) {
        //    throw new ApplicationException(String.format("插入笔记内容异常: %s", ex.getMessage()), ex);
        //}

        // 写法二
        try {
            Path path = combineAbsPath(relPath);
            String oldContent = "";
            if (path.toFile().exists()) {
                oldContent = Files.readString(path, StandardCharsets.UTF_8);
            }
            try (FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8, false)) {
                writer.write(content + oldContent);
            }
        } catch (IOException ex) {
            throw new ApplicationException(String.format("插入笔记内容异常: %s", ex.getMessage()), ex);
        }

        // 写法三
        // try {
        //    Path path = combineAbsPath(relPath);
        //    Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        //    // 写入插入内容
        //    try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
        //        writer.write(content);
        //        writer.newLine();
        //        // 追加原文件内容
        //        if (Files.exists(path)) {
        //            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        //                String line;
        //                while ((line = reader.readLine()) != null) {
        //                    writer.write(line);
        //                    writer.newLine();
        //                }
        //            }
        //        }
        //    }
        //    // 替换原文件
        //    Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        //} catch (IOException ex) {
        //    throw new ApplicationException(String.format("插入笔记内容异常: %s", ex.getMessage()), ex);
        //}
    }

    ///**
    // * 在笔记文件夹中创建文件
    // */
    // public void createFile(String relPath) {
    //    Path path = combineAbsPath(relPath);
    //    if (Files.exists(path)) {
    //        throw new ApplicationException("创建笔记异常: 笔记文件已存在");
    //    }
    //    try {
    //        Files.writeString(path, "");
    //        // 后续由 watcher 监控并处理
    //    } catch (IOException ex) {
    //        throw new ApplicationException(String.format("创建笔记异常: %s", ex.getMessage()), ex);
    //    }
    //}

    /**
     * 在笔记文件夹中创建笔记文件
     */
    public void createFile(String relPath) {
        // String relPath = parentRelPath + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".md";
        Path absPath = combineAbsPath(relPath);
        if (!Files.exists(absPath)) {
            try {
                Files.writeString(absPath, "");    // 创建文件
                // 后续由 watcher 监控并处理
            } catch (IOException ex) {
                throw new ApplicationException(String.format("创建笔记文件异常: %s", ex.getMessage()), ex);
            }
        }
    }

    /**
     * 在笔记文件夹中删除笔记文件
     */
    public void deleteFile(String relPath) {
        Path path = combineAbsPath(relPath);
        try {
            Files.delete(path);  // 不会进回收站
            // 后续由 watcher 监控并处理
        } catch (IOException ex) {
            throw new ApplicationException(String.format("删除笔记文件异常: %s", ex.getMessage()), ex);
        }
    }

    /**
     * 在笔记文件夹中移动笔记文件
     */
    public void moveFile(String sourceRelPath, String targetRelPath) {
        try {
            Path source = combineAbsPath(sourceRelPath);
            Path target = combineAbsPath(targetRelPath);
            // Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            Files.move(source, target);
            // 后续由 watcher 监控并处理
        } catch (IOException ex) {
            // throw new ApplicationException(String.format("移动笔记异常: %s", ex.getMessage()), ex);
            throw new ApplicationException(String.format("移动笔记文件异常: 目的路径中的父文件夹必须存在且不能存在同名文件"), ex);
        }
    }

    /**
     * 在笔记文件夹中创建文件夹
     */
    public void createDirectory(String relPath) {
        // String relPath = parentRelPath + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path absPath = combineAbsPath(relPath);
        if (!Files.exists(absPath)) {
            try {
                Files.createDirectories(absPath);
            } catch (IOException ex) {
                throw new ApplicationException(String.format("创建笔记文件夹异常: %s", ex.getMessage()), ex);
            }
        }
    }

    /**
     * 在笔记文件夹中删除文件夹
     */
    public void deleteDirectory(String relPath) {
        Path path = combineAbsPath(relPath);
        try {
            Files.delete(path);  // 不会进回收站
            // 后续由 watcher 监控并处理
        } catch (IOException ex) {
            // throw new ApplicationException(String.format("删除笔记文件夹异常: %s", ex.getMessage()), ex);
            throw new ApplicationException(String.format("删除笔记文件夹异常: 不能删除非空的笔记文件夹"), ex);
        }
    }

    /**
     * 在笔记文件夹中移动文件夹
     */
    public void moveDirectory(String sourceRelPath, String targetRelPath) {
        try {
            Path source = combineAbsPath(sourceRelPath);
            Path target = combineAbsPath(targetRelPath);
            // Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            Files.move(source, target);
        } catch (IOException ex) {
            // throw new ApplicationException(String.format("移动笔记文件夹异常: %s", ex.getMessage()), ex);
            throw new ApplicationException(String.format("移动笔记文件夹异常: 目的路径中的父文件夹必须存在且不能存在同名文件夹"), ex);
        }
    }

    // =================================================================================================================

    /**
     * 启动监控笔记文件夹变化
     */
    public void startDirectoryWatch() {
        try {
            watcher = DirectoryWatcher.builder()
                    .path(Paths.get(noteRootAbsPath))
                    .listener(event -> {

                        File file = event.path().toFile();
                        // String path = event.path().toAbsolutePath().normalize().toString();
                        String path = file.getPath();
                        extraLogService.logNoteInfo(
                                String.format("监测到笔记文件夹变化: %s %s", event.eventType(), path), logger
                        );

                        switch (event.eventType()) {
                            case CREATE:
                                processFile(file);
                                break;
                            case MODIFY:
                                removeFileAndCron(file);
                                processFile(file);
                                break;
                            case DELETE:
                                removeFileAndCron(file);
                                break;
                            default:
                                break;
                        }
                    })
                    //.fileHashing(false)     // 禁用 hashing，参数为 true 时删除空文件会监控不到
                    .fileHashing(true)
                    .build();
            watcher.watchAsync();
            extraLogService.logNoteInfo(String.format("启动笔记文件夹监控成功: %s", noteRootAbsPath), logger);
        } catch (IOException ex) {
            extraLogService.logNoteError(
                    String.format("启动笔记文件夹监控异常: %s %s", noteRootAbsPath, ex.getMessage()), logger
            );
        }
    }

    /**
     * 停止监控笔记文件夹变化
     */
    public void stopDirectoryWatch() {
        if (watcher != null) {
            try {
                watcher.close();
                extraLogService.logNoteInfo(String.format("停止笔记文件夹监控成功"), logger);
            } catch (IOException ex) {
                extraLogService.logNoteError(String.format("停止笔记文件夹监控异常: %s", ex.getMessage()), logger);
            }
        }
    }
}


















