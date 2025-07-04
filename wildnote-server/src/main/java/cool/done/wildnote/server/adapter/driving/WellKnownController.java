package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.WildnoteServerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Well Known Controller
 */
@Controller
public class WellKnownController {
    private static final Logger logger = LoggerFactory.getLogger(WellKnownController.class);

    /**
     * 应用信息
     */
    @RequestMapping(value = "/.well-known/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String info() {
        return "File-Name: " + WildnoteServerApplication.getFileName() +
                System.lineSeparator() +
                "Build-Time: " + WildnoteServerApplication.getBuildTime() +
                System.lineSeparator();
    }

    /**
     * 测试异常
     */
    @RequestMapping(value = "/.well-known/exception", method = RequestMethod.GET)
    @ResponseBody
    public Result exception() {
        throw new RuntimeException("测试异常");
    }

    ///**
    // * 测试remind-url
    // */
    //@RequestMapping(value = "/.well-known/mock-remind-url", method = RequestMethod.GET)
    //@ResponseBody
    //public Result mockRemindUrl(String message){
    //    logger.info("模拟remind-url收到数据: message={}", message);
    //    return Result.success(message);
    //}

    ///**
    // * 测试sms-url
    // */
    //@RequestMapping(value = "/.well-known/mock-sms-url", method = RequestMethod.GET)
    //@ResponseBody
    //public Result mockSmsUrl(String mobile, String message){
    //    logger.info("模拟sms-url收到数据: mobile={}, message={}", mobile, message);
    //    return Result.success(message);
    //}
}


