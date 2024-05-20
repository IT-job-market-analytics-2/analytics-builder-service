package ru.borshchevskiy.analyticsbuilderservice.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.borshchevskiy.analyticsbuilderservice.dto.Currency;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.SalaryEntity;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.VacancyEntity;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyAnalyticsRepository;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prepares infrastructure for integration tests:
 * <ul>
 *     <li>launches containers;</li>
 *     <li>populates properties from container-provided values;</li>
 *     <li>populates MongoDb with vacancies;</li>
 *     <li>populates set of present queries;</li>
 *     <li>clears databases after each test;</li>
 *     <li>purges RabbitMQ queue after each test;</li>
 * </ul>
 */
@Testcontainers
public abstract class IntegrationTestBase {

    /**
     * Set of queries available in vacancies in Mongo database.
     */
    protected Set<String> queries = new HashSet<>();

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private VacancyAnalyticsRepository vacancyAnalyticsRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("analytics_db");

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:6.0.6"));

    @Container
    static GenericContainer<?> rabbitMQContainer =
            new GenericContainer<>(DockerImageName.parse("bitnami/rabbitmq:3.12.0"))
                    .withExposedPorts(5672, 15672)
                    .withEnv("RABBITMQ_USERNAME", "user")
                    .withEnv("RABBITMQ_PASSWORD", "password");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getMappedPort(5672));
        registry.add("spring.rabbitmq.username", () -> rabbitMQContainer.getEnvMap().get("RABBITMQ_USERNAME"));
        registry.add("spring.rabbitmq.password", () -> rabbitMQContainer.getEnvMap().get("RABBITMQ_PASSWORD"));
    }

    @BeforeEach
    public void prepare() {
        VacancyEntity vacancyJavaPythonWith100k = new VacancyEntity();
        VacancyEntity vacancyJavaWith150k = new VacancyEntity();
        VacancyEntity vacancyJavaKotlinWith200k = new VacancyEntity();
        VacancyEntity vacancyPythonKotlinWith300k = new VacancyEntity();

        SalaryEntity salary100k = new SalaryEntity();
        salary100k.setCurrency(Currency.RUR.name());
        salary100k.setFrom(100000);

        SalaryEntity salary150k = new SalaryEntity();
        salary150k.setCurrency(Currency.RUR.name());
        salary150k.setFrom(100000);
        salary150k.setTo(200000);

        SalaryEntity salary200k = new SalaryEntity();
        salary200k.setCurrency(Currency.RUR.name());
        salary200k.setTo(200000);

        SalaryEntity salary300k = new SalaryEntity();
        salary300k.setCurrency(Currency.RUR.name());
        salary300k.setTo(300000);

        String javaQuery = "Java";
        String kotlinQuery = "Kotlin";
        String pythonQuery = "Python";

        queries.add(javaQuery);
        queries.add(kotlinQuery);
        queries.add(pythonQuery);

        Set<String> javaPythonQueries = Set.of(javaQuery, pythonQuery);
        Set<String> javaQueries = Set.of(javaQuery);
        Set<String> javaKotlinQueries = Set.of(javaQuery, kotlinQuery);
        Set<String> pythonKotlinQueries = Set.of(pythonQuery, kotlinQuery);

        vacancyJavaPythonWith100k.setSalaryEntity(salary100k);
        vacancyJavaPythonWith100k.setQuery(javaPythonQueries);

        vacancyJavaWith150k.setSalaryEntity(salary150k);
        vacancyJavaWith150k.setQuery(javaQueries);

        vacancyJavaKotlinWith200k.setSalaryEntity(salary200k);
        vacancyJavaKotlinWith200k.setQuery(javaKotlinQueries);

        vacancyPythonKotlinWith300k.setSalaryEntity(salary300k);
        vacancyPythonKotlinWith300k.setQuery(pythonKotlinQueries);

        List<VacancyEntity> vacancies = new ArrayList<>();
        vacancies.add(vacancyJavaPythonWith100k);
        vacancies.add(vacancyJavaWith150k);
        vacancies.add(vacancyJavaKotlinWith200k);
        vacancies.add(vacancyPythonKotlinWith300k);

        vacancyRepository.insert(vacancies);
    }

    @AfterEach
    public void clear() {
        vacancyRepository.deleteAll();
        vacancyAnalyticsRepository.deleteAll();
        rabbitAdmin.purgeQueue("analytics-builder-scheduled-tasks-queue");
    }
}
