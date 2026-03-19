package io.github.kht2199.mongo.failover.core;

import io.github.kht2199.mongo.failover.config.MongoInstanceConfig;
import io.github.kht2199.mongo.failover.config.MongoProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoClientRegistryTest {

    private MongoProperties buildProperties(String... uris) {
        MongoProperties props = new MongoProperties();
        props.setDatabase("testdb");
        props.setConnectTimeoutMs(500);
        props.setServerSelectionTimeoutMs(500);
        List<MongoInstanceConfig> instances = new java.util.ArrayList<>();
        for (int i = 0; i < uris.length; i++) {
            MongoInstanceConfig cfg = new MongoInstanceConfig();
            cfg.setName("instance-" + i);
            cfg.setUri(uris[i]);
            instances.add(cfg);
        }
        props.setInstances(instances);
        return props;
    }

    @Test
    void createsClientForEachInstance() {
        MongoProperties props = buildProperties(
            "mongodb://localhost:27017/testdb",
            "mongodb://localhost:27018/testdb"
        );
        MongoClientRegistry registry = new MongoClientRegistry(props);
        registry.init();

        assertThat(registry.size()).isEqualTo(2);
        assertThat(registry.getClient(0)).isNotNull();
        assertThat(registry.getClient(1)).isNotNull();
        registry.destroy();
    }

    @Test
    void throwsOnOutOfBoundsIndex() {
        MongoProperties props = buildProperties("mongodb://localhost:27017/testdb");
        MongoClientRegistry registry = new MongoClientRegistry(props);
        registry.init();

        assertThatThrownBy(() -> registry.getClient(1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        registry.destroy();
    }
}
