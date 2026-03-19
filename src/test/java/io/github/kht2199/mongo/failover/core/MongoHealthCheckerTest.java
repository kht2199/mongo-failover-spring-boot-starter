package io.github.kht2199.mongo.failover.core;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.github.kht2199.mongo.failover.config.MongoInstanceConfig;
import io.github.kht2199.mongo.failover.config.MongoProperties;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MongoHealthCheckerTest {

    private MongoClientRegistry registry;
    private MongoRouter router;
    private MongoProperties properties;
    private MongoHealthStatus healthStatus;
    private MongoHealthChecker checker;

    @BeforeEach
    void setUp() {
        properties = new MongoProperties();
        properties.setDatabase("testdb");
        MongoInstanceConfig cfg0 = new MongoInstanceConfig(); cfg0.setName("primary"); cfg0.setUri("mongodb://localhost:27017/testdb");
        MongoInstanceConfig cfg1 = new MongoInstanceConfig(); cfg1.setName("secondary"); cfg1.setUri("mongodb://localhost:27018/testdb");
        properties.setInstances(List.of(cfg0, cfg1));

        registry = mock(MongoClientRegistry.class);
        when(registry.size()).thenReturn(2);

        router = mock(MongoRouter.class);
        when(router.getCurrentIndex()).thenReturn(0);

        healthStatus = new MongoHealthStatus(2);
        checker = new MongoHealthChecker(registry, router, properties, healthStatus);
    }

    @Test
    void marksInstanceUnhealthyWhenPingFails() {
        MongoClient failingClient = mock(MongoClient.class);
        MongoDatabase db = mock(MongoDatabase.class);
        when(failingClient.getDatabase("admin")).thenReturn(db);
        when(db.runCommand(any(Document.class))).thenThrow(new RuntimeException("connection refused"));
        when(registry.getClient(0)).thenReturn(failingClient);

        MongoClient healthyClient = mock(MongoClient.class);
        MongoDatabase db2 = mock(MongoDatabase.class);
        when(healthyClient.getDatabase("admin")).thenReturn(db2);
        when(registry.getClient(1)).thenReturn(healthyClient);

        checker.checkHealth();

        assertThat(healthStatus.get(0).get()).isFalse();
        assertThat(healthStatus.get(1).get()).isTrue();
    }

    @Test
    void triggersFailoverWhenActiveInstanceFails() {
        MongoClient failingClient = mock(MongoClient.class);
        MongoDatabase db = mock(MongoDatabase.class);
        when(failingClient.getDatabase("admin")).thenReturn(db);
        when(db.runCommand(any(Document.class))).thenThrow(new RuntimeException("down"));
        when(registry.getClient(0)).thenReturn(failingClient);

        MongoClient healthyClient = mock(MongoClient.class);
        MongoDatabase db2 = mock(MongoDatabase.class);
        when(healthyClient.getDatabase("admin")).thenReturn(db2);
        when(registry.getClient(1)).thenReturn(healthyClient);

        checker.checkHealth();

        verify(router).failover(0);
    }

    @Test
    void doesNotTriggerFailoverWhenNonActiveInstanceFails() {
        when(router.getCurrentIndex()).thenReturn(0);

        MongoClient healthyClient = mock(MongoClient.class);
        MongoDatabase db = mock(MongoDatabase.class);
        when(healthyClient.getDatabase("admin")).thenReturn(db);
        when(registry.getClient(0)).thenReturn(healthyClient);

        MongoClient failingClient = mock(MongoClient.class);
        MongoDatabase db2 = mock(MongoDatabase.class);
        when(failingClient.getDatabase("admin")).thenReturn(db2);
        when(db2.runCommand(any(Document.class))).thenThrow(new RuntimeException("down"));
        when(registry.getClient(1)).thenReturn(failingClient);

        checker.checkHealth();

        verify(router, never()).failover(anyInt());
    }
}
