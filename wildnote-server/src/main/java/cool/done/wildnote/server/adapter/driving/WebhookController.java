package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.utility.ValueUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Webhook Controller
 */
@RestController
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final ApplicationContext applicationContext;
    private final NoteExploreService noteExploreService;
    private final NoteSettingService noteSettingService;

    public WebhookController(
            ApplicationContext applicationContext,
            NoteSettingService noteSettingService,
            NoteExploreService noteExploreService
    ) {
        this.applicationContext = applicationContext;
        this.noteSettingService = noteSettingService;
        this.noteExploreService = noteExploreService;
    }

    /**
     * 提醒 Webhook
     */
    @RequestMapping(value = "/webhook/remind/{name}", method = RequestMethod.GET)
    public Result remind(HttpServletRequest request, @PathVariable String name) {
        if (ValueUtility.isBlank(name))
            throw new ControllerException("Webhook remind 未指定路径参数 name");

        name = name.trim();
        String message = RequestValueHelper.getRequestParamStringTrimReq(request, "message");

        String value = noteSettingService.getWebhookRemind(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook remind 未配置 %s", name));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(value);
        remindGateway.remind(String.format("Webhook remind 成功: %s %s", name, message));

        return Result.ok();
    }

    /**
     * 记录 Webhook，未指定 mode 默认为 append
     */
    @RequestMapping(value = "/webhook/record/{name}", method = RequestMethod.GET)
    public Result record(HttpServletRequest request, @PathVariable String name) {
        if (ValueUtility.isBlank(name))
            throw new ControllerException("Webhook record 未指定路径参数 name");

        name = name.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "append", "mode");
        String content = RequestValueHelper.getRequestParamStringTrimReq(request, "content");

        String value = noteSettingService.getWebhookRecord(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook record 未配置 %s", name));

        String text = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()) + " " + content;

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(value, "\n\n" + text);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(value, "\n\n" + text);
        } else {
            throw new ControllerException("Webhook record mode 非法");
        }

        return Result.okData(Map.of(
                "content", content,
                "mode", mode
        ));
    }

    // /**
    //  * 网址 Webhook，未指定 mode 默认为 append
    //  */
    // @RequestMapping(value = "/webhook/url/{name}", method = RequestMethod.GET)
    // public Result url(HttpServletRequest request, @PathVariable String name) {
    //     if (ValueUtility.isBlank(name))
    //         throw new ControllerException("Webhook url 未指定路径参数 name");
    //
    //     name = name.trim();
    //     String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "append", "mode");
    //     String url = RequestValueHelper.getRequestParamStringTrimReq(request, "url");
    //
    //     String value = noteSettingService.getWebhookUrl(name);
    //     if (ValueUtility.isBlank(value))
    //         throw new ControllerException(String.format("Webhook record 未配置 %s", name));
    //
    //     String urlLowerCase = url.toLowerCase();
    //     String urlResponseBody = "";
    //     // 判断是否为合法的 url
    //     if (urlLowerCase.startsWith("https://") || urlLowerCase.startsWith("http://")) {
    //         try {
    //             java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
    //                     //.connectTimeout(java.time.Duration.ofSeconds(5))
    //                     .build();
    //
    //             java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
    //                     .uri(java.net.URI.create(url))
    //                     //.timeout(java.time.Duration.ofSeconds(8))
    //                     .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
    //                     .GET()
    //                     .build();
    //
    //             java.net.http.HttpResponse<String> response = client.send(
    //                     httpRequest,
    //                     java.net.http.HttpResponse.BodyHandlers.ofString()
    //             );
    //
    //             logger.info("Webhook url 响应: url={}, status={}, headers={}, bodyLength={}",
    //                     url, response.statusCode(), response.headers().map(), response.body() == null ? 0 : response.body().length());
    //
    //             if (response.statusCode() == 200) {
    //                 urlResponseBody = response.body()
    //                         .replaceAll("(?is)<script.*?>.*?</script>", " ")
    //                         .replaceAll("(?is)<style.*?>.*?</style>", " ")
    //                         //.replaceAll("(?is)<[^>]+>", " ")
    //                         //.replaceAll("\\s+", " ")
    //                         .trim();
    //             } else {
    //                 logger.warn("Webhook url 获取网页内容失败: {} 响应 HTTP {}", url, response.statusCode());
    //             }
    //         } catch (Exception e) {
    //             logger.warn("Webhook url 获取网页内容异常: {}", url, e);
    //         }
    //
    //         // try {
    //         //     java.net.URL targetUrl = new java.net.URL(url);
    //         //     java.net.HttpURLConnection connection = (java.net.HttpURLConnection) targetUrl.openConnection();
    //         //     connection.setRequestMethod("GET");
    //         //     connection.setConnectTimeout(5000);
    //         //     connection.setReadTimeout(8000);
    //         //     connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    //         //
    //         //     StringBuilder sb = new StringBuilder();
    //         //     try (java.io.BufferedReader reader = new java.io.BufferedReader(
    //         //             new java.io.InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
    //         //     )) {
    //         //         String line;
    //         //         while ((line = reader.readLine()) != null) {
    //         //             sb.append(line).append("\n");
    //         //         }
    //         //     }
    //         //
    //         //     urlResponseBody = sb.toString().trim();
    //         // } catch (Exception e) {
    //         //     logger.warn("Webhook url 获取网页内容异常: {}", url, e);
    //         // }
    //     }
    //
    //     // 从网页中提取标题和日期
    //
    //     // String text = "";
    //     // // 如果解析网址内容失败，则直接记录网址文本，前面加上时间戳
    //     // if (ValueUtility.isBlank(urlResponseBody)) {
    //     //     text = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()) + " " + url;
    //     // }
    //
    //     // if ("append".equalsIgnoreCase(mode)) {
    //     //     noteExploreService.appendFileContent(value, "\n\n" + text);
    //     // } else if ("insert".equalsIgnoreCase(mode)) {
    //     //     noteExploreService.insertFileContent(value, "\n\n" + text);
    //     // } else {
    //     //     throw new ControllerException("Webhook url mode 非法");
    //     // }
    //
    //     return Result.okData(Map.of(
    //             "url", url,
    //             "urlResponseBody", urlResponseBody,
    //             "mode", mode
    //     ));
    // }
}
