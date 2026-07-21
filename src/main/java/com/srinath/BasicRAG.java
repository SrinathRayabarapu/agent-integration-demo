package com.srinath;

import com.srinath.internal.AccountData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Retrieval-Augmented Generation demos over synthetic account documents.
 *
 * <ul>
 *   <li>{@code ragDocuments} — basic retrieve-and-answer via {@link QuestionAnswerAdvisor}</li>
 *   <li>{@code ragDomain} — richer pipeline via {@link RetrievalAugmentationAdvisor}
 *       with document post-processing</li>
 * </ul>
 */
@Component
public class BasicRAG {

    /** Profile {@code ragDocuments}: QuestionAnswerAdvisor over an in-memory vector store. */
    @Bean
    @Profile("ragDocuments")
    CommandLineRunner runRagDocuments(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors, EmbeddingModel embeddingModel) {
        return _ -> {
            var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
            vectorStore.add(AccountData.accountDocuments);

            var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();

            var advisors = new java.util.ArrayList<>(loggingAdvisors);
            advisors.add(qaAdvisor);

            var chatClient = builder.defaultAdvisors(advisors).build();

            var resp = chatClient.prompt().user("What is the account number of my checking account?").call().content();

            IO.println(resp);
        };
    }

    /**
     * Profile {@code ragDomain}: RetrievalAugmentationAdvisor with top-K retrieval
     * and {@link AccountData.EnrichingDocumentPostProcessor} to inject balances.
     */
    @Bean
    @Profile("ragDomain")
    CommandLineRunner runRagDomain(ChatClient.Builder builder, @Qualifier("loggingAdvisors") List<Advisor> loggingAdvisors, EmbeddingModel embeddingModel) {
        return _ -> {
            var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
            vectorStore.add(AccountData.accountDocuments);

            var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(1).build())
                    .documentPostProcessors(new AccountData.EnrichingDocumentPostProcessor())
                    .build();

            var advisors = new java.util.ArrayList<>(loggingAdvisors);
            advisors.add(ragAdvisor);

            var chatClient = builder.defaultAdvisors(advisors).build();

            var resp = chatClient.prompt().user("What is the account balance of my checking account?").call().content();

            IO.println(resp);
        };
    }

}
