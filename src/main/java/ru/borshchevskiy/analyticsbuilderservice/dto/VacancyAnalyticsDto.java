package ru.borshchevskiy.analyticsbuilderservice.dto;

import lombok.Data;

@Data
public class VacancyAnalyticsDto {
    private int totalVacancies;
    private Double avgSalary;

    public VacancyAnalyticsDto(int totalVacancies, Double avgSalary) {
        this.totalVacancies = totalVacancies;
        this.avgSalary = avgSalary;
    }
}
