package ru.example.batch.formatter.impl;

import lombok.RequiredArgsConstructor;
import ru.example.batch.formatter.ReportFormatter;
import ru.example.model.VerifyTransactionResult;
import org.springframework.batch.item.file.transform.LineAggregator;

@RequiredArgsConstructor
public class LineAggregatorAdapter implements LineAggregator<VerifyTransactionResult> {

    private final ReportFormatter reportFormatter;

    @Override
    public String aggregate(VerifyTransactionResult item) {
        return reportFormatter.format(item);
    }
}
