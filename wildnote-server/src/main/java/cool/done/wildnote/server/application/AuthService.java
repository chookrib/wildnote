package cool.done.wildnote.server.application;

import com.auth0.jwt.interfaces.Claim;
import cool.done.wildnote.server.utility.CryptoUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
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

    @Value("${wildnote.auth-jwt-secret:}")
    private String authJwtSecret;

    private final NoteSettingService noteSettingService;

    public AuthService(NoteSettingService noteSettingService) {
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

        if(!authSuccess) {
            // 再从笔记配置文件中获取认证信息
            try {
                String encryptedPassword = noteSettingService.getAuthEncryptedPassword(username);
                if (
                        !ValueUtility.isBlank(encryptedPassword) &&
                        //encryptedPassword.equals(CryptoUtility.encodeMd5(password))
                        encryptedPassword.equals(password)
                ) {
                    authSuccess = true;
                }
            } catch (Exception e) {
                logger.info("从配置文件中获取认证信息异常: {}", e.getMessage());
            }
        }

        if (!authSuccess) {
            throw new ApplicationException("用户名或密码错误");
        }

        return CryptoUtility.encodeJwt(
                Map.of("username", username),
                new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L),   // 30天过期
                this.authJwtSecret
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
            Map<String, Claim> payload = CryptoUtility.decodeJwt(accessToken, this.authJwtSecret);
            // return payload.get("username").asString().equals(this.authUsername);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
