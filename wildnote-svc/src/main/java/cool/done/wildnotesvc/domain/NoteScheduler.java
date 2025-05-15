package cool.done.wildnotesvc.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 笔记定时器
 */
@Component
public class NoteScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NoteScheduler.class);

    private final NoteService noteService;

    public NoteScheduler(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 应用启动后运行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        noteService.reloadNote();
        noteService.startWatch();
    }
}
