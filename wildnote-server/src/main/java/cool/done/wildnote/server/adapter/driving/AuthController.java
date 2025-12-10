package cool.done.wildnote.server.adapter.driving;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cool.done.wildnote.server.application.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证 Controller
 */
@RestController
public class AuthController {

    private static final Cache<String, String> captchaCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 生成一个在白色背景上可读的随机颜色
     */
    private Color getRandomReadableColor() {
        int r, g, b;
        do {
            r = (int) (Math.random() * 256);
            g = (int) (Math.random() * 256);
            b = (int) (Math.random() * 256);
            // 保证颜色与白色有足够对比度
        } while ((r + g + b) > 600); // 白色为255+255+255=765，600以下较易分辨
        return new Color(r, g, b);
    }

    /**
     * 生成验证码图片
     */
    @RequestMapping(value = "/api/captcha", method = RequestMethod.GET)
    public ResponseEntity<byte[]> captcha(HttpServletRequest request) throws IOException {
        String fingerprint = RequestValueHelper.getRequestParamStringTrimReq(request, "f");

        // String code = String.valueOf((int)((Math.random() * 9 + 1) * 1000));
        // String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";  //去除易混淆字符01IlOo
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int idx = (int) (Math.random() * chars.length());
            codeBuilder.append(chars.charAt(idx));
        }
        String code = codeBuilder.toString();

        captchaCache.put(fingerprint, code);

        BufferedImage image = new BufferedImage(code.length() * 12 + 8 + 8, 24, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 24);

        // g.setFont(new Font("Arial", Font.BOLD, 20));
        // String preferred = "Arial";
        // String fontFamily = java.util.Arrays.asList(
        //        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        // ).contains(preferred) ? preferred : Font.SANS_SERIF;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

        // 添加杂线
        for (int i = 0; i < 5; i++) {
            int x1 = (int) (Math.random() * image.getWidth());
            int y1 = (int) (Math.random() * image.getHeight());
            int x2 = (int) (Math.random() * image.getWidth());
            int y2 = (int) (Math.random() * image.getHeight());
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x1, y1, x2, y2);
        }

        // 添加验证码
        // g.setColor(Color.BLACK);
        g.setColor(getRandomReadableColor());
        int x = (image.getWidth() - g.getFontMetrics().stringWidth(code)) / 2;
        int y = (image.getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
        g.drawString(code, x, y);

        // 添加杂点
        for (int i = 0; i < 20; i++) {
            int px = (int) (Math.random() * image.getWidth());
            int py = (int) (Math.random() * image.getHeight());
            int pr = (int) (Math.random() * 256);
            int pg = (int) (Math.random() * 256);
            int pb = (int) (Math.random() * 256);
            image.setRGB(px, py, new Color(pr, pg, pb).getRGB());
        }

        // 添加边框
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public Result login(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String fingerprint = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "f");
        String captcha = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "captcha");
        String username = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "username");
        String password = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "password");

        String code = captchaCache.getIfPresent(fingerprint);
        if (code == null) {
            throw new ControllerException("验证码已过期");
        }
        if (!captcha.equalsIgnoreCase(code)) {
            throw new ControllerException("验证码错误");
        }
        String accessToken = authService.login(username, password);
        return Result.okData(Map.of("accessToken", accessToken));
    }
}
