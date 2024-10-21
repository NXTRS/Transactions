package com.example.transactionservice;

import com.example.transactionservice.model.TransactionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TransactionsIntegrationTests extends BaseTest {
    private static final String USER_1 = "asd123-user1";
    private static final String USER_2 =  "asd123-user2";
    private static final String API_PATH = "api/transactions";
    @Autowired
    WebTestClient client;

    @Test
    void testCreateTransaction_shouldSendNotificationSuccessfully() {
        client.post()
                .uri(builder -> builder.path("api/transactions").build())
                .bodyValue(TransactionDto.builder().userId(USER_1).amount(101d).build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody().consumeWith(response -> {
                    var body = response.getResponseBodyContent();
                    assertNotNull(body);
                    assertThatJson(new String(body)).isEqualTo(readFile("/responses/createTransactionWithNotification.json"));
                });

        verify(kafkaConsumer, timeout(5000)).listener(eventCaptor.capture());
        assertThatJson(eventCaptor.getValue().value()).isEqualTo(readFile("/responses/kafkaMessages/transaction.json"));
    }

    @Test
    void testCreateTransaction_shouldSucceedWithoutNotification() {
        client.post()
                .uri(builder -> builder.path("api/transactions").build())
                .bodyValue(TransactionDto.builder().userId(USER_1).amount(99d).build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody().consumeWith(response -> {
                    var body = response.getResponseBodyContent();
                    assertNotNull(body);
                    assertThatJson(new String(body)).isEqualTo(readFile("/responses/createTransactionWithoutNotification.json"));
                });

        verify(kafkaConsumer, never()).listener(eventCaptor.capture());
    }

    @ParameterizedTest
    @MethodSource("generateInvalidTransactionFields")
    void testCreateTransaction_shouldFail(String userId, Double amount) {
        client.post()
                .uri(builder -> builder.path(API_PATH).build())
                .bodyValue(TransactionDto.builder().userId(userId).amount(amount).build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("generateValidTransactionRequestParams")
    @Sql("/scripts/init-transactions.sql")
    void testGetTransactions_shouldSucceed(String userId, int page, int size, String expectedFilename) {
        client.get()
                .uri(builder -> builder.path(API_PATH)
                        .queryParam("userId", userId)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(response -> {
                    var body = response.getResponseBodyContent();
                    assertNotNull(body);
                    assertThatJson(new String(body)).isEqualTo(readFile(expectedFilename));
                });
    }

    @Test
    void testGetTransactions_noItemsshouldSucceed() {
        client.get()
                .uri(builder -> builder.path(API_PATH)
                        .queryParam("userId", USER_1)
                        .queryParam("page", 0)
                        .queryParam("size", 50)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(response -> {
                    var body = response.getResponseBodyContent();
                    assertNotNull(body);
                    assertThatJson(new String(body)).isEqualTo(readFile("/responses/emptyResponse.json"));
                });
    }

    private static Stream<Arguments> generateValidTransactionRequestParams() {
        return Stream.of(
                Arguments.of(USER_1, 0, 5, "/responses/transactionsUser1FirstPage.json"),
                Arguments.of(USER_1, 1, 5, "/responses/transactionsUser1SecondPage.json"),
                Arguments.of(USER_1, 2, 5, "/responses/transactionsUser1ThirdPage.json"),
                Arguments.of(USER_1, 0, 13, "/responses/transactionsUser1SinglePage.json"),
                Arguments.of(USER_2, 0, 10, "/responses/transactionsUser2FirstPage.json"),
                Arguments.of(USER_2, 1, 10, "/responses/transactionsUser2SecondPage.json")
        );
    }

    private static Stream<Arguments> generateInvalidTransactionFields() {
        return Stream.of(
                Arguments.of(null, 22d),
                Arguments.of("", 22d),
                Arguments.of("asd123-user1", null),
                Arguments.of(null, null)
        );
    }

}
