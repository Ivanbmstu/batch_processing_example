package ru.example.batch.formatter;

import ru.example.model.VerifyTransactionResult;

public interface ReportFormatter {

    String format(VerifyTransactionResult transactionResult);
}
