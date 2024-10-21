package com.example.transactionservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;

import static com.example.transactionservice.config.AuthenticationTestUtils.createJWTToken;
import static com.example.transactionservice.config.AuthenticationTestUtils.mockOauth2JwksEndpoint;
import static io.jsonwebtoken.impl.crypto.RsaProvider.generateKeyPair;
import static java.util.Objects.requireNonNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"spring.config.additional-location=classpath:application-test.yml"})
@MockServerTest({"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:${mockServerPort}/realms/NotificationRealm/protocol/openid-connect/certs"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected String token;
    protected String expiredToken;
    private KeyPair keyPair;
    protected final String USER_1 = "testuser1";
    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
    @Autowired
    protected DataSource dataSource;
    private MockServerClient client;
    @SpyBean
    protected KafkaTestConsumer kafkaConsumer;

    static {
        kafka.start();
    }

    // needed to read kafka events published during a test
    @Captor
    ArgumentCaptor<ConsumerRecord<String, String>> eventCaptor;

    /**
     * Dynamically replace the application configured kafka bootstrap servers
     * with the test container defined bootstrap servers
     */
    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    /**
     * Before running the tests, a mock token is created that will be valid for 1 minute
     * An expired token is also created for negative tests
     */
    @BeforeAll
    void before() {
        keyPair = generateKeyPair(2048);
        token = createJWTToken(keyPair.getPrivate(), USER_1, new Date(System.currentTimeMillis() + 60 * 1000));
        expiredToken = createJWTToken(keyPair.getPrivate(), USER_1, new Date(System.currentTimeMillis() - 1));
    }

    @BeforeEach
    void setUp() {
        mockOauth2JwksEndpoint(keyPair, client);
    }

    /**
     * Clean the db after each test, custom script to also restart identity
     */
    @AfterEach
    public void afterEach() {
        var dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ClassPathResource("/scripts/tear-down.sql"));
        dbPopulator.execute(this.dataSource);
    }

    protected String readFile(String filename) {
        try {
            return IOUtils.toString(
                    requireNonNull(this.getClass().getResourceAsStream(filename)), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
