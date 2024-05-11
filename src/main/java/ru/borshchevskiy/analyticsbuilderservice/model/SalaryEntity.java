package ru.borshchevskiy.analyticsbuilderservice.model;

import lombok.Data;

@Data
public class SalaryEntity {

    private Integer from;

    private Integer to;

    private String currency;

    private Boolean gross;
}