package cool.done.wildnotesvc.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnotesvc.common.util.JacksonUtils;
import cool.done.wildnotesvc.domain.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证Controller
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
        JsonNode json = JacksonUtils.readTree(requestBody);
        String username = json.path("username").asText();
        String password = json.path("password").asText();
        String accessToken = authService.login(username, password);
        return Result.successData(accessToken);
    }
}
