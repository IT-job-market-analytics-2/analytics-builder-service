package ru.borshchevskiy.analyticsbuilderservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.dto.AnalyticsBuilderServiceTaskDto;

@Service
@Slf4j
public class TaskListenerService {

    private final VacancyAnalyticsService vacancyAnalyticsService;

    public TaskListenerService(VacancyAnalyticsService vacancyAnalyticsService) {
        this.vacancyAnalyticsService = vacancyAnalyticsService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.analytics-builder-scheduled-tasks-queue}")
    private void consumeTask(AnalyticsBuilderServiceTaskDto scheduledTask) {
        log.debug("Received scheduled task");
        vacancyAnalyticsService.buildAnalytics();
    }

}
