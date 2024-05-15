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

    /**
     * Method builds analytics data per each query that it finds among all vacancies.
     * <p>
     * First, method acquires all vacancies with non-null salary and required currency (RUR).
     * Then, iterating over these vacancies, for every query it finds,
     * it accumulates analytical data into {@link VacancyAnalyticsDto}.
     * <p>
     * As a result of these operations a <code>perQueryVacancyAnalyticsMap</code> is created,
     * mapping query to {@link VacancyAnalyticsDto} which holds analytical data on all vacancies related to that query.
     * <p>
     * Finally, method calls {@link VacancyAnalyticsService#saveAnalytics(String, VacancyAnalyticsDto)} for each
     * <code>perQueryVacancyAnalyticsMap</code> entry.
     */
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

    /**
     * Method attempts to create {@link VacancyAnalytics} object from
     * {@link VacancyAnalyticsDto} and save it to database.
     * <p>
     * First, method checks if analytics data for specified <code>query</code> and <code>createdAt</code> date
     * is already present in database. If present, it updates existing data with new values.
     * If there is no existing data, it creates new entity
     * using {@link VacancyAnalyticsMapper#mapToEntity(VacancyAnalyticsDto, String, LocalDate)} and saves it to
     * database.
     * @param query query for which analytics data should be stored.
     * @param vacancyAnalyticsDto object, holding analytics data.
     */
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

    /**
     * Method creates analytics data for single vacancy,
     * specifying vacancy count as 1 and calculating average salary for this vacancy.
     * @param vacancy VacancyEntity on which analytics data is created.
     * @return VacancyAnalyticsDto containing analytics data on single vacancy.
     */
    private VacancyAnalyticsDto getSingleVacancyAnalytics(VacancyEntity vacancy) {
        return new VacancyAnalyticsDto(1, calculateVacancySalary(vacancy.getSalaryEntity()));
    }

    /**
     * Method merges <code>newData</code>, which is most commonly analytics data for single vacancy,
     * with <code>existingData</code> existing analytics data. Updates original <code>existingData</code> object.
     * Average salary is calculated as cumulative moving average.
     * @param existingData existing analyticsData.
     * @param newData new data to be merged to existing.
     * @return updated <code>existingData</code>.
     */
    private VacancyAnalyticsDto updateAnalyticsData(VacancyAnalyticsDto existingData, VacancyAnalyticsDto newData) {
        int newVacanciesCount = existingData.getVacancyCount() + newData.getVacancyCount();
        double avgSalary = existingData.getAverageSalary();
        double salary = newData.getAverageSalary();
        double newAvgSalary = avgSalary + (salary - avgSalary) / newVacanciesCount;
        existingData.setVacancyCount(newVacanciesCount);
        existingData.setAverageSalary(newAvgSalary);
        return existingData;
    }

    /**
     * Method calculates average salary for single vacancy.
     * If both upper and lower salary limits are specified - calculates average value.
     * If either upper or lower limit is specified - just takes this value as salary.
     * @param salary Salary entity specified in vacancy.
     * @return Double value of average salary calculated.
     */
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
