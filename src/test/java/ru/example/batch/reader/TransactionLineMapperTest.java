package ru.example.batch.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionLineMapperTest {


    private TransactionLineTokenizer tokenizer;
    private FieldSetMapper fieldSetMapper;
    private TransactionLineMapper mapper;

    @BeforeEach
    void init() {
        tokenizer = mock(TransactionLineTokenizer.class);
        fieldSetMapper = mock(FieldSetMapper.class);
        mapper = TransactionLineMapper.builder()
                .tokenizer(tokenizer)
                .fieldSetMapper(fieldSetMapper)
                .build();

    }

    @Test
    @DisplayName("should return exception with line data and line number info if tokenizer parse fails")
    void testInvalidData() {
        when(tokenizer.tokenize(anyString())).thenThrow(new RuntimeException());
        String line = "testLine";
        int lineNumber = 1;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> mapper.mapLine(line, lineNumber));

        assertTrue(exception.getMessage().contains(line));
        assertTrue(exception.getMessage().contains(Integer.toString(lineNumber)));
    }


    @Test
    @DisplayName("should return exception with line data and line number info if field mapper fails")
    void testInvalidDataMapperException() throws BindException {
        FieldSet fieldSet = mock(FieldSet.class);
        when(tokenizer.tokenize(anyString())).thenReturn(fieldSet);
        when(fieldSetMapper.mapFieldSet(fieldSet)).thenThrow(new RuntimeException());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> mapper.mapLine("testLine", 1));

        assertTrue(exception.getMessage().contains("testLine"));
        assertTrue(exception.getMessage().contains(Integer.toString(1)));
    }

}