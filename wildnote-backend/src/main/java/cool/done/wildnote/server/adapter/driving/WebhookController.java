package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.OpenAIGateway;
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
    private final OpenAIGateway openAIGateway;

    // @Value("${app.article-parser-url:}")
    // private String articleParserUrl;

    @Value("${app.chrome-cdp-port:}")
    private String chromeCdpPort;

    public WebhookController(
            ApplicationContext applicationContext,
            NoteSettingService noteSettingService,
            NoteExploreService noteExploreService,
            OpenAIGateway openAIGateway
    ) {
        this.applicationContext = applicationContext;
        this.noteSettingService = noteSettingService;
        this.noteExploreService = noteExploreService;
        this.openAIGateway = openAIGateway;
    }

    /**
     * 通知 Webhook
     */
    @RequestMapping(value = "/webhook/notify/{key}", method = RequestMethod.GET)
    public Result notify(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook notify 未指定路径参数 key");

        key = key.trim();
        String message = RequestValueHelper.getRequestParamStringTrimReq(request, "message");

        String beanName = noteSettingService.getWebhookNotify(key);
        if (ValueUtility.isEmptyString(beanName))
            throw new ControllerException(String.format("Webhook notify 未配置 %s", key));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(beanName);
        remindGateway.notify(String.format("Webhook notify 成功: %s %s", key, message));

        return Result.ok();
    }

    /**
     * 保存文本 Webhook，未指定 mode 默认为 insert
     */
    @RequestMapping(value = "/webhook/save-text/{key}", method = RequestMethod.GET)
    public Result saveText(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook save-text 未指定路径参数 key");

        key = key.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "insert", "mode");
        String text = RequestValueHelper.getRequestParamStringTrimReq(request, "text");

        String path = noteSettingService.getWebhookSaveText(key);
        if (ValueUtility.isEmptyString(path))
            throw new ControllerException(String.format("Webhook save-text 未配置 %s", key));

        text = new SimpleDateFormat("*yyyyMMdd HH:mm:ss*").format(new Date()) + " " + text;

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(path, "\n\n" + text);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(path, "\n\n" + text);
        } else {
            throw new ControllerException("Webhook save-text mode 非法");
        }

        return Result.okData(Map.of(
                "text", text,
                "mode", mode
        ));
    }

    /**
     * 保存网址 Webhook，未指定 mode 默认为 insert
     */
    @RequestMapping(value = "/webhook/save-url/{key}", method = RequestMethod.GET)
    public Result saveUrl(HttpServletRequest request, @PathVariable String key) {
        if (ValueUtility.isEmptyString(key))
            throw new ControllerException("Webhook save-url 未指定路径参数 key");

        key = key.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "insert", "mode");
        String url = RequestValueHelper.getRequestParamStringTrimReq(request, "url");

        String path = noteSettingService.getWebhookSaveUrl(key);
        if (ValueUtility.isEmptyString(path))
            throw new ControllerException(String.format("Webhook save-url 未配置 %s", key));

        // 判断是否为 url
        String urlLowerCase = url.toLowerCase();
        if (!urlLowerCase.startsWith("https://") && !urlLowerCase.startsWith("http://")) {
            throw new ControllerException(String.format("Webhook save-url 指定的 url 不合法"));
        }

        // 解析结果
        String parsedDate = "";
        String parsedTitle = "";
        String parsedMessage = "";

        // 由另一个程序负责解析网页标题和日期
        // try {
        //     java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
        //             //.connectTimeout(java.time.Duration.ofSeconds(5))
        //             .build();
        //
        //     java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
        //             .uri(java.net.URI.create(articleParserUrl + "?url=" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8)))
        //             //.timeout(java.time.Duration.ofSeconds(8))
        //             //.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
        //             .GET()
        //             .build();
        //
        //     java.net.http.HttpResponse<String> response = client.send(
        //             httpRequest,
        //             java.net.http.HttpResponse.BodyHandlers.ofString()
        //     );
        //
        //     // logger.info("解析网页响应: url={}, status={}, headers={}, bodyLength={}",
        //     //         url, response.statusCode(), response.headers().map(), response.body() == null ? 0 : response.body().length());
        //
        //     if (response.statusCode() == 200) {
        //         // String responseBody = response.body()
        //         //         .replaceAll("(?is)<script.*?>.*?</script>", " ")
        //         //         .replaceAll("(?is)<style.*?>.*?</style>", " ")
        //         //         .replaceAll("(?is)<[^>]+>", " ")
        //         //         .replaceAll("\\s+", " ")
        //         //         .trim();
        //         var responseJson = JsonUtility.deserialize(response.body());
        //         parsedDate = responseJson.get("published").asText();
        //         parsedTitle = responseJson.get("title").asText();
        //     } else {
        //         parsedMessage = String.format("解析网址失败: %s %s", response.statusCode(), response.body());
        //     }
        // } catch (Exception e) {
        //     parsedMessage = String.format("解析网址异常: %s", e.getMessage());
        // }

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
        // } catch (Exception ex) {
        //     logger.warn("Webhook url 获取网页内容异常: {}", url, e);
        // }

        // 使用 Playwright 并连接到远程调试端口
        String playwrightPageContent = "";
        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();

            BrowserContext context;
            try (Browser browser = chromium.connectOverCDP("http://localhost:" + this.chromeCdpPort)) {
                // 获取默认上下文和页面
                context = browser.contexts().get(0);
                Page page = context.pages().get(0);

                // 打开指定网址
                page.navigate(url);

                // 等待页面加载完成
                page.waitForLoadState(LoadState.NETWORKIDLE);

                // String title = page.title();
                // 提取 <meta> 标签中的日期
                // String date = page.evaluate("() => { " +
                //         "const metaDate = document.querySelector('meta[property=\"article:published_time\"]'); " +
                //         "return metaDate ? metaDate.getAttribute('content') : null; " +
                //         "}");

                // This model's maximum context length is 262144 tokens. However, you requested 0 output tokens and your prompt contains at least 262145 input tokens, for a total of at least 262145 tokens. Please reduce the length of the input prompt or the number of requested output tokens. (parameter=input_tokens, value=262145)
                // 直接用网页源代码会超上下文
                // playwrightPageContent = page.content();

                // 返回用户实际看到的文本（类似浏览器中 Ctrl+A 复制的效果），自动跳过隐藏元素。
                playwrightPageContent = page.innerText("body");
                // 返回所有元素的文本节点，包括隐藏元素。如果页面有隐藏但包含重要文本的元素，可用此方法。
                // playwrightPageContent = page.textContent("body");

                if (ValueUtility.isEmptyString(playwrightPageContent)) {
                    throw new RuntimeException("使用 Chrome CDP 打开网址未获得内容");
                }
            }
        } catch (Exception ex) {
            parsedMessage = String.format("使用 Chrome CDP 打开网址异常: %s", ex.getMessage());
        }

        String llmAnswer = this.openAIGateway.chatCompletions("""
                # Role
                你是一个专业的网页内容提取专家。你的任务是从提供的 HTML 源代码中准确提取“页面标题”和“发布时间”。
                
                # Extraction Rules
                请严格遵守以下规则进行提取：
                
                1. **标题**:
                   - 优先查找 `<title>` 标签内的文本。
                   - 如果 `<title>` 包含网站名称后缀（如 " | 知乎"），请尝试去除后缀，只保留核心文章标题。
                   - 如果找不到 `<title>`，或者 `<title>` 没有当前网页内容对应的标题，则查找 `<h1>` 或带有 `class="article-title"` / `id="post-title"` 等语义化标签的内容。
                   - 输出必须为纯文本字符串，如果完全找不到发布时间，请返回空字符串。
                
                2. **发布日期**:
                   - 优先查找结构化数据标记：`<meta property="article:published_time">`、`<time datetime="...">` 或 JSON-LD 中的 `datePublished` 字段。
                   - 如果找不到结构化数据，则查找包含“发布”、“时间”、“日期”等关键词附近的文本节点。
                   - **标准化格式**：当找到的发布时间中包含年份，请返回日期 (YYYY-MM-DD)，如果找到的发布时间中没有年份，返回月日 (MM-DD)。
                   - 如果完全找不到发布时间，请返回空字符串。
                
                3. **噪声过滤**:
                   - 忽略导航栏、页脚、广告脚本和无关的元数据。
                   - 只关注正文区域的核心信息。
                
                # Output Format
                请以严格的 JSON 格式输出结果，不要包含任何 Markdown 代码块标记（如 ```json），也不要输出额外的解释性文字。JSON 结构如下：
                {
                    "title": "提取到的标题字符串或空字符串",
                    "date": "标准化后的日期字符串或空字符串"
                }
                
                # Example
                Input: <html>...<meta property="article:published_time" content="2023-10-05T14:30:00+08:00">...<title>人工智能的未来 - TechBlog</title>...</html>
                Output: {"title": "人工智能的未来", "date": "2023-10-05"}
                
                # Input Data
                以下是待分析的 HTML 源代码：
                """ + playwrightPageContent);

        JsonNode llmAnswerJson = JsonUtility.deserialize(llmAnswer);
        JsonNode llmAnswerJsonTitle = llmAnswerJson.path("title");
        if (!llmAnswerJsonTitle.isMissingNode()) {
            parsedTitle = llmAnswerJsonTitle.asText();
        }
        JsonNode llmAnswerJsonDate = llmAnswerJson.path("date");
        if (!llmAnswerJsonDate.isMissingNode()) {
            parsedDate = llmAnswerJsonDate.asText();
        }

        // 没有日期时取当前时间
        if (ValueUtility.isEmptyString(parsedDate)) {
            parsedDate = new SimpleDateFormat("*yyyyMMdd HH:mm:ss*").format(new Date());
        }
        String text = String.format("%s [%s](%s)", parsedDate, parsedTitle, url);
        if (!ValueUtility.isEmptyString(parsedMessage)) {
            text += String.format("\n> %s", parsedMessage);
        }

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(path, "\n\n" + text);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(path, "\n\n" + text);
        } else {
            throw new ControllerException("Webhook save-url mode 非法");
        }

        return Result.okData(Map.of(
                "url", url,
                "text", text,
                "mode", mode
        ));
    }
}
