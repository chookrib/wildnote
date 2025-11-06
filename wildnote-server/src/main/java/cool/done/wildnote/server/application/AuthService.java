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

    @Value("${wildnote.auth-username:}")
    private String authUsername;

    @Value("${wildnote.auth-password:}")
    private String authPassword;

    private String authJwtSecret;
    private int authJwtExpiresMinute;

    private final NoteSettingService noteSettingService;

    public AuthService(
            @Value("${wildnote.auth-jwt-secret:}") String jwtSecret,
            @Value("${wildnote.auth-jwt-expires:}") String jwtExpires,
            NoteSettingService noteSettingService) {
        if (!ValueUtility.isBlank(jwtSecret)) {
            this.authJwtSecret = jwtSecret;
        } else {
            throw new ApplicationException("wildnote.auth-jwt-secret 配置错误");
        }
        try {
            this.authJwtExpiresMinute = CryptoUtility.jwtExpiresMinute(jwtExpires);
        } catch (Exception ex) {
            throw new ApplicationException("wildnote.auth-jwt-expires 配置错误");
        }
        this.noteSettingService = noteSettingService;
    }

    /**
     * 登录
     */
    public String login(String username, String password) {
        if (ValueUtility.isBlank(username) || ValueUtility.isBlank(password)) {
            throw new ApplicationException("用户名或密码不能为空");
        }

        boolean authSuccess = false;

        // 先从环境配置文件中获取认证信息
        if (this.authUsername.equals(username) && this.authPassword.equals(password)) {
            authSuccess = true;
        }

        if (!authSuccess) {
            // 再从笔记配置文件中获取认证信息
            try {
                String settingPassword = noteSettingService.getAuthPassword(username);
                if (
                        !ValueUtility.isBlank(settingPassword) &&
                                settingPassword.equals(CryptoUtility.md5Encode(password))
                    //settingPassword.equals(password)
                ) {
                    authSuccess = true;
                }
            } catch (Exception ex) {
                logger.info("从配置文件中获取认证信息异常: {}", ex.getMessage());
            }
        }

        if (!authSuccess) {
            throw new ApplicationException("用户名或密码错误");
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
        if (ValueUtility.isBlank(accessToken)) {
            return false;
        }

        try {
            Map<String, ?> payload = CryptoUtility.jwtDecode(accessToken, this.authJwtSecret);
            //return Objects.toString(payload.get("username"), "").equals(this.authUsername);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
