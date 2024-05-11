package ru.borshchevskiy.analyticsbuilderservice.service;

import org.springframework.stereotype.Service;
import ru.borshchevskiy.analyticsbuilderservice.model.vacancy.VacancyEntity;
import ru.borshchevskiy.analyticsbuilderservice.repository.VacancyRepository;

import java.util.List;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacanciesRepository) {
        this.vacancyRepository = vacanciesRepository;
    }

    public List<VacancyEntity> findAll() {
        return vacancyRepository.findAll();
    }

    public List<VacancyEntity> findAllWithSalary() {
        return vacancyRepository.findBySalaryEntityNotNull();
    }
}
