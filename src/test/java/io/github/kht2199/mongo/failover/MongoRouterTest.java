package io.github.kht2199.mongo.failover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoRouterTest {

    private MongoClientRegistry registry;
    private MongoProperties properties;
    private MongoHealthStatus healthStatus;
    private MongoRouter router;

    @BeforeEach
    void setUp() {
        registry = mock(MongoClientRegistry.class);
        when(registry.size()).thenReturn(3);

        properties = new MongoProperties();
        properties.setDatabase("testdb");

        healthStatus = new MongoHealthStatus(3);
        router = new MongoRouter(registry, properties, healthStatus);
    }

    @Test
    void startsAtIndexZero() {
        assertThat(router.getCurrentIndex()).isEqualTo(0);
    }

    @Test
    void failoverAdvancesToNextHealthyInstance() {
        router.failover(0);
        assertThat(router.getCurrentIndex()).isEqualTo(1);
    }

    @Test
    void failoverSkipsUnhealthyInstances() {
        healthStatus.get(1).set(false);
        router.failover(0);
        assertThat(router.getCurrentIndex()).isEqualTo(2);
    }

    @Test
    void failoverIsNoOpWhenAlreadyAdvanced() {
        router.failover(0); // advances to 1
        router.failover(0); // stale call — currentIndex != failingIndex, returns immediately
        assertThat(router.getCurrentIndex()).isEqualTo(1);
    }

    @Test
    void throwsWhenAllInstancesUnhealthy() {
        healthStatus.get(0).set(false);
        healthStatus.get(1).set(false);
        healthStatus.get(2).set(false);
        assertThatThrownBy(() -> router.failover(0))
            .isInstanceOf(MongoUnavailableException.class)
            .hasMessageContaining("unavailable");
    }
}
