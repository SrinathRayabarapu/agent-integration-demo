package com.srinath;

import com.srinath.internal.AccountData;
import com.srinath.internal.AccountData.AccountSelectionQuestionHandler;
import org.springaicommunity.agent.tools.AskUserQuestionTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Human-in-the-loop demo: the model uses {@link AskUserQuestionTool} to ask which
 * account to use before answering a balance question.
 */
@Component
public class BasicHITL {

    /** Profile {@code hitl}: interactive account selection via stdin. */
    @Bean
    @Profile("hitl")
    CommandLineRunner runHITL(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors) {
        return _ -> {
            var askTool = AskUserQuestionTool.builder()
                    .questionHandler(new AccountSelectionQuestionHandler())
                    .build();

            var chatClient = builder
                    .defaultAdvisors(loggingAdvisors)
                    .defaultSystem(s -> s.text("""
                        You are a helpful banking assistant.
                        The user's accounts are:
                        {accounts}
                        Use this information to answer their questions.
                        """).param("accounts", AccountData.accountDocuments)
                    )
                    .defaultTools(askTool)
                    .build();

            var resp = chatClient.prompt().user("Get my account balance").call().content();

            IO.println(resp);
        };
    }

}
