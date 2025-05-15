package cool.done.wildnotesvc.adapter.driven;

import cool.done.wildnotesvc.domain.IReminder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 提醒
 */
@Component
public class Reminder implements IReminder {

    @Value("${wildnote.reminder-url:}")
    private String reminderUrl;


    @Override
    public void remind(String message) {
        String url = reminderUrl + message;
        new RestTemplate().getForObject(url, String.class);
    }
}
