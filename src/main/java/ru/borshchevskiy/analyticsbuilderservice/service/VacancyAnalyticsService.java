package ru.borshchevskiy.analyticsbuilderservice.service;

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
        List<VacancyEntity> vacancies = filterByCurrency(vacancyService.findAllWithSalary(), Currency.RUB);
        Map<String, VacancyAnalyticsDto> perQueryVacancyAnalyticsMap = new HashMap<>();
        for (var vacancy : vacancies) {
            for (String query : vacancy.getQuery()) {
                perQueryVacancyAnalyticsMap.merge(query,
                        getSingleVacancyAnalytics(vacancy),
                        this::updateAnalyticsData);
            }
        }
        for (var entry : perQueryVacancyAnalyticsMap.entrySet()) {
            saveAnalytics(entry.getKey(), entry.getValue());
        }
    }

    public void saveAnalytics(String query, VacancyAnalyticsDto vacancyAnalyticsDto) {
        LocalDate createdAt = LocalDate.now();

        Optional<VacancyAnalytics> existingAnalytics =
                vacancyAnalyticsRepository.findByQueryAndCreatedAt(query, createdAt);

        existingAnalytics.ifPresentOrElse(
                analytics -> {
                    analytics.setVacancyCount(vacancyAnalyticsDto.getVacanciesCount());
                    analytics.setAverageSalary(vacancyAnalyticsDto.getAverageSalary());
                    vacancyAnalyticsRepository.save(analytics);
                },
                () -> {
                    VacancyAnalytics vacancyAnalytics =
                            vacancyAnalyticsMapper.mapToEntity(vacancyAnalyticsDto, query, createdAt);
                    vacancyAnalyticsRepository.save(vacancyAnalytics);
                }
        );
    }

    private VacancyAnalyticsDto getSingleVacancyAnalytics(VacancyEntity vacancy) {
        return new VacancyAnalyticsDto(1, calculateVacancySalary(vacancy.getSalaryEntity()));
    }

    private VacancyAnalyticsDto updateAnalyticsData(VacancyAnalyticsDto newData, VacancyAnalyticsDto existingData) {
        int newVacanciesCount = existingData.getVacanciesCount() + 1;
        double avgSalary = existingData.getAverageSalary();
        double salary = newData.getAverageSalary();
        double newAvgSalary = avgSalary + (salary - avgSalary) / newVacanciesCount;
        existingData.setVacanciesCount(newVacanciesCount);
        existingData.setAverageSalary(newAvgSalary);
        return existingData;
    }

    private List<VacancyEntity> filterByCurrency(List<VacancyEntity> vacancies, Currency currency) {
        return vacancies.stream()
                .filter(vacancy -> vacancy.getSalaryEntity().getCurrency() != null)
                .filter(vacancy -> vacancy.getSalaryEntity().getCurrency().equals(currency.name()))
                .toList();
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
