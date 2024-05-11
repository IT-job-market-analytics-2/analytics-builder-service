package ru.borshchevskiy.analyticsbuilderservice.model;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class VacancyEntity {

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
