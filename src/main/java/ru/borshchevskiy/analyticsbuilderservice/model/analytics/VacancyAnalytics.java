package ru.borshchevskiy.analyticsbuilderservice.model.analytics;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Table("Vacancy_analytics")
public class VacancyAnalytics {
    private Long id;
    private LocalDate createdAt;
    private String query;
    private int vacancyCount;
    private Double averageSalary;
}
