package ru.example.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Optional;

@Data
@Validated
@ConfigurationProperties
public class TransactionVerifierProperties {

    @Positive
    @NotNull
    private Integer defaultChunk;

    @NotEmpty
    private String defaultCsvDelimiter;

    @Valid
    private FileSourceProperties fileSource;

    @Valid
    private ReportFileOutputProperties fileOutput;


    public String getOutputFilePath() {
        return Optional.ofNullable(fileOutput)
                .map(ReportFileOutputProperties::getBaseFilePath)
                .orElseGet(() -> Optional.ofNullable(fileSource)
                        .map(FileSourceProperties::getPath)
                        .map(path -> path + ".output")
                        .orElseThrow(() ->
                                new RuntimeException("Output base file path must be specified or source file path")));
    }

    @Data
    public static class FileSourceProperties {
        @NotEmpty
        private String path;
        @NotEmpty
        private String delimiter;
        private String lastLinePrefix;
    }

    @Data
    public static class ReportFileOutputProperties {

        @NotEmpty
        private String baseFilePath;
    }
}
