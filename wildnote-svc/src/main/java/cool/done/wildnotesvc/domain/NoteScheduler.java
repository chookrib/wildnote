package cool.done.wildnotesvc.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

/**
 * 笔记定时器
 */
@Component
public class NoteScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NoteScheduler.class);

    private final NoteService noteService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    public NoteScheduler(NoteService noteService, ApplicationEventPublisher applicationEventPublisher,
                         TaskScheduler taskScheduler) {
        this.noteService = noteService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 应用启动后运行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        noteService.watchNoteChange();

        //watchNoteChange();
        //startNoteScheduler();
    }

    /**
     * 监控笔记目录变化
     */
    private void watchNoteChange() {

    }

    /**
     * 开始监控笔记
     */
    private void refreshRoutes() {
        //boolean changed = apiRouteService.loadApiRoutes();
        //if (changed) {
        //    //通知路由缓存刷新
        //    logger.info("路由缓存需更新，发布RefreshRoutesEvent事件");
        //    applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        //}
    }

    /**
     * 启动定时器
     */
    private void startNoteScheduler() {
        //Long l = null;
        //try {
        //    l = Long.valueOf(fixedRate);
        //} catch (NumberFormatException ignored) {
        //}
        //
        //if (l != null && l > 0) {
        //    Long fl = l;
        //    scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {
        //                logger.info("路由刷新定时器启动，间隔{}秒", fl);
        //                refreshRoutes();
        //            },
        //            Duration.of(l, ChronoUnit.SECONDS));
        //} else
        //    logger.info("路由刷新定时器未启动，未配置间隔时间或间隔时间不正确");
    }

    /**
     * 取消定时器
     */
    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    ///**
    // * 定期执行
    // */
    //@Scheduled(fixedRateString = "${scheduler.fixed-rate:60000}")
    //public void run() {
    //    logger.info("路由刷新定时器运行");
    //
    //    boolean changed = apiRouteService.loadApiRoutes();
    //    if (changed) {
    //        //通知路由缓存刷新
    //        logger.info("发布路由刷新事件RefreshRoutesEvent");
    //        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    //    }
    //}
}
