package io.github.kht2199.mongo.failover.core;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.kht2199.mongo.failover.config.MongoInstanceConfig;
import io.github.kht2199.mongo.failover.config.MongoProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MongoClientRegistry implements DisposableBean {

    private final MongoProperties properties;
    private List<MongoClient> clients;

    public MongoClientRegistry(MongoProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        List<MongoClient> list = new ArrayList<>();
        for (MongoInstanceConfig instance : properties.getInstances()) {
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(instance.getUri()))
                .applyToSocketSettings(b -> b
                    .connectTimeout(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS))
                .applyToClusterSettings(b -> b
                    .serverSelectionTimeout(properties.getServerSelectionTimeoutMs(), TimeUnit.MILLISECONDS))
                .build();
            list.add(MongoClients.create(settings));
            log.info("MongoDB client created: {}", instance.getName());
        }
        this.clients = Collections.unmodifiableList(list);
    }

    public MongoClient getClient(int index) {
        if (clients == null) {
            throw new IllegalStateException("MongoClientRegistry is not initialized. init() must be called before use.");
        }
        return clients.get(index);
    }

    public int size() {
        if (clients == null) {
            throw new IllegalStateException("MongoClientRegistry is not initialized. init() must be called before use.");
        }
        return clients.size();
    }

    @Override
    public void destroy() {
        if (clients != null) {
            clients.forEach(MongoClient::close);
            log.info("All MongoDB clients closed");
        }
    }
}
