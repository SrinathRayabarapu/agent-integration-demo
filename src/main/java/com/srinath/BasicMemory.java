package com.srinath;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chat memory demos: independent turns vs conversation-scoped in-memory history.
 */
@Component
public class BasicMemory {

    /**
     * Profile {@code noMemory}: two prompts without shared conversation id —
     * the model cannot recall the earlier name.
     */
    @Bean
    @Profile("noMemory")
    CommandLineRunner runNoMemory(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).build();

            var resp1 = chatClient.prompt().user("My name is Srinath").call().content();

            IO.println(resp1);

            var resp2 = chatClient.prompt().user("What is my name?").call().content();

            IO.println(resp2);
        };
    }

    /**
     * Profile {@code inMemory}: both prompts share the same {@link ChatMemory#CONVERSATION_ID}
     * so {@link MessageChatMemoryAdvisor} can recall prior turns.
     */
    @Bean
    @Profile("inMemory")
    CommandLineRunner runInMemory(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();

            var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

            var advisors = new java.util.ArrayList<>(loggingAdvisors);
            advisors.add(memoryAdvisor);

            var chatClient = builder.defaultAdvisors(advisors).build();

            // Both prompts must use the same conversation id for memory to apply.
            var resp1 = chatClient.prompt().user("My name is Srinath")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "Srinath"))
                    .call().content();

            IO.println(resp1);

            var resp2 = chatClient.prompt().user("What is my name?")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "Srinath"))
                    .call().content();

            IO.println(resp2);
        };
    }

}
