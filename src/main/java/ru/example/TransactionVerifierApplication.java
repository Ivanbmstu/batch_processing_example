package ru.example;

import ru.example.configuration.TransactionVerifierProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TransactionVerifierProperties.class})
public class TransactionVerifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionVerifierApplication.class, args);
    }
}
