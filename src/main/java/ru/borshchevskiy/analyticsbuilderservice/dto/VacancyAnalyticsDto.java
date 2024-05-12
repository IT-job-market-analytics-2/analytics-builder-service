package ru.borshchevskiy.analyticsbuilderservice.dto;

import lombok.Data;

@Data
public class VacancyAnalyticsDto {
    private int vacanciesCount;
    private Double averageSalary;

    public VacancyAnalyticsDto(int vacanciesCount, Double averageSalary) {
        this.vacanciesCount = vacanciesCount;
        this.averageSalary = averageSalary;
    }
}
