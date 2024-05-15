package ru.borshchevskiy.analyticsbuilderservice.mapper;

import org.springframework.stereotype.Component;
import ru.borshchevskiy.analyticsbuilderservice.dto.VacancyAnalyticsDto;
import ru.borshchevskiy.analyticsbuilderservice.model.analytics.VacancyAnalytics;

import java.time.LocalDate;

@Component
public class VacancyAnalyticsMapper {
    public VacancyAnalytics mapToEntity(VacancyAnalyticsDto vacancyAnalyticsDto, String query, LocalDate createdAt) {
        VacancyAnalytics vacancyAnalytics = new VacancyAnalytics();
        vacancyAnalytics.setCreatedAt(createdAt);
        vacancyAnalytics.setQuery(query);
        vacancyAnalytics.setVacancyCount(vacancyAnalyticsDto.getVacancyCount());
        vacancyAnalytics.setAverageSalary(vacancyAnalyticsDto.getAverageSalary());
        return vacancyAnalytics;
    }
}
