package com.srinath;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Foundational ChatClient demos: plain chat, structured output, streaming,
 * system prompts, and chat without tools.
 */
@Component
public class BasicChat {

    /** Profile {@code default}: single-turn chat. */
    @Bean
    @Profile("default")
    CommandLineRunner runDefault(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).build();

            var resp = chatClient.prompt().user("say hello").call().content();
            IO.println(resp);
        };
    }

    /**
     * Profile {@code structuredOutput}: map the model response to typed Java records.
     *
     * <p>Uses a wrapper record (not a top-level JSON array) plus provider-native schema
     * constraints and validation retries so local models behave more reliably.
     */
    @Bean
    @Profile("structuredOutput")
    CommandLineRunner runStructuredOutput(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {

        record City(
                String name,
                Integer population,
                @JsonPropertyDescription("Most popular food")
                String food) {}

        // Wrapper avoids top-level JSON array schemas, which many local models mishandle
        // (e.g. returning ["Tokyo", ...] instead of [{name, population, food}, ...]).
        record Cities(List<City> cities) {}

        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).build();

            var cities = chatClient.prompt()
                    .user("""
                            Return 10 big cities. Each city must include name, population, and most popular food.
                            """)
                    .call()
                    .entity(Cities.class, spec -> spec
                            .useProviderStructuredOutput()
                            .validateSchema())
                    .cities();

            cities.forEach(city ->
                IO.println(city.name() + " has " + city.population() + " people and likes " + city.food())
            );
        };
    }

    /** Profile {@code streaming}: stream tokens to stdout. */
    @Bean
    @Profile("streaming")
    CommandLineRunner runStreaming(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).build();
            chatClient.prompt().user("write a short story about the java programming language").stream().content()
                    .doOnNext(IO::print)
                    .blockLast();
        };
    }

    /** Profile {@code systemPrompt}: apply a system message that shapes the persona. */
    @Bean
    @Profile("systemPrompt")
    CommandLineRunner runSystemPrompt(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).defaultSystem("You are a wookie").build();
            var resp = chatClient.prompt().user("say hello").call().content();
            IO.println(resp);
        };
    }

    /**
     * Profile {@code noTools}: ask a time question without registering tools,
     * so the model answers from knowledge only.
     */
    @Bean
    @Profile("noTools")
    CommandLineRunner runNoTools(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var chatClient = builder.defaultAdvisors(loggingAdvisors).build();

            var resp = chatClient.prompt().user("what time is it?").call().content();

            IO.println(resp);
        };
    }
}
