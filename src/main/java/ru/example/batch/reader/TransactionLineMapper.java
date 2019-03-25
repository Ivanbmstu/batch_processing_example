package ru.example.batch.reader;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.example.model.SourceTransaction;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;

@Slf4j
@Builder
public class TransactionLineMapper implements LineMapper<SourceTransaction> {
    private LineTokenizer tokenizer;
    private FieldSetMapper<SourceTransaction> fieldSetMapper;

    @Override
    public SourceTransaction mapLine(String line, int lineNumber) throws Exception {
        try {
            FieldSet fieldSet = tokenizer.tokenize(line);
            if (fieldSet != null) {
                return fieldSetMapper.mapFieldSet(fieldSet);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Cannot map source file line %s lineNumber %s", line, lineNumber);
            throw new RuntimeException(errorMessage, e);
        }
        return null;
    }
}
