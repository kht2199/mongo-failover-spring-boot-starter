package io.github.kht2199.mongo.failover.config;

import io.github.kht2199.mongo.failover.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
    "mongodb.instances[0].name=primary",
    "mongodb.instances[0].uri=mongodb://localhost:27017/testdb",
    "mongodb.instances[1].name=secondary",
    "mongodb.instances[1].uri=mongodb://localhost:27018/testdb",
    "mongodb.database=testdb",
    "mongodb.health-check-interval-ms=5000",
    "mongodb.connect-timeout-ms=1000",
    "mongodb.server-selection-timeout-ms=1000"
})
class MongoPropertiesTest {

    @Autowired
    MongoProperties properties;

    @Test
    void bindsInstancesList() {
        assertThat(properties.getInstances()).hasSize(2);
        assertThat(properties.getInstances().get(0).getName()).isEqualTo("primary");
        assertThat(properties.getInstances().get(0).getUri()).isEqualTo("mongodb://localhost:27017/testdb");
        assertThat(properties.getInstances().get(1).getName()).isEqualTo("secondary");
    }

    @Test
    void bindsScalarProperties() {
        assertThat(properties.getDatabase()).isEqualTo("testdb");
        assertThat(properties.getHealthCheckIntervalMs()).isEqualTo(5000);
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(1000);
        assertThat(properties.getServerSelectionTimeoutMs()).isEqualTo(1000);
    }
}
