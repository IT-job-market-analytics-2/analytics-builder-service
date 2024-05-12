package ru.borshchevskiy.analyticsbuilderservice.unittests.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.borshchevskiy.analyticsbuilderservice.dto.Currency;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.SalaryEntity;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.VacancyEntity;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyRepository;
import ru.borshchevskiy.analyticsbuilderservice.service.VacancyService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    VacancyRepository vacancyRepository;

    @InjectMocks
    VacancyService vacancyService;

    @Test
    @DisplayName("Test findAllWithSalaryCurrency(Currency.RUR) - method returns all vacancies with RUR currency")
    public void testFindAllWithSalaryCurrency() {
        //Given
        VacancyEntity vacancyWithNullCurrency1 = new VacancyEntity();
        VacancyEntity vacancyWithNullCurrency2 = new VacancyEntity();

        vacancyWithNullCurrency1.setSalaryEntity(new SalaryEntity());
        vacancyWithNullCurrency2.setSalaryEntity(new SalaryEntity());

        VacancyEntity vacancyWithCurrency1 = new VacancyEntity();
        VacancyEntity vacancyWithCurrency2 = new VacancyEntity();

        SalaryEntity salary = new SalaryEntity();
        salary.setCurrency(Currency.RUR.name());
        salary.setFrom(100000);
        salary.setTo(200000);

        vacancyWithCurrency1.setSalaryEntity(salary);
        vacancyWithCurrency2.setSalaryEntity(salary);

        List<VacancyEntity> fromRepositoryList = List.of(vacancyWithNullCurrency1, vacancyWithNullCurrency2,
                vacancyWithCurrency1, vacancyWithCurrency2);

        List<VacancyEntity> expectedList = List.of(vacancyWithCurrency1, vacancyWithCurrency2);
        //When
        doReturn(fromRepositoryList)
                .when(vacancyRepository).findBySalaryEntityNotNull();

        List<VacancyEntity> actualList = vacancyService.findAllWithSalaryCurrency(Currency.RUR);
        //Then
        assertThat(actualList).containsAll(expectedList);
    }

}