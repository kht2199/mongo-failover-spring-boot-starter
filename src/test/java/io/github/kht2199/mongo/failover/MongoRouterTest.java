package io.github.kht2199.mongo.failover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoRouterTest {

    private MongoClientRegistry registry;
    private MongoProperties properties;
    private AtomicBoolean[] healthStatus;
    private MongoRouter router;

    @BeforeEach
    void setUp() {
        registry = mock(MongoClientRegistry.class);
        when(registry.size()).thenReturn(3);

        properties = new MongoProperties();
        properties.setDatabase("testdb");

        healthStatus = new AtomicBoolean[]{
            new AtomicBoolean(true),
            new AtomicBoolean(true),
            new AtomicBoolean(true)
        };
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
        healthStatus[1].set(false);
        router.failover(0);
        assertThat(router.getCurrentIndex()).isEqualTo(2);
    }

    @Test
    void failoverIsIdempotentWhenCalledConcurrently() {
        router.failover(0);
        router.failover(0);
        assertThat(router.getCurrentIndex()).isEqualTo(1);
    }

    @Test
    void throwsWhenAllInstancesUnhealthy() {
        healthStatus[0].set(false);
        healthStatus[1].set(false);
        healthStatus[2].set(false);
        assertThatThrownBy(() -> router.failover(0))
            .isInstanceOf(MongoUnavailableException.class)
            .hasMessageContaining("unavailable");
    }
}
