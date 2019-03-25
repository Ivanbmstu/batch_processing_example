package ru.example.batch.reader;

import lombok.RequiredArgsConstructor;
import ru.example.configuration.TransactionVerifierProperties;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class TransactionLineTokenizer implements LineTokenizer {
    private final FieldSetFactory fieldSetFactory;
    private final String[] names;
    private final TransactionVerifierProperties.FileSourceProperties fileSourceProperties;

    @Override
    public FieldSet tokenize(String line) {
        // NOTE: не очень нравится такой подход, но предполагаю, что формат жесткий для данного типа источника данных
        // в общем могут не все поля понадобится, поэтому не завязываюсь на число данных в файле
        if (line.startsWith(fileSourceProperties.getLastLinePrefix())) {
            return null;
        }

        String[] split = Stream.of(line.split(fileSourceProperties.getDelimiter()))
                .limit(names.length)
                .toArray(String[]::new);
        return fieldSetFactory.create(split, names);
    }
}
