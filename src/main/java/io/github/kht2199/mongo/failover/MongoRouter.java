package io.github.kht2199.mongo.failover;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.concurrent.atomic.AtomicInteger;

public class MongoRouter {

    private final MongoClientRegistry registry;
    private final MongoProperties properties;
    private final MongoHealthStatus healthStatus;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public MongoRouter(MongoClientRegistry registry,
                       MongoProperties properties,
                       MongoHealthStatus healthStatus) {
        this.registry = registry;
        this.properties = properties;
        this.healthStatus = healthStatus;
    }

    public MongoDatabase getDatabase() {
        return registry.getClient(currentIndex.get())
            .getDatabase(properties.getDatabase());
    }

    public MongoClient getActiveClient() {
        return registry.getClient(currentIndex.get());
    }

    public int getCurrentIndex() {
        return currentIndex.get();
    }

    public void failover(int failingIndex) {
        // If already advanced by another thread, nothing to do.
        if (currentIndex.get() != failingIndex) {
            return;
        }
        int size = registry.size();
        for (int i = 1; i <= size; i++) {
            int candidate = (failingIndex + i) % size;
            if (healthStatus.get(candidate).get()) {
                // CAS: succeeds if this thread is first. If another thread already advanced, this is a no-op.
                currentIndex.compareAndSet(failingIndex, candidate);
                return;
            }
        }
        throw new MongoUnavailableException("All MongoDB instances are unavailable");
    }
}
