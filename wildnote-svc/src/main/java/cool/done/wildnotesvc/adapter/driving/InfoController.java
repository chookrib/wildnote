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
public class InfoController {
    /**
     * 应用信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String  info() {
        StringBuilder sb = new StringBuilder();
        sb.append("File-Name: ").append(WildnoteSvcApplication.getFileName());
        sb.append(System.lineSeparator());
        sb.append("Build-Time: ").append(WildnoteSvcApplication.getBuildTime());
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}


