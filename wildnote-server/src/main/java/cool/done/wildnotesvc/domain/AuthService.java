package cool.done.wildnotesvc.domain;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

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
    //private String accessToken;

    /**
     * 登录
     */
    public String login(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ValidationException("用户名或密码不能为空");
        }

        if (!noteUsername.equals(username) || !notePassword.equals(password)) {
            throw new ValidationException("用户名或密码错误");
        }

        //令牌不存在时创建令牌
        //if (StringUtils.isEmpty(accessToken)) {
        //    accessToken = UUID.randomUUID().toString();
        //}
        //return accessToken;

        return createJWT(username);
    }


    /**
     * 验证令牌
     */
    public boolean verifyAccessToken(String token) {

        //return !StringUtils.isEmpty(token) && token.equals(accessToken);

        return !StringUtils.isEmpty(token) && verifyJWT(token);
    }

    /**
     * 创建JWT令牌
     */
    private String createJWT(String username) {
        return JWT.create()
                .withIssuer("wildnote")
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L)) // 30天过期
                .sign(Algorithm.HMAC256(notePassword));
    }

    /**
     * 验证JWT令牌
     */
    public boolean verifyJWT(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            JWT.require(Algorithm.HMAC256(notePassword))
                    .withIssuer("wildnote")
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
