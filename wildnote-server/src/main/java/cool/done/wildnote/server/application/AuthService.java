package cool.done.wildnote.server.application;

import com.auth0.jwt.interfaces.Claim;
import cool.done.wildnote.server.utility.CryptoUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 认证 Service
 */
@Component
public class AuthService {

    //private final SettingService settingService;

    @Value("${wildnote.username:}")
    private String noteUsername;

    @Value("${wildnote.password:}")
    private String notePassword;

    //访问令牌
    //private String accessToken;

    //public AuthService(SettingService settingService) {
    //    this.settingService = settingService;
    //}

    /**
     * 登录
     */
    public String login(String username, String password) {
        if (ValueUtility.isBlank(username) || ValueUtility.isBlank(password)) {
            throw new ApplicationException("用户名或密码不能为空");
        }

        if (!noteUsername.equals(username) || !notePassword.equals(password)) {
            throw new ApplicationException("用户名或密码错误");
        }

        //if (!settingService.auth(username, password)) {
        //    throw new ApplicationException("用户名或密码错误");
        //}

        //令牌不存在时创建令牌
        //if (StringUtils.isEmpty(accessToken)) {
        //    accessToken = UUID.randomUUID().toString();
        //}
        //return accessToken;

        return CryptoUtility.encodeJwt(
                Map.of("username", noteUsername),
                new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L),   // 30天过期
                notePassword
        );
    }


    /**
     * 验证 AccessToken
     */
    public boolean verifyAccessToken(String accessToken) {
        if(ValueUtility.isBlank(accessToken)) {
            return false;
        }

        try {
            Map<String, Claim> payload = CryptoUtility.decodeJwt(accessToken, notePassword);
            return payload.get("username").asString().equals(noteUsername);
        }
        catch (Exception e) {
            return false;
        }
    }
}
