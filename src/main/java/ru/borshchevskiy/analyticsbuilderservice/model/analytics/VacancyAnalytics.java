package ru.borshchevskiy.analyticsbuilderservice.model.analytics;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VacancyAnalytics {
    private Long id;
    private LocalDate createdAt;
    private String query;
    private int vacancyCount;
    private Double averageSalary;
}
