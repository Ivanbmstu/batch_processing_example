package ru.example.batch.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionVerifyJobLauncher {
    private final JobLauncher jobLauncher;
    private final Job verifyTransactionsJob;
    private final JobExplorer jobExplorer;


    public boolean tryLaunchJob() throws Exception {
        boolean canRunJob = canRunJob();
        if (canRunJob) {
            log.info("Start job {}", verifyTransactionsJob.getName());
            jobLauncher.run(verifyTransactionsJob, createJobParameters());
        } else {
            log.info("Skip job start cause it is already running");
        }
        return canRunJob;
    }

    private JobParameters createJobParameters() {
        // note: вообще лучше сделать отдельный интерфейс для определения суффикса и передевать его
        String suffix = "." + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        return new JobParametersBuilder()
                // note: вообще лучше сделать отдельный интерфейс для определения суффикса и передевать его
                .addString("output.file.suffix", suffix)
                .toJobParameters();
    }

    private boolean canRunJob() {
        return jobExplorer.findRunningJobExecutions(verifyTransactionsJob.getName()).isEmpty();
    }

}
