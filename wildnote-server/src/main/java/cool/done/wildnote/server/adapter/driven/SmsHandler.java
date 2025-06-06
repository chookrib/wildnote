package cool.done.wildnote.server.adapter.driven;

import cool.done.wildnote.server.domain.ISmsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 短信处理器
 */
public class SmsHandler implements ISmsHandler {
    private static final Logger logger = LoggerFactory.getLogger(SmsHandler.class);

    @Value("${wildnote.sms-url:}")
    private String smsUrl;

    private final String smsLog;

    public SmsHandler(@Value("${wildnote.sms-log:}") String smsLog) {
        this.smsLog = smsLog;
        if (!smsLog.isEmpty()) {
            File smsLogDir = new File(smsLog).getParentFile();
            if (smsLogDir != null && !smsLogDir.exists()) {
                smsLogDir.mkdirs();
            }
        }
    }

    @Override
    public void send(String mobile, String message) {

        String log = "";
        try {
            String url = smsUrl.replace("<mobile>", mobile).replace("<message>", message);
            new RestTemplate().getForObject(url, String.class);
            log = String.format("短信发送成功: %s", message);
        }
        catch (Exception e) {
            log = String.format("短信发送失败: %s", e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(smsLog, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
            fileWriter.close();
        }
        catch (Exception e) {
            logger.error("短信发送写日志失败: {}", e.getMessage());
        }
    }
}
