package cool.done.wildnotesvc.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;


/**
 * 认证Service
 */
@Service
public class AuthService {

    @Value("${wildnote.username:}")
    private String noteUsername;

    @Value("${wildnote.password:}")
    private String notePassword;

    //访问令牌
    private String accessToken;

    /**
     * 登录
     */
    public String login(String username, String password) {
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ValidationException("用户名或密码不能为空");
        }

        if(!noteUsername.equals(username) || !notePassword.equals(password)) {
            throw new ValidationException("用户名或密码错误");
        }

        //令牌不存在时创建令牌
        if(StringUtils.isEmpty(accessToken)){
            accessToken = UUID.randomUUID().toString();
        }

        return accessToken;
    }

    /**
     * 验证令牌
     */
    public boolean verifyAccessToken(String token)
    {
        return !StringUtils.isEmpty(token) && token.equals(accessToken);
    }
}
