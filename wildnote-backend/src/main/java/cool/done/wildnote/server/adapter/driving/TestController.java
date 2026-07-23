package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.ApplicationConfig;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.OpenAIGateway;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.domain.SmsGateway;
import cool.done.wildnote.server.utility.CryptoUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 测试 Controller
 */
@Controller
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final OpenAIGateway openAIGateway;

    public TestController(
            OpenAIGateway openAIGateway
    ) {
        this.openAIGateway = openAIGateway;
    }

    /**
     * 测试异常处理
     */
    @RequestMapping(value = "/api/test/exception", method = RequestMethod.GET)
    @ResponseBody
    public Result testException() {
        throw new RuntimeException("测试异常");
    }

    /**
     * MD5 编码
     */
    @RequestMapping(value = "/api/test/crypto/md5-encode", method = RequestMethod.GET)
    @ResponseBody
    public Result testCryptoMd5Encode(HttpServletRequest request) {
        String text = RequestValueHelper.getRequestParamStringTrimReq(request, "text");
        return Result.okData(CryptoUtility.md5Encode(text));
    }

    /**
     * OpenAI 测试
     */
    @RequestMapping(value = "/api/test/openai", method = RequestMethod.GET)
    @ResponseBody
    public Result testOpenAI(HttpServletRequest request) {
        String prompt = RequestValueHelper.getRequestParamStringTrimReq(request, "prompt");
        String result = this.openAIGateway.chatCompletions(prompt);
        return Result.okData(result);
    }
}


