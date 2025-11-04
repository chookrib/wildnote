package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证 Controller
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public Result login(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String username = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "username");
        String password = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "password");
        String accessToken = authService.login(username, password);
        return Result.okData(Map.of("accessToken", accessToken));
    }
}
