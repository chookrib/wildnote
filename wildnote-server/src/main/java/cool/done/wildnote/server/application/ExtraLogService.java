package cool.done.wildnote.server.application;

import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 扩展日志 Service
 */
@Component
public class ExtraLogService {

    private static final Logger logger = LoggerFactory.getLogger(ExtraLogService.class);

    private String logRootAbsPath;

    public ExtraLogService(@Value("${wildnote.extra-log-root-path:}") String logRootPath) {
        if (ValueUtility.isBlank(logRootPath)) {
            throw new ApplicationException("未配置日志根路径");
        }
        Path path;
        try {
            // toAbsolutePath 会返回该路径的绝对路径（不解析符号链接，也不检查路径是否存在），只将相对路径转换为绝对路径
            path = Path.of(logRootPath).toAbsolutePath();
        } catch (Exception ex) {
            throw new ApplicationException(String.format("日志根路径非法 %s", ex.getMessage()), ex);
        }

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                throw new ApplicationException(String.format("创建日志根路径文件夹异常: %s", ex.getMessage()), ex);
            }
        } else {
            if (!Files.isDirectory(path)) {
                throw new ApplicationException(String.format("日志根路径不能为文件"));
            }
        }

        this.logRootAbsPath = path.toString();
    }

    /**
     * 记录笔记信息日志
     */
    public void logNoteInfo(String message, Logger originLogger) {
        writeLog(ExtraLogType.NOTE, message);
        originLogger.info(message);
    }

    /**
     * 记录笔记错误日志
     */
    public void logNoteError(String message, Logger originLogger) {
        writeLog(ExtraLogType.NOTE, message);
        originLogger.error(message);
    }

    /**
     * 记录提醒信息日志
     */
    public void logRemindInfo(String message, Logger originLogger) {
        writeLog(ExtraLogType.REMIND, message);
        originLogger.info(message);
    }

    /**
     * 记录提醒错误日志
     */
    public void logRemindError(String message, Logger originLogger) {
        writeLog(ExtraLogType.REMIND, message);
        originLogger.error(message);
    }

    /**
     * 记录短信信息日志
     */
    public void logSmsInfo(String message, Logger originLogger) {
        writeLog(ExtraLogType.SMS, message);
        originLogger.info(message);
    }

    /**
     * 记录短信错误日志
     */
    public void logSmsError(String message, Logger originLogger) {
        writeLog(ExtraLogType.SMS, message);
        originLogger.error(message);
    }

    /**
     * 拼接日志文件路径
     */
    private Path combineLogFilePath(String logFilename) {
        return Path.of(logRootAbsPath, logFilename);
    }

    /**
     * 写入日志
     */
    private void writeLog(ExtraLogType logType, String message) {
        try {
            FileWriter fileWriter = new FileWriter(combineLogFilePath(logType.getFilename()).toFile(),
                    StandardCharsets.UTF_8, true);
            fileWriter.write(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                            " " + message + "\n");
            fileWriter.close();
        } catch (Exception ex) {
            logger.error("写入日志失败: {} {}", message, ex.getMessage());
        }
    }

    /**
     * 读取日志
     */
    public ExtraLogResult readLog(ExtraLogType logType, long negativeOffset) {
        int size = 4096;
        if (negativeOffset < 0) {
            negativeOffset = 0;
        }
        try (RandomAccessFile raf = new RandomAccessFile(combineLogFilePath(logType.getFilename()).toFile(), "r")) {
            long fileLength = raf.length();
            if (fileLength <= 0) {
                throw new ApplicationException(String.format("没有日志内容"));
            }
            if(negativeOffset >= fileLength) {
                throw new ApplicationException(String.format("没有更多日志内容"));
            }
            long start = fileLength - negativeOffset - size;
            if (start < 0) {
                start = 0;
            }
            raf.seek(start);
            if (size > fileLength - negativeOffset) {
                size = (int) (fileLength - negativeOffset);
            }
            byte[] buffer = new byte[size];
            raf.readFully(buffer);

            String result = new String(buffer, StandardCharsets.UTF_8);

            // 仅当不是从文件头开始读时，去除第一行，避免返回被截断的行
            if(start != 0) {
                result = result.substring(result.indexOf("\n") + 1);
            }
            int resultLength = result.getBytes(StandardCharsets.UTF_8).length;

            //反转行顺序
            result = result.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                java.util.Collections.reverse(list);
                                return String.join("\n", list);
                            }
                    ));
            // 计算下次开始读取日志的负偏移位置
            long nextNegativeOffset = fileLength - (start + (size - resultLength));
            // System.out.println(" fileLength: " + fileLength + " nextNegativeOffset: " + nextNegativeOffset);
            return new ExtraLogResult(result, start > 0, nextNegativeOffset);
        } catch (Exception ex) {
            throw new ApplicationException(String.format("读取日志失败: %s %s", logType.getFilename(), ex.getMessage()), ex);
        }
    }
}


