package ru.example.batch.launcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionVerifyJobLauncherTest {

    @Test
    @DisplayName("should skip job run if any job running")
    void testShouldNotRunAlreadyExecutngJobAgain() throws Exception {
        String jobName = "jobName";
        JobLauncher launcher = mock(JobLauncher.class);
        Job job = mock(Job.class);
        JobExplorer jobExplorer = mock(JobExplorer.class);
        when(job.getName()).thenReturn(jobName);
        when(jobExplorer.findRunningJobExecutions(eq(jobName)))
                .thenReturn(Collections.singleton(mock(JobExecution.class)));
        when(launcher.run(any(Job.class), any(JobParameters.class))).thenThrow(RuntimeException.class);
        TransactionVerifyJobLauncher transactionVerifyJobLauncher = new TransactionVerifyJobLauncher(launcher, job, jobExplorer);

        boolean jobLaunched = transactionVerifyJobLauncher.tryLaunchJob();

        assertFalse(jobLaunched);
    }

}