package com.srinath;

import com.srinath.internal.MyLoggingAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot entry point for the agent integration demos.
 *
 * <p>Each demo is activated by a Spring profile (for example {@code systemPrompt}
 * or {@code ragDocuments}). Append {@code debug} to enable request/response logging
 * via {@link MyLoggingAdvisor}.
 */
@SpringBootApplication
public class Application {

    /**
     * Advisors used when the {@code debug} profile is active: tool-call history plus
     * pretty-printed request/response logging.
     */
    @Bean("loggingAdvisors")
    @Profile("debug")
    List<Advisor> debugLoggingAdvisor() {
        return List.of(
                ToolCallingAdvisor.builder()
                        .conversationHistoryEnabled(true)
                        .build(),
                new MyLoggingAdvisor()
        );
    }

    /**
     * No-op advisors when {@code debug} is not active.
     */
    @Bean("loggingAdvisors")
    @Profile("!debug")
    List<Advisor> noopLoggingAdvisor() {
        return new ArrayList<>();
    }

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
