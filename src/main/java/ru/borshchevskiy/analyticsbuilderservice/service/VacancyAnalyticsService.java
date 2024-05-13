package ru.borshchevskiy.analyticsbuilderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.dto.Currency;
import ru.borshchevskiy.analyticsbuilderservice.dto.VacancyAnalyticsDto;
import ru.borshchevskiy.analyticsbuilderservice.mapper.VacancyAnalyticsMapper;
import ru.borshchevskiy.analyticsbuilderservice.model.analytics.VacancyAnalytics;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.SalaryEntity;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.VacancyEntity;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyAnalyticsRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class VacancyAnalyticsService {

    private final VacancyService vacancyService;
    private final VacancyAnalyticsRepository vacancyAnalyticsRepository;
    private final VacancyAnalyticsMapper vacancyAnalyticsMapper;

    public VacancyAnalyticsService(VacancyService vacancyService,
                                   VacancyAnalyticsRepository vacancyAnalyticsRepository,
                                   VacancyAnalyticsMapper vacancyAnalyticsMapper) {
        this.vacancyService = vacancyService;
        this.vacancyAnalyticsRepository = vacancyAnalyticsRepository;
        this.vacancyAnalyticsMapper = vacancyAnalyticsMapper;
    }

    public void buildAnalytics() {
        log.debug("Started building analytics");
        List<VacancyEntity> vacancies = vacancyService.findAllWithSalaryCurrency(Currency.RUR);
        log.debug("Found {} vacancies with salary in RUR", vacancies.size());
        Map<String, VacancyAnalyticsDto> perQueryVacancyAnalyticsMap = new HashMap<>();
        for (var vacancy : vacancies) {
            for (String query : vacancy.getQuery()) {
                perQueryVacancyAnalyticsMap.merge(query,
                        getSingleVacancyAnalytics(vacancy),
                        this::updateAnalyticsData);
            }
        }
        log.debug("Prepared analytics data for queries: {}", perQueryVacancyAnalyticsMap.keySet());
        for (var entry : perQueryVacancyAnalyticsMap.entrySet()) {
            saveAnalytics(entry.getKey(), entry.getValue());
        }
    }

    public void saveAnalytics(String query, VacancyAnalyticsDto vacancyAnalyticsDto) {
        LocalDate createdAt = LocalDate.now();
        log.debug("Starting saving analytics data for query: {}, on date: {}.", query, createdAt);
        Optional<VacancyAnalytics> existingAnalytics =
                vacancyAnalyticsRepository.findByQueryAndCreatedAt(query, createdAt);

        existingAnalytics.ifPresentOrElse(
                analytics -> {
                    log.debug("Analytics for query {} on date {} already present. Updating analytics data...",
                            query, createdAt);
                    analytics.setVacancyCount(vacancyAnalyticsDto.getVacancyCount());
                    analytics.setAverageSalary(vacancyAnalyticsDto.getAverageSalary());
                    vacancyAnalyticsRepository.save(analytics);
                    log.debug("Analytics for query {} on date {} successfully updated.", query, createdAt);
                },
                () -> {
                    VacancyAnalytics vacancyAnalytics =
                            vacancyAnalyticsMapper.mapToEntity(vacancyAnalyticsDto, query, createdAt);
                    vacancyAnalyticsRepository.save(vacancyAnalytics);
                    log.debug("Analytics for query {} on date {} successfully saved.", query, createdAt);
                }
        );
    }

    private VacancyAnalyticsDto getSingleVacancyAnalytics(VacancyEntity vacancy) {
        return new VacancyAnalyticsDto(1, calculateVacancySalary(vacancy.getSalaryEntity()));
    }

    private VacancyAnalyticsDto updateAnalyticsData(VacancyAnalyticsDto existingData, VacancyAnalyticsDto newData) {
        int newVacanciesCount = existingData.getVacancyCount() + 1;
        double avgSalary = existingData.getAverageSalary();
        double salary = newData.getAverageSalary();
        double newAvgSalary = avgSalary + (salary - avgSalary) / newVacanciesCount;
        existingData.setVacancyCount(newVacanciesCount);
        existingData.setAverageSalary(newAvgSalary);
        return existingData;
    }

    private Double calculateVacancySalary(SalaryEntity salary) {
        if (salary.getFrom() != null && salary.getTo() != null) {
            return (double) ((salary.getFrom() + salary.getTo()) / 2);
        } else if (salary.getFrom() == null) {
            return salary.getTo().doubleValue();
        } else {
            return salary.getFrom().doubleValue();
        }
    }
}
