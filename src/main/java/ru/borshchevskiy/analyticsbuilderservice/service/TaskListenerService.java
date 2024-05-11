package ru.borshchevskiy.analyticsbuilderservice.service;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.dto.ScheduledTask;

@Service
public class TaskListenerService {

    private final AnalyticsService analyticsService;

    public TaskListenerService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.analytics-builder-scheduled-tasks-queue}")
    private void consumeTask(ScheduledTask scheduledTask) {
        analyticsService.buildAnalytics();
    }

}
