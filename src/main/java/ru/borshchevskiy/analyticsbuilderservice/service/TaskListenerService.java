package ru.borshchevskiy.analyticsbuilderservice.service;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.dto.ScheduledTask;

@Service
public class TaskListenerService {

    private final VacancyAnalyticsService vacancyAnalyticsService;

    public TaskListenerService(VacancyAnalyticsService vacancyAnalyticsService) {
        this.vacancyAnalyticsService = vacancyAnalyticsService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.analytics-builder-scheduled-tasks-queue}")
    private void consumeTask(ScheduledTask scheduledTask) {
        vacancyAnalyticsService.buildAnalytics();
    }

}
