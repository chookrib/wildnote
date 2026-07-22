package cool.done.wildnote.server.domain;

/**
 * OpenAI Gateway 接口
 */
public interface OpenAIGateway {
    /**
     * LLM 对话
     */
    String chatCompletions(String prompt);
}
