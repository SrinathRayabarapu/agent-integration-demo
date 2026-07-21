package com.srinath.internal;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.ai.util.JsonHelper;
import tools.jackson.databind.SerializationFeature;

/**
 * Debug advisor that pretty-prints ChatClient requests and responses to stdout.
 *
 * <p>Activated only when the {@code debug} Spring profile is enabled
 * (for example {@code ./gradlew systemPromptDebug}).
 */
public class MyLoggingAdvisor implements BaseAdvisor {

    // Spring AI 2.0 uses JsonHelper instead of ModelOptionsUtils#toJsonString(PrettyPrinter).
    private static final JsonHelper JSON = new JsonHelper(
            JacksonUtils.getDefaultJsonMapper()
                    .rebuild()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .build());

    @Override
    public int getOrder() {
        return 999999999;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        var out = JSON.toJson(request.prompt());
        System.out.println("\nREQUEST:\n" + out);

        if (request.prompt().getOptions() instanceof ToolCallingChatOptions toolOptions) {
            var toolCallbacks = toolOptions.getToolCallbacks();
            if (toolCallbacks != null) {
                var tools = toolCallbacks.stream().map(tc -> tc.getToolDefinition().name()).toList();
                if (!tools.isEmpty()) {
                    IO.println("\nTOOLS: " + JSON.toJson(tools));
                }
            }
        }

        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        var out = JSON.toJson(response.chatResponse());
        System.out.println("\nRESPONSE:\n" + out);
        return response;
    }

}
