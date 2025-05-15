package cool.done.wildnotesvc.adapter.driving;

import cool.done.wildnotesvc.WildnoteSvcApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 应用信息Controller
 */
@Controller
public class DefaultController {
    /**
     * 应用信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String info() {
        return "File-Name: " + WildnoteSvcApplication.getFileName() +
                System.lineSeparator() +
                "Build-Time: " + WildnoteSvcApplication.getBuildTime() +
                System.lineSeparator();
    }

    /**
     * 用于reminder的测试
     */
    @RequestMapping(value = "/reminder", method = RequestMethod.GET)
    @ResponseBody
    public Result reminder(String message){
        System.out.println("收到reminder消息: " + message);
        return Result.success(message);
    }
}


