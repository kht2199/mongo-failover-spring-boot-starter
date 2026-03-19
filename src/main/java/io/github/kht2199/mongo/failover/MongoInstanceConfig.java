package io.github.kht2199.mongo.failover;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongoInstanceConfig {
    private String name;
    private String uri;
}
