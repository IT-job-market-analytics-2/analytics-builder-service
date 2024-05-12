package ru.borshchevskiy.analyticsbuilderservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.borshchevskiy.analyticsbuilderservice.model.analytics.VacancyAnalytics;

import java.time.LocalDate;
import java.util.Optional;

public interface VacancyAnalyticsRepository extends CrudRepository<VacancyAnalytics, Long> {

    Optional<VacancyAnalytics> findByQueryAndCreatedAt (String query, LocalDate date);
}
