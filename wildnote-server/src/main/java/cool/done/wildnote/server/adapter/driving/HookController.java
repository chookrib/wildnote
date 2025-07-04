package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.domain.IRemindHandler;
import cool.done.wildnote.server.domain.NoteService;
import cool.done.wildnote.server.domain.SiteConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 钩子Controller
 */
@RestController
public class HookController {
    private static final Logger logger = LoggerFactory.getLogger(HookController.class);

    private final NoteService noteService;
    private final SiteConfigService siteConfigService;
    private final IRemindHandler remindHandler;

    public HookController(NoteService noteService, SiteConfigService siteConfigService, IRemindHandler remindHandler) {
        this.noteService = noteService;
        this.siteConfigService = siteConfigService;
        this.remindHandler = remindHandler;
    }

    /**
     * 提醒钩子
     */
    @RequestMapping(value = "/hook/remind/{name}", method = RequestMethod.GET)
    public Result remind(@PathVariable String name, @RequestParam String message) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(message))
            return Result.error(ResultCodes.ERROR_DEFAULT);

        logger.info(siteConfigService.getHookRemindName());

        if (!name.equals(siteConfigService.getHookRemindName()))
            return Result.error(ResultCodes.ERROR_DEFAULT);

        remindHandler.remind(String.format("HOOK %s %s", name, message));

        return Result.success();
    }

    /**
     * 记录钩子
     */
    @RequestMapping(value = "/hook/record/{name}", method = RequestMethod.GET)
    public Result record(@PathVariable String name, @RequestParam String content) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(content))
            return Result.error(ResultCodes.ERROR_DEFAULT);

        String note = siteConfigService.getHookRecordNote(name);
        if(StringUtils.isEmpty(note))
            return Result.error(ResultCodes.ERROR_DEFAULT);

        //File file = new File(note);
        //if (!file.exists())
        //    return Result.error(ResultCodes.ERROR_DEFAULT);

        Path notePath = noteService.combineAbsPath(note);
        try (FileWriter writer = new FileWriter(notePath.toFile(), true)) {
            writer.write(String.format("\n\n%s %s",
                    new SimpleDateFormat("yyyyMMdd").format(new Date()),
                    content));
        } catch (IOException e) {
            return Result.error(ResultCodes.ERROR_DEFAULT);
        }

        return Result.success();
    }
}
