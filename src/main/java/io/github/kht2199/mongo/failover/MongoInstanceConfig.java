package io.github.kht2199.mongo.failover;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongoInstanceConfig {
    @NotBlank
    private String name;
    @NotBlank
    private String uri;
}
