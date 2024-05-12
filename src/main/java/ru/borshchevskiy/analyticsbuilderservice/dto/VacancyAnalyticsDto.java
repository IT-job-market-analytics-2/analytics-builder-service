package ru.borshchevskiy.analyticsbuilderservice.dto;

import lombok.Data;

@Data
public class VacancyAnalyticsDto {
    private int vacancyCount;
    private Double averageSalary;

    public VacancyAnalyticsDto(int vacancyCount, Double averageSalary) {
        this.vacancyCount = vacancyCount;
        this.averageSalary = averageSalary;
    }
}
