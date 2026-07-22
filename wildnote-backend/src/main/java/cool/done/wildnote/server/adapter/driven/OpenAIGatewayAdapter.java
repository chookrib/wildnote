package cool.done.wildnote.server.adapter.driven;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.domain.OpenAIGateway;
import cool.done.wildnote.server.utility.JsonUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Gateway 接口 Adapter
 */
public class OpenAIGatewayAdapter implements OpenAIGateway {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIGatewayAdapter.class);

    @Value("${app.openai-base-url:}")
    private String baseUrl;

    @Value("${app.openai-api-key:}")
    private String apiKey;

    @Value("${app.openai-model:}")
    private String model;

    @Override
    public String chatCompletions(String prompt) {
        if (ValueUtility.isEmptyString(baseUrl)) {
            throw new GatewayException("app.openai-base-url 未配置");
        }
        if (ValueUtility.isEmptyString(apiKey)) {
            throw new GatewayException("app.openai-api-key 未配置");
        }
        if (ValueUtility.isEmptyString(model)) {
            throw new GatewayException("app.openai-model 未配置");
        }

        // String requestBody = "{\"model\": \"\", \"messages\": [{\"role\": \"user\", \"content\": \"\"}]}";
        String requestBody = JsonUtility.serialize(Map.of(
                "model", model,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                //.POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        // HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = null;
        try {
            response = client.send(request,
                    // HttpResponse.BodyHandlers.ofString(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            throw new GatewayException("发送请求失败", ex);
        }

        // if (response.statusCode() < 200 || response.statusCode() >= 300)
        if (response.statusCode() != 200) {
            throw new GatewayException("请求响应错误, status=" + response.statusCode());
        }

        JsonNode root = JsonUtility.deserialize(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.isNull()) {
            throw new GatewayException("请求响应内容为空");
        }

        return content.asText();
    }
}
