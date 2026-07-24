package cool.done.wildnote.server.application;

import cool.done.wildnote.server.utility.CryptoUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 认证 Service
 */
@Component
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${app.auth-by:}")
    private String authBy;

    @Value("${app.auth-username:}")
    private String authUsername;

    @Value("${app.auth-password:}")
    private String authPassword;

    private String authJwtSecret;
    private int authJwtExpiresMinute;

    private final NoteSettingService noteSettingService;

    public AuthService(
            @Value("${app.auth-jwt-secret:}") String jwtSecret,
            @Value("${app.auth-jwt-expires:}") String jwtExpires,
            NoteSettingService noteSettingService) {
        if (!ValueUtility.isEmptyString(jwtSecret)) {
            this.authJwtSecret = jwtSecret;
        } else {
            throw new ApplicationException("app.auth-jwt-secret 配置错误");
        }
        try {
            this.authJwtExpiresMinute = CryptoUtility.jwtExpiresMinute(jwtExpires);
        } catch (Exception ex) {
            throw new ApplicationException("app.auth-jwt-expires 配置错误");
        }
        this.noteSettingService = noteSettingService;
    }

    /**
     * 登录
     */
    public String login(String username, String password) {
        if (ValueUtility.isEmptyString(username) || ValueUtility.isEmptyString(password)) {
            throw new ApplicationException("用户名或密码不能为空");
        }

        if (this.authBy.equals("app")) {
            if (!this.authUsername.equals(username) || !this.authPassword.equals(password)) {
                throw new ApplicationException("用户名或密码错误，错误代码 101");
            }
        } else if (this.authBy.equals("setting")) {
            try {
                String settingPassword = noteSettingService.getAuthPassword(username);
                if(ValueUtility.isEmptyString(settingPassword))
                {
                    throw new ApplicationException("用户名或密码错误，错误代码 201");
                }
                if (!settingPassword.equals(CryptoUtility.md5Encode(password))) {
                    throw new ApplicationException("用户名或密码错误，错误代码 202");
                }
            } catch (Exception ex) {
                // logger.info("从用户配置文件中获取认证信息异常: {}", ex.getMessage());
                throw new ApplicationException("用户名或密码错误，错误代码 203");
            }
        }
        else {
            throw new ApplicationException("用户名或密码错误，错误代码 301");
        }

        return CryptoUtility.jwtEncode(
                Map.of("username", username),
                this.authJwtSecret,
                LocalDateTime.now().plusMinutes(this.authJwtExpiresMinute)
        );
    }

    /**
     * 验证 AccessToken
     */
    public boolean verifyAccessToken(String accessToken) {
        if (ValueUtility.isEmptyString(accessToken)) {
            return false;
        }

        try {
            Map<String, ?> payload = CryptoUtility.jwtDecode(accessToken, this.authJwtSecret);
            // return Objects.toString(payload.get("username"), "").equals(this.authUsername);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
