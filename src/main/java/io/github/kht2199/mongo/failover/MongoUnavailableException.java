package io.github.kht2199.mongo.failover;

public class MongoUnavailableException extends RuntimeException {
    public MongoUnavailableException(String message) {
        super(message);
    }
}
