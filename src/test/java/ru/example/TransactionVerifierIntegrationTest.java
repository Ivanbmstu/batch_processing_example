package ru.example;

import ru.example.configuration.TransactionVerifierProperties;
import ru.example.repository.TransactionRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBatchTest
public class TransactionVerifierIntegrationTest extends BaseTest {


    @Autowired
    private JobLauncher jobLauncher;

    @SpyBean
    private TransactionVerifierProperties verifierProperties;
    @Autowired
    private Job verifyTransactionsJob;
    @Autowired
    private TransactionRepository repository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @AfterEach
    public void dropTable() {
        repository.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

    public File initFile(String sourceFile) throws IOException {
        File file = temporaryFolder.newFile("test-1.csv");
        Files.copy(new ClassPathResource(sourceFile).getFile(), file);
        TransactionVerifierProperties.ReportFileOutputProperties properties = mock(TransactionVerifierProperties.ReportFileOutputProperties.class);
        TransactionVerifierProperties.FileSourceProperties sourceProperties = mock(TransactionVerifierProperties.FileSourceProperties.class);
        when(sourceProperties.getPath()).thenReturn(file.getAbsolutePath());
        when(properties.getBaseFilePath()).thenReturn(file.getAbsolutePath());
        when(verifierProperties.getOutputFilePath()).thenReturn(file.getAbsolutePath());
        when(verifierProperties.getFileOutput()).thenReturn(properties);
        when(verifierProperties.getFileSource()).thenReturn(sourceProperties);
        return file;
    }

    @Test
    @Sql("/test-1/insert.sql")
    @DisplayName("should write to file the same line count as source file transactions count")
    public void testExample() throws Exception{
        final int sourceHeader = 1;
        final int sourceFooter = 1;
        File file = initFile("test-1/test-1.csv");
        String suffix = ".suffix";
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("output.file.suffix", suffix).toJobParameters();

        jobLauncher.run(verifyTransactionsJob, jobParameters);

        File outputFile = new File(file.getAbsolutePath() + suffix);
        assertTrue(outputFile.exists());
        assertEquals(Files.readLines(outputFile, Charset.defaultCharset()).size(),
                Files.readLines(file, Charset.defaultCharset()).size() - sourceHeader - sourceFooter);
        JobExecution lastJobExecution = jobRepository.getLastJobExecution(verifyTransactionsJob.getName(), jobParameters);
        assertEquals(lastJobExecution.getStatus(), BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("should complete job with empty result file if no source transactions")
    @Sql("/test-2/insert.sql")
    public void testEmptyDatabaseData() throws Exception{
        File file = initFile("test-2/test-empty.csv");
        String suffix = ".suffix";
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("output.file.suffix", suffix).toJobParameters();

        jobLauncher.run(verifyTransactionsJob, jobParameters);

        File outputFile = new File(file.getAbsolutePath() + suffix);
        assertTrue(outputFile.exists());
        assertEquals(Files.readLines(outputFile, Charset.defaultCharset()).size(), 0);
        JobExecution lastJobExecution = jobRepository.getLastJobExecution(verifyTransactionsJob.getName(), jobParameters);
        assertEquals(lastJobExecution.getStatus(), BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("should fail job if source transactions contains invalid data")
    @Sql("/test-3/insert.sql")
    public void testFailJobInvalidData() throws Exception{
        File file = initFile("test-3/test-3.csv");
        String suffix = ".suffix";
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("output.file.suffix", suffix).toJobParameters();

        jobLauncher.run(verifyTransactionsJob, jobParameters);

        JobExecution lastJobExecution = jobRepository.getLastJobExecution(verifyTransactionsJob.getName(), jobParameters);
        assertEquals(lastJobExecution.getStatus(), BatchStatus.FAILED);
    }

}
