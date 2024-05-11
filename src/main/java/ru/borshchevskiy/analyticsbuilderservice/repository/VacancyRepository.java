package ru.borshchevskiy.analyticsbuilderservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.borshchevskiy.analyticsbuilderservice.model.VacancyEntity;

import java.util.List;

public interface VacancyRepository extends MongoRepository<VacancyEntity, String> {
    List<VacancyEntity> findBySalaryEntityNotNull ();
}
