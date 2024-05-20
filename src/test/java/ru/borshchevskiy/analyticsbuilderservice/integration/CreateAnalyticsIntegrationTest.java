package ru.borshchevskiy.analyticsbuilderservice.integration;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import ru.borshchevskiy.analyticsbuilderservice.dto.AnalyticsBuilderServiceTaskDto;
import ru.borshchevskiy.analyticsbuilderservice.dto.VacancyAnalyticsDto;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyAnalyticsRepository;
import ru.borshchevskiy.analyticsbuilderservice.service.VacancyAnalyticsService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestRabbitConfig.class)
@ActiveProfiles("test")
public class CreateAnalyticsIntegrationTest extends IntegrationTestBase {

    @SpyBean
    private VacancyAnalyticsService vacancyAnalyticsService;

    @Autowired
    private VacancyAnalyticsRepository vacancyAnalyticsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    void testRabbitMQMessageReceived() {
        String queue = "analytics-builder-scheduled-tasks-queue";
        // Verify queue is empty at the start of test
        assertEquals(0, rabbitAdmin.getQueueInfo(queue).getMessageCount());

        rabbitTemplate.convertAndSend("scheduled-tasks-exchange",
                "analytics-builder-task",
                new AnalyticsBuilderServiceTaskDto());
        // Verify that buildAnalytics was called only once
        await().atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> verify(vacancyAnalyticsService, times(1)).buildAnalytics()
                );
        // Verify that saveAnalytics was called number of times to the number of queries
        await().atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> verify(vacancyAnalyticsService, times(queries.size()))
                                .saveAnalytics(anyString(), any(VacancyAnalyticsDto.class))
                );
        // Verify that number of entries in database equals number of queries
        await().atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(vacancyAnalyticsRepository.findAll()).hasSize(queries.size()));
        // Verify that entries in database has correct date (now)
        await().atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(vacancyAnalyticsRepository.findAll()).are(new Condition<>(element ->
                            element.getCreatedAt().equals(LocalDate.now()), "Has 'now' date"));
                });
        // Verify that no queries are missing in database
        await().atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Set<String> savedQueries = new HashSet<>();
                    vacancyAnalyticsRepository.findAll().forEach(element -> savedQueries.add(element.getQuery()));
                    assertThat(queries).hasSameElementsAs(savedQueries);
                });
    }
}
