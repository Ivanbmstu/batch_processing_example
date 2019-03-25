package ru.example.batch.formatter.dto;

import lombok.Builder;
import lombok.Value;
import ru.example.model.VerifyStatus;

@Value
@Builder
public class VerifyResultReportDTO {
    private final VerifyStatus verifyStatus;
    private final ReportTransactionDTO sourceTransaction;
    private final ReportTransactionDTO storedTransaction;
}
