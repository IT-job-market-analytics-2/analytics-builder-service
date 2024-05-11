package ru.borshchevskiy.analyticsbuilderservice.service;

import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.dto.Currency;
import ru.borshchevskiy.analyticsbuilderservice.model.SalaryEntity;
import ru.borshchevskiy.analyticsbuilderservice.model.VacancyEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final VacancyService vacancyService;

    public AnalyticsService(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    static class QueryData {
        private int totalVacancies;
        private Double avgSalary;

        public QueryData(int totalVacancies, Double avgSalary) {
            this.totalVacancies = totalVacancies;
            this.avgSalary = avgSalary;
        }

        public int getTotalVacancies() {
            return totalVacancies;
        }

        public void setTotalVacancies(int totalVacancies) {
            this.totalVacancies = totalVacancies;
        }

        public Double getAvgSalary() {
            return avgSalary;
        }

        public void setAvgSalary(Double avgSalary) {
            this.avgSalary = avgSalary;
        }
    }

    public void buildAnalytics() {
        List<VacancyEntity> vacancies = filterByCurrency(vacancyService.findAllWithSalary(), Currency.RUB);
        Map<String, QueryData> queryDataMap = new HashMap<>();
        for (var vacancy : vacancies) {
            for (String query : vacancy.getQuery()) {
                queryDataMap.merge(query,
                        new QueryData(1, calculateVacancySalary(vacancy.getSalaryEntity())),
                        this::updateQueryData);
            }
        }
    }

    private QueryData updateQueryData(QueryData newData, QueryData existingData) {
        int newVacanciesCount = existingData.getTotalVacancies() + 1;
        double avgSalary = existingData.getAvgSalary();
        double salary = newData.getAvgSalary();
        double newAvgSalary = avgSalary + (salary - avgSalary) / newVacanciesCount;
        existingData.setTotalVacancies(newVacanciesCount);
        existingData.setAvgSalary(newAvgSalary);
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
