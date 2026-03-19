package io.github.kht2199.mongo.failover.exception;

public class MongoUnavailableException extends RuntimeException {
    public MongoUnavailableException(String message) {
        super(message);
    }
}
