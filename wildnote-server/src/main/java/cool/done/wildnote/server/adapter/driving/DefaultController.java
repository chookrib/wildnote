package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.WildnoteServerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 应用信息Controller
 */
@Controller
public class DefaultController {
    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);

    /**
     * 应用信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String info() {
        return "File-Name: " + WildnoteServerApplication.getFileName() +
                System.lineSeparator() +
                "Build-Time: " + WildnoteServerApplication.getBuildTime() +
                System.lineSeparator();
    }

    /**
     * reminder测试
     */
    @RequestMapping(value = "/reminder", method = RequestMethod.GET)
    @ResponseBody
    public Result reminder(String message){
        logger.info("Reminder收到消息: {}", message);
        return Result.success(message);
    }
}


