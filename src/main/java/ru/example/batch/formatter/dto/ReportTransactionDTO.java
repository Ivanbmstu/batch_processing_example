package ru.example.batch.formatter.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ReportTransactionDTO {

    private final Integer id;
    private final BigDecimal amount;
}
