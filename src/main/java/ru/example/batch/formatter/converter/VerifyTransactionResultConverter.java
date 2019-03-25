package ru.example.batch.formatter.converter;

import lombok.experimental.UtilityClass;
import ru.example.batch.formatter.dto.ReportTransactionDTO;
import ru.example.batch.formatter.dto.VerifyResultReportDTO;
import ru.example.model.SourceTransaction;
import ru.example.model.VerifyTransactionResult;
import ru.example.repository.entity.Transaction;

@UtilityClass
public class VerifyTransactionResultConverter {


    public static VerifyResultReportDTO convert(VerifyTransactionResult verifyTransactionResult) {
        return VerifyResultReportDTO.builder()
                .sourceTransaction(convertSourceTransaction(verifyTransactionResult.getSourceTransaction()))
                .storedTransaction(convertStoredTransaction(verifyTransactionResult.getStoredTransaction()))
                .verifyStatus(verifyTransactionResult.getVerifyStatus())
                .build();
    }

    private static ReportTransactionDTO convertSourceTransaction(SourceTransaction sourceTransaction) {
        if (sourceTransaction == null) {
            return null;
        }
        return new ReportTransactionDTO(sourceTransaction.getPid(), sourceTransaction.getPamount());
    }

    private static ReportTransactionDTO convertStoredTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new ReportTransactionDTO(transaction.getId(), transaction.getAmount());
    }
}
