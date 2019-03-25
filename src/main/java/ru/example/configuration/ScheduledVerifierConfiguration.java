package ru.example.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.example.batch.launcher.TransactionVerifyJobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Profile("!test")
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledVerifierConfiguration {

    private final TransactionVerifyJobLauncher verifyJobLauncher;

    @Scheduled(fixedRateString = "${job.repeat}")
    public void scheduleVerifier() throws Exception {
        verifyJobLauncher.tryLaunchJob();
    }
}
