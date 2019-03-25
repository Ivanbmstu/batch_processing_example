package ru.example.batch.formatter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.example.batch.formatter.ReportFormatter;
import ru.example.batch.formatter.converter.VerifyTransactionResultConverter;
import ru.example.batch.formatter.dto.VerifyResultReportDTO;
import ru.example.model.VerifyTransactionResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonReportFormatter implements ReportFormatter {

    private final ObjectMapper objectMapper;

    @Override
    public String format(VerifyTransactionResult transactionResult) {
        VerifyResultReportDTO dto = VerifyTransactionResultConverter.convert(transactionResult);
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            String error = String.format("error while convert dto %s", dto);
            log.error(error, e);
        }
        return null;
    }
}
