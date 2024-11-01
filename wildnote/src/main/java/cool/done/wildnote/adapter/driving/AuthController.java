package cool.done.wildnote.adapter.driving;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.done.wildnote.domain.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证Controller
 */
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public Result login(@RequestBody String requestBody) throws JsonProcessingException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String username = json.get("username").asText();
        String password = json.get("password").asText();
        String accessToken = authService.login(username, password);
        return Result.successData(accessToken);
    }
}
