package ru.borshchevskiy.analyticsbuilderservice.model.analytics;

import java.time.LocalDateTime;

public class VacancyAnalytics {
    private Long id;
    private LocalDateTime createdAt;
    private String query;
    private int vacancyCount;
    private Double averageSalary;
}
