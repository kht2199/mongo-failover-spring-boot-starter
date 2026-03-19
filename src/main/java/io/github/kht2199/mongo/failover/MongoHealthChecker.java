package io.github.kht2199.mongo.failover;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MongoHealthChecker {

    private final MongoClientRegistry registry;
    private final MongoRouter router;
    private final MongoProperties properties;
    private final AtomicBoolean[] healthStatus;

    public MongoHealthChecker(MongoClientRegistry registry,
                               MongoRouter router,
                               MongoProperties properties,
                               AtomicBoolean[] healthStatus) {
        this.registry = registry;
        this.router = router;
        this.properties = properties;
        this.healthStatus = healthStatus;
    }

    @Scheduled(fixedDelayString = "${mongodb.health-check-interval-ms:10000}")
    public void checkHealth() {
        int activeIndex = router.getCurrentIndex();
        for (int i = 0; i < registry.size(); i++) {
            boolean healthy = ping(i);
            boolean wasHealthy = healthStatus[i].getAndSet(healthy);
            if (!healthy && wasHealthy) {
                log.warn("MongoDB instance [{}] became unhealthy", instanceName(i));
            } else if (healthy && !wasHealthy) {
                log.info("MongoDB instance [{}] recovered", instanceName(i));
            }
            if (!healthy && i == activeIndex) {
                router.failover(i);
            }
        }
    }

    private boolean ping(int index) {
        try {
            registry.getClient(index)
                .getDatabase("admin")
                .runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            log.debug("Ping failed for index {}: {}", index, e.getMessage());
            return false;
        }
    }

    private String instanceName(int index) {
        var instances = properties.getInstances();
        if (instances != null && index < instances.size()) {
            return instances.get(index).getName();
        }
        return String.valueOf(index);
    }
}
