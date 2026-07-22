package cool.done.wildnote.server.adapter.driving;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.utility.JsonUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.article-parser-url:}")
    private String articleParserUrl;

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
    @RequestMapping(value = "/webhook/remind/{key}", method = RequestMethod.GET)
    public Result remind(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook remind 未指定路径参数 key");

        key = key.trim();
        String message = RequestValueHelper.getRequestParamStringTrimReq(request, "message");

        String beanName = noteSettingService.getWebhookRemind(key);
        if (ValueUtility.isEmptyString(beanName))
            throw new ControllerException(String.format("Webhook remind 未配置 %s", key));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(beanName);
        remindGateway.remind(String.format("Webhook remind 成功: %s %s", key, message));

        return Result.ok();
    }

    /**
     * 记录 Webhook，未指定 mode 默认为 append
     */
    @RequestMapping(value = "/webhook/record/{key}", method = RequestMethod.GET)
    public Result record(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook record 未指定路径参数 key");

        key = key.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "append", "mode");
        String content = RequestValueHelper.getRequestParamStringTrimReq(request, "content");

        String path = noteSettingService.getWebhookRecord(key);
        if (ValueUtility.isEmptyString(path))
            throw new ControllerException(String.format("Webhook record 未配置 %s", key));

        content = new SimpleDateFormat("*yyyyMMdd HH:mm:ss*").format(new Date()) + " " + content;

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(path, "\n\n" + content);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(path, "\n\n" + content);
        } else {
            throw new ControllerException("Webhook record mode 非法");
        }

        return Result.okData(Map.of(
                "content", content,
                "mode", mode
        ));
    }

    /**
     * 记录网址 Webhook，未指定 mode 默认为 append
     */
    @RequestMapping(value = "/webhook/record-url/{key}", method = RequestMethod.GET)
    public Result url(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook record-url 未指定路径参数 key");

        key = key.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "append", "mode");
        String url = RequestValueHelper.getRequestParamStringTrimReq(request, "url");

        String path = noteSettingService.getWebhookRecordUrl(key);
        if (ValueUtility.isEmptyString(path))
            throw new ControllerException(String.format("Webhook record-url 未配置 %s", key));

        // 判断是否为 url
        String urlLowerCase = url.toLowerCase();
        if (!urlLowerCase.startsWith("https://") && !urlLowerCase.startsWith("http://")) {
            throw new ControllerException(String.format("Webhook record-url 指定的 url 不合法"));
        }

        // 解析结果
        String parsedDate = "";
        String parsedTitle = "";
        String parsedMessage = "";

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    //.connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(articleParserUrl + "?url=" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8)))
                    //.timeout(java.time.Duration.ofSeconds(8))
                    //.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            // logger.info("解析网页响应: url={}, status={}, headers={}, bodyLength={}",
            //         url, response.statusCode(), response.headers().map(), response.body() == null ? 0 : response.body().length());

            if (response.statusCode() == 200) {
                // String responseBody = response.body()
                //         .replaceAll("(?is)<script.*?>.*?</script>", " ")
                //         .replaceAll("(?is)<style.*?>.*?</style>", " ")
                //         .replaceAll("(?is)<[^>]+>", " ")
                //         .replaceAll("\\s+", " ")
                //         .trim();
                var responseJson = JsonUtility.deserialize(response.body());
                parsedDate = responseJson.get("published").asText();
                parsedTitle = responseJson.get("title").asText();
            } else {
                parsedMessage = String.format("解析网址失败: %s %s", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            parsedMessage = String.format("解析网址异常: %s", e.getMessage());
        }

        // try {
        //     java.net.URL targetUrl = new java.net.URL(url);
        //     java.net.HttpURLConnection connection = (java.net.HttpURLConnection) targetUrl.openConnection();
        //     connection.setRequestMethod("GET");
        //     connection.setConnectTimeout(5000);
        //     connection.setReadTimeout(8000);
        //     connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        //
        //     StringBuilder sb = new StringBuilder();
        //     try (java.io.BufferedReader reader = new java.io.BufferedReader(
        //             new java.io.InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
        //     )) {
        //         String line;
        //         while ((line = reader.readLine()) != null) {
        //             sb.append(line).append("\n");
        //         }
        //     }
        //
        //     urlResponseBody = sb.toString().trim();
        // } catch (Exception e) {
        //     logger.warn("Webhook url 获取网页内容异常: {}", url, e);
        // }

        // 启动 Playwright 并连接到远程调试端口
        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();

            // 连接到远程调试端口
            BrowserContext context;
            try (Browser browser = chromium.connectOverCDP("http://localhost:9222")) {
                // 获取默认上下文和页面
                context = browser.contexts().get(0);
                Page page = context.pages().get(0);

                // 打开指定网址
                page.navigate(url);

                // 等待页面加载完成
                page.waitForLoadState(LoadState.NETWORKIDLE);

                // 提取页面标题
                String title = page.title();
                System.out.println("页面标题: " + title);

                // 提取页面中的日期（假设日期在某个特定元素中）
                // 这里以提取 <meta> 标签中的日期为例
                String date = page.evaluate("() => { " +
                        "const metaDate = document.querySelector('meta[property=\"article:published_time\"]'); " +
                        "return metaDate ? metaDate.getAttribute('content') : null; " +
                        "}");

                if (date != null) {
                    System.out.println("发布日期: " + date);
                } else {
                    System.out.println("未找到发布日期。");
                }
            }

        }


        if (ValueUtility.isEmptyString(parsedDate)) {
            parsedDate = new SimpleDateFormat("*yyyyMMdd HH:mm:ss*").format(new Date());
        }
        String content = String.format("%s [%s](%s)", parsedDate, parsedTitle, url);
        if (!ValueUtility.isEmptyString(parsedMessage)) {
            content += String.format("\n> %s", parsedMessage);
        }

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(path, "\n\n" + content);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(path, "\n\n" + content);
        } else {
            throw new ControllerException("Webhook url mode 非法");
        }

        return Result.okData(Map.of(
                "url", url,
                "content", content,
                "mode", mode
        ));
    }
}
