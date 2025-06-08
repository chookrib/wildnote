package cool.done.wildnote.server.adapter.driven;

import cool.done.wildnote.server.domain.IRemindHandler;
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
 * 提醒处理器
 */
@Component
public class RemindHandler implements IRemindHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemindHandler.class);

    @Value("${wildnote.remind-url:}")
    private String remindUrl;

    private final String remindLog;

    public RemindHandler(@Value("${wildnote.remind-log:}") String remindLog) {
        this.remindLog = remindLog;
        if (!remindLog.isEmpty()) {
            File remindLogDir = new File(remindLog).getParentFile();
            if (remindLogDir != null && !remindLogDir.exists()) {
                remindLogDir.mkdirs();
            }
        }
    }

    @Override
    public void remind(String message) {

        String log = "";
        try {
            String url = remindUrl + message;
            new RestTemplate().getForObject(url, String.class);
            //logger.info("笔记提醒成功: {}", message);
            log = String.format("笔记提醒成功: %s", message);
        }
        catch (Exception e) {
            //logger.error("笔记提醒失败: {}", e.getMessage());
            log = String.format("笔记提醒失败: %s", e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(remindLog, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
            fileWriter.close();
        }
        catch (Exception e) {
            logger.error("笔记提醒写日志失败: {} {}", log, e.getMessage());
        }
    }
}
