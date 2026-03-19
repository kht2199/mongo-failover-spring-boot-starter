package io.github.kht2199.mongo.failover.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {

    @Valid
    @NotEmpty
    private List<MongoInstanceConfig> instances;

    @NotBlank
    private String database;

    @Min(1)
    private int healthCheckIntervalMs = 10000;

    @Min(1)
    private int connectTimeoutMs = 3000;

    @Min(1)
    private int serverSelectionTimeoutMs = 3000;
}
