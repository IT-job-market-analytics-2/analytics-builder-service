package ru.borshchevskiy.analyticsbuilderservice.unittests.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.borshchevskiy.analyticsbuilderservice.dto.Currency;
import ru.borshchevskiy.analyticsbuilderservice.dto.VacancyAnalyticsDto;
import ru.borshchevskiy.analyticsbuilderservice.mapper.VacancyAnalyticsMapper;
import ru.borshchevskiy.analyticsbuilderservice.model.analytics.VacancyAnalytics;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.SalaryEntity;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.VacancyEntity;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyAnalyticsRepository;
import ru.borshchevskiy.analyticsbuilderservice.service.VacancyAnalyticsService;
import ru.borshchevskiy.analyticsbuilderservice.service.VacancyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyAnalyticsServiceTest {
    @Mock
    private VacancyService vacancyService;
    @Mock
    private VacancyAnalyticsRepository vacancyAnalyticsRepository;
    @Spy
    private VacancyAnalyticsMapper vacancyAnalyticsMapper;
    @InjectMocks
    @Spy
    private VacancyAnalyticsService vacancyAnalyticsService;

    @Test
    @DisplayName("Test buildAnalytics - " +
            "saveAnalytics() method called expected number of times with correctly built analytics dtos")
    void testBuildAnalytics() {
        //Given
        VacancyEntity vacancyJavaPythonWith100k = new VacancyEntity();
        VacancyEntity vacancyJavaWith150k = new VacancyEntity();
        VacancyEntity vacancyJavaKotlinWith200k = new VacancyEntity();

        SalaryEntity salary100k = new SalaryEntity();
        salary100k.setCurrency(Currency.RUR.name());
        salary100k.setFrom(100000);

        SalaryEntity salary150k = new SalaryEntity();
        salary150k.setCurrency(Currency.RUR.name());
        salary150k.setFrom(100000);
        salary150k.setTo(200000);

        SalaryEntity salary200k = new SalaryEntity();
        salary200k.setCurrency(Currency.RUR.name());
        salary200k.setTo(200000);

        String javaQuery = "Java";
        String kotlinQuery = "Kotlin";
        String pythonQuery = "Python";

        Set<String> javaPythonQueries = Set.of(javaQuery, pythonQuery);
        Set<String> javaQueries = Set.of(javaQuery);
        Set<String> javaKotlinQueries = Set.of(javaQuery, kotlinQuery);

        vacancyJavaPythonWith100k.setSalaryEntity(salary100k);
        vacancyJavaPythonWith100k.setQuery(javaPythonQueries);

        vacancyJavaWith150k.setSalaryEntity(salary150k);
        vacancyJavaWith150k.setQuery(javaQueries);

        vacancyJavaKotlinWith200k.setSalaryEntity(salary200k);
        vacancyJavaKotlinWith200k.setQuery(javaKotlinQueries);

        List<VacancyEntity> fromRepositoryList = List.of(vacancyJavaPythonWith100k,
                vacancyJavaWith150k,
                vacancyJavaKotlinWith200k);

        VacancyAnalyticsDto javaAnalytics = new VacancyAnalyticsDto(3, 150_000d);
        VacancyAnalyticsDto pythonAnalytics = new VacancyAnalyticsDto(1, 100_000d);
        VacancyAnalyticsDto kotlinAnalytics = new VacancyAnalyticsDto(1, 200_000d);
        //When
        when(vacancyService.findAllWithSalaryCurrency(Currency.RUR)).thenReturn(fromRepositoryList);
        vacancyAnalyticsService.buildAnalytics();
        //Then
        verify(vacancyAnalyticsService, times(1))
                .saveAnalytics(eq(javaQuery), eq(javaAnalytics));
        verify(vacancyAnalyticsService, times(1))
                .saveAnalytics(eq(pythonQuery), eq(pythonAnalytics));
        verify(vacancyAnalyticsService, times(1))
                .saveAnalytics(eq(kotlinQuery), eq(kotlinAnalytics));

    }

    @Test
    @DisplayName("Test saveAnalytics without any existing data in db - " +
            "repository's save() method called with expected argument")
    void testSaveAnalyticsWithoutExistingData() {
        //Given
        LocalDate date = LocalDate.now();
        String query = "Java";
        VacancyAnalyticsDto vacancyAnalyticsDto = new VacancyAnalyticsDto(1, 100_000d);
        VacancyAnalytics expectedAnalytics = new VacancyAnalytics();
        expectedAnalytics.setCreatedAt(date);
        expectedAnalytics.setQuery(query);
        expectedAnalytics.setVacancyCount(vacancyAnalyticsDto.getVacancyCount());
        expectedAnalytics.setAverageSalary(vacancyAnalyticsDto.getAverageSalary());

        //When
        when(vacancyAnalyticsRepository.findByQueryAndCreatedAt(query, date))
                .thenReturn(Optional.empty());

        vacancyAnalyticsService.saveAnalytics(query, vacancyAnalyticsDto);

        //Then
        verify(vacancyAnalyticsRepository, times(1)).save(eq(expectedAnalytics));
    }

    @Test
    @DisplayName("Test saveAnalytics with existing data in db - " +
            "repository's save() method called with updated existing data as argument")
    void testSaveAnalyticsWithExistingData() {
        //Given
        LocalDate date = LocalDate.now();
        String query = "Java";
        int existingVacancyCount = 100;
        double existingAverageSalary = 100_000d;
        VacancyAnalyticsDto vacancyAnalyticsDto = new VacancyAnalyticsDto(200, 200_000d);

        VacancyAnalytics expectedAnalytics = new VacancyAnalytics();
        expectedAnalytics.setCreatedAt(date);
        expectedAnalytics.setQuery(query);
        expectedAnalytics.setVacancyCount(vacancyAnalyticsDto.getVacancyCount());
        expectedAnalytics.setAverageSalary(vacancyAnalyticsDto.getAverageSalary());

        VacancyAnalytics existingAnalytics = new VacancyAnalytics();
        existingAnalytics.setCreatedAt(date);
        existingAnalytics.setQuery(query);
        existingAnalytics.setVacancyCount(existingVacancyCount);
        existingAnalytics.setAverageSalary(existingAverageSalary);

        //When
        when(vacancyAnalyticsRepository.findByQueryAndCreatedAt(query, date))
                .thenReturn(Optional.of(existingAnalytics));

        vacancyAnalyticsService.saveAnalytics(query, vacancyAnalyticsDto);

        //Then
        verify(vacancyAnalyticsRepository, times(1)).save(eq(expectedAnalytics));
    }
}