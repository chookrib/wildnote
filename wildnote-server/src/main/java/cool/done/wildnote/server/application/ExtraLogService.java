package cool.done.wildnote.server.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 扩展日志 Service
 */
@Component
public class ExtraLogService {

    private static final Logger logger = LoggerFactory.getLogger(ExtraLogService.class);

    private String logFolderPath;

    private final String LOG_FILE_REMIND = "remind.log";
    private final String LOG_FILE_SMS = "sms.log";
    private final String LOG_FILE_NOTE = "note.log";
    private final String LOG_FILE_EXCEPTION = "exception.log";

    public ExtraLogService(@Value("${wildnote.log-folder-path:}") String logFolderPath) {
        this.logFolderPath = logFolderPath;
        if (!remindLogPath.isEmpty()) {
            File remindLogDir = new File(remindLogPath).getParentFile();
            if (remindLogDir != null && !remindLogDir.exists()) {
                remindLogDir.mkdirs();
            }
        }
    }

    /**
     * 记录异常日志
     */
    public void logException(String message, Logger originLogger) {
        writeLog(LOG_FILE_EXCEPTION, message);
        originLogger.error(message);
    }

    /**
     * 记录笔记信息日志
     */
    public void logNoteInfo(String message, Logger originLogger) {
        writeLog(LOG_FILE_NOTE, message);
        originLogger.info(message);
    }

    /**
     * 记录笔记错误日志
     */
    public void logNoteError(String message, Logger originLogger) {
        writeLog(LOG_FILE_NOTE, message);
        originLogger.error(message);
    }

    /**
     * 记录提醒信息日志
     */
    public void logRemindInfo(String message, Logger originLogger) {
        writeLog(LOG_FILE_REMIND, message);
        originLogger.info(message);
    }

    /**
     * 记录提醒错误日志
     */
    public void logRemindError(String message, Logger originLogger) {
        writeLog(LOG_FILE_REMIND, message);
        originLogger.error(message);
    }

    /**
     * 记录短信信息日志
     */
    public void logSmsInfo(String message, Logger originLogger) {
        writeLog(LOG_FILE_SMS, message);
        originLogger.info(message);
    }

    /**
     * 记录短信错误日志
     */
    public void logSmsError(String message, Logger originLogger) {
        writeLog(LOG_FILE_SMS, message);
        originLogger.error(message);
    }

    /**
     * 拼接日志文件路径
     */
    private String combineLogFilePath(String logFilename) {
        if (logFolderPath == null || logFolderPath.isEmpty()) {
            return logFilename;
        }
        String path = logFolderPath;
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += FileSystems.getDefault().getSeparator();
        }
        path += logFilename;
        return path;
    }

    /**
     * 写日志到文件
     */
    private void writeLog(String logFilename, String message) {
        try {
            String logFilepath = combineLogFilePath(logFilename);
            FileWriter fileWriter = new FileWriter(logFilepath, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + message + "\n");
            fileWriter.close();
        } catch (Exception e) {
            logger.error("写入日志失败: {} {}", message, e.getMessage());
        }
    }

    /**
     * 读取日志
     */
    public String readLog(String logFilename, int size) {
        try (RandomAccessFile raf = new RandomAccessFile(combineLogFilePath(logFilename), "r")) {
            long fileLength = raf.length();
            long start = Math.max(0, fileLength - size);
            raf.seek(start);
            byte[] buffer = new byte[(int) Math.min(size, fileLength)];
            raf.readFully(buffer);

            String result = new String(buffer, StandardCharsets.UTF_8);
            if (start > 0) {
                //去除被载断的行
                result = result.substring(result.indexOf("\n") + 1);
            }
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
            return result;
        } catch (Exception e) {
            logger.error("读取日志失败: {} {}", logFilename, e.getMessage());
        }
    }
}

