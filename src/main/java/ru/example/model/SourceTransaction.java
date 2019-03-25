package ru.example.model;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Data
@FieldNameConstants
public class SourceTransaction {

    private Integer pid;
    private BigDecimal pamount;
}
