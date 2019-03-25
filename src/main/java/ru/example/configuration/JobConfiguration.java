package ru.example.configuration;

import lombok.RequiredArgsConstructor;
import ru.example.batch.formatter.ReportFormatter;
import ru.example.batch.formatter.impl.LineAggregatorAdapter;
import ru.example.batch.processor.TransactionProcessor;
import ru.example.batch.reader.TransactionLineMapper;
import ru.example.batch.reader.TransactionLineTokenizer;
import ru.example.model.SourceTransaction;
import ru.example.model.VerifyTransactionResult;
import ru.example.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final TransactionVerifierProperties properties;

    @Bean
    public Job verifyTransactionsJob(Step verifyFileSourceStep) {
        return jobBuilderFactory.get("verifyTransactionsJob")
                .flow(verifyFileSourceStep)
                .end()
                .build();
    }

    @Bean
    public Step verifyFileSourceStep(CompositeItemWriter<VerifyTransactionResult> compositeWriter,
                                     ItemReader<SourceTransaction> transactionFlatFileItemReader,
                                     ItemProcessor<SourceTransaction, VerifyTransactionResult> processor) {
        return stepBuilderFactory.get("verifyTransactionsFromFileStep")
                .allowStartIfComplete(true)
                .<SourceTransaction, VerifyTransactionResult>chunk(properties.getDefaultChunk())
                .reader(transactionFlatFileItemReader)
                .processor(processor)
                .writer(compositeWriter)
                .build();
    }

    @Bean
    public ItemProcessor<SourceTransaction, VerifyTransactionResult> processor(TransactionRepository transactionRepository) {
        return new TransactionProcessor(transactionRepository);
    }

    @Bean
    public LineTokenizer lineTokenizer() {
        return new TransactionLineTokenizer(new DefaultFieldSetFactory(), new String[]{
                SourceTransaction.Fields.pid,
                SourceTransaction.Fields.pamount
        }, properties.getFileSource());
    }

    @Bean
    public FlatFileItemReader<SourceTransaction> transactionFlatFileItemReader() {
        final int skipHeaderLinesCount = 1;

        return new FlatFileItemReaderBuilder<SourceTransaction>()
                .lineMapper(lineMapper())
                .name("transactionFileReader")
                .resource(itemReaderResource())
                .linesToSkip(skipHeaderLinesCount)
                .build();
    }

    // NOTE: для частных форматов отчета в виде таблиц с заголовками и т.п. тут можно задать header и footer callback.
    @Bean
    public ItemWriter<VerifyTransactionResult> fileItemWriter(LineAggregator<VerifyTransactionResult> lineAggregator,
                                                              Resource itemWriterResource) {
        FlatFileItemWriter<VerifyTransactionResult> itemWriter = new FlatFileItemWriter<>();
        itemWriter.setResource(itemWriterResource);
        itemWriter.setLineAggregator(lineAggregator);
        return itemWriter;
    }

    @Bean
    public LineAggregator<VerifyTransactionResult> lineAggregator(ReportFormatter reportFormatter) {
        return new LineAggregatorAdapter(reportFormatter);
    }

    @Bean
    public LineMapper<SourceTransaction> lineMapper() {
        return TransactionLineMapper.builder()
                .tokenizer(lineTokenizer())
                .fieldSetMapper(new BeanWrapperFieldSetMapper<SourceTransaction>() {{
                    setTargetType(SourceTransaction.class);
                }})
                .build();
    }

    @Bean
    @StepScope
    public Resource itemReaderResource() {
        String fileSourcePath = Optional.ofNullable(properties.getFileSource())
                .map(fileSource -> fileSource.getPath())
                .orElseThrow(() -> new RuntimeException("No source file for file item reader"));
        return new FileSystemResource(fileSourcePath);
    }

    @Bean
    @StepScope
    public Resource itemWriterResource(@Value("#{jobParameters['output.file.suffix']}") String outputFileSuffix) {
        String fileOutputPath = properties.getOutputFilePath();
        return new FileSystemResource(fileOutputPath + outputFileSuffix);
    }

    @Bean
    public CompositeItemWriter<VerifyTransactionResult> compositeItemWriter(List<ItemWriter<? super VerifyTransactionResult>> itemWriters) {
        CompositeItemWriter<VerifyTransactionResult> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(itemWriters);
        return compositeItemWriter;
    }
}
