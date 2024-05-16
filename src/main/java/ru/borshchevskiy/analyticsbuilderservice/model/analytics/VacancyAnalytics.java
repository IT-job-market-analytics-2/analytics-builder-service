package ru.borshchevskiy.analyticsbuilderservice.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Table("Vacancy_analytics")
public class VacancyAnalytics {
    @Id
    private Integer id;
    @Column("date")
    private LocalDate createdAt;
    @Column("query")
    private String query;
    @Column("vacancy_count")
    private int vacancyCount;
    @Column("average_salary")
    private Double averageSalary;
}
