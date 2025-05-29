package cool.done.wildnotesvc.adapter.driven;

import cool.done.wildnotesvc.domain.IReminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提醒器
 */
@Component
public class Reminder implements IReminder {
    private static final Logger logger = LoggerFactory.getLogger(Reminder.class);

    @Value("${wildnote.note-remind-url:}")
    private String noteRemindUrl;

    private final String noteRemindLog;

    public Reminder(@Value("${wildnote.note-remind-log:}") String noteRemindLog) {
        this.noteRemindLog = noteRemindLog;
        if (!noteRemindLog.isEmpty()) {
            File logDir = new File(noteRemindLog).getParentFile();
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs();
            }
        }
    }

    @Override
    public void remind(String message) {

        String log = "";
        try {
            String url = noteRemindUrl + message;
            new RestTemplate().getForObject(url, String.class);
            //logger.info("笔记提醒成功: {}", message);
            log = String.format("笔记提醒成功: %s", message);
        }
        catch (Exception e) {
            //logger.error("笔记提醒失败: {}", e.getMessage());
            log = String.format("笔记提醒失败: %s", e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(noteRemindLog, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
            fileWriter.close();
        }
        catch (Exception e) {
            logger.error("记录笔记提醒日志失败: {}", e.getMessage());
        }
    }
}
