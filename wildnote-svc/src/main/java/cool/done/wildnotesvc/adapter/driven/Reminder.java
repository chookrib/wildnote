package cool.done.wildnotesvc.adapter.driven;

import cool.done.wildnotesvc.domain.IReminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 提醒
 */
@Component
public class Reminder implements IReminder {
    private static final Logger logger = LoggerFactory.getLogger(Reminder.class);

    @Value("${wildnote.reminder-url:}")
    private String reminderUrl;


    @Override
    public void remind(String message) {
        try {
            String url = reminderUrl + message;
            new RestTemplate().getForObject(url, String.class);
            logger.info("笔记提醒成功: {}", message);
        }
        catch (Exception e) {
            logger.error("笔记提醒失败: {}", e.getMessage());
        }
    }
}
