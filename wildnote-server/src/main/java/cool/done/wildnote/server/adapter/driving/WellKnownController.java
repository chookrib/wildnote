package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.Application;

import cool.done.wildnote.server.utility.CryptoUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

/**
 * Well Known Controller
 */
@Controller
public class WellKnownController {

    private static final Logger logger = LoggerFactory.getLogger(WellKnownController.class);

    /**
     * 应用信息，显示非涉密信息
     */
    @RequestMapping(value = "/.well-known/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String info() {
        Properties props = Application.getManifestProperties();
        return "File-Name: " + Application.getFileName() +
               System.lineSeparator() +
               "Build-Time: " + props.getProperty("Build-Time", "") +
               System.lineSeparator() +
               "Git-Commit-Id-Abbrev: " + props.getProperty("Git-Commit-Id-Abbrev", "") +
               System.lineSeparator();
    }

    /**
     * 测试异常处理
     */
    @RequestMapping(value = "/.well-known/test/exception", method = RequestMethod.GET)
    @ResponseBody
    public Result testException() {
        throw new RuntimeException("测试异常");
    }

    /**
     * MD5 编码
     */
    @RequestMapping(value = "/.well-known/test/crypto/md5-encode", method = RequestMethod.GET)
    @ResponseBody
    public Result testMd5(HttpServletRequest request) {
        String text = RequestValueHelper.getRequestParamStringTrimReq(request,"text");
        return Result.okData(CryptoUtility.md5Encode(text));
    }
}


