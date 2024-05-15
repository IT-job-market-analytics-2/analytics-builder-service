package ru.borshchevskiy.analyticsbuilderservice.model.vacancy;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "vacancy")
public class VacancyEntity {
    @Indexed(name = "main_index", unique = true)
    private String id;

    private String name;

    private AreaEntity areaEntity;

    private SalaryEntity salaryEntity;

    private TypeEntity typeEntity;

    private Object responseUrl;

    private String publishedAt;

    private String createdAt;

    private Boolean archived;

    private String alternateUrl;

    private EmployerEntity employerEntity;

    private SnippetEntity snippetEntity;

    private ExperienceEntity experienceEntity;

    private EmploymentEntity employmentEntity;

    private Date deleteAt;

    private Set<String> query;
}
