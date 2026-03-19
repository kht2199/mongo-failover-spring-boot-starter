package io.github.kht2199.mongo.failover.core;

import java.util.concurrent.atomic.AtomicBoolean;

public class MongoHealthStatus {

    private final AtomicBoolean[] statuses;

    public MongoHealthStatus(int size) {
        this.statuses = new AtomicBoolean[size];
        for (int i = 0; i < size; i++) {
            this.statuses[i] = new AtomicBoolean(true);
        }
    }

    public AtomicBoolean get(int index) {
        return statuses[index];
    }

    public int size() {
        return statuses.length;
    }
}
