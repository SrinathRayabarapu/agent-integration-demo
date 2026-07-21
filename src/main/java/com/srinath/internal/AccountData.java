package com.srinath.internal;

import org.springaicommunity.agent.tools.AskUserQuestionTool.Question;
import org.springaicommunity.agent.tools.AskUserQuestionTool.QuestionHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Synthetic banking fixtures used by RAG and human-in-the-loop demos.
 *
 * <p><strong>Not real account data.</strong> Values are hard-coded for local examples only.
 */
public final class AccountData {

    private AccountData() {
    }

    record Account(String id, String name, String accountNumber, BigDecimal balance) {
        String toDetailString() {
            return "name=%s, accountNumber=%s, balance=$%,.2f".formatted(name, accountNumber, balance);
        }
    }

    static final List<Account> accounts = List.of(
            new Account("1", "Checking", "1234567890", new BigDecimal("1000")),
            new Account("2", "Savings", "999999999", new BigDecimal("500")),
            new Account("3", "Credit Card", "4321654398761234", new BigDecimal("2000"))
    );

    static java.util.Optional<Account> getAccountById(String id) {
        return accounts.stream()
                .filter(account -> account.id().equals(id))
                .findFirst();
    }

    /** Documents indexed for RAG (name + account number; balances added by post-processing). */
    public static final List<Document> accountDocuments = accounts.stream()
            .map(account -> new Document(
                    account.id(),
                    "name=%s, accountNumber=%s".formatted(account.name(), account.accountNumber()),
                    new HashMap<>()))
            .toList();

    /**
     * Interactive handler that prompts the user (stdin) to pick an account for HITL demos.
     */
    public static class AccountSelectionQuestionHandler implements QuestionHandler {

        @Override
        public Map<String, String> handle(List<Question> questions) {
            Map<String, String> answers = new HashMap<>();
            Scanner scanner = new Scanner(System.in);

            for (Question q : questions) {
                System.out.println("\n" + q.header() + ": " + q.question());

                for (int i = 0; i < accounts.size(); i++) {
                    Account account = accounts.get(i);
                    System.out.printf("  %d. %s (%s)%n", i + 1, account.name(), account.accountNumber());
                }

                System.out.println("  (Enter a number to select an account)");

                String response = scanner.nextLine().trim();
                try {
                    int index = Integer.parseInt(response) - 1;
                    if (index >= 0 && index < accounts.size()) {
                        String accountId = accounts.get(index).id();
                        answers.put(q.question(), getAccountById(accountId)
                                .map(Account::toDetailString)
                                .orElse(response));
                    } else {
                        answers.put(q.question(), response);
                    }
                } catch (NumberFormatException e) {
                    answers.put(q.question(), response);
                }
            }

            return answers;
        }
    }

    /**
     * Enriches retrieved account documents with balance details for domain RAG.
     */
    public static class EnrichingDocumentPostProcessor implements DocumentPostProcessor {

        @Override
        public List<Document> process(Query query, List<Document> documents) {
            return documents.stream()
                    .map(document -> getAccountById(document.getId())
                            .map(account -> new Document(account.id(), account.toDetailString(), new HashMap<>()))
                            .orElse(document))
                    .toList();
        }
    }

}
