package io.github.kht2199.mongo.failover;

import com.mongodb.client.MongoClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.atomic.AtomicBoolean;

@AutoConfiguration
@ConditionalOnClass(MongoClient.class)
@EnableConfigurationProperties(MongoProperties.class)
@EnableScheduling
public class MongoFailoverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MongoClientRegistry mongoClientRegistry(MongoProperties properties) {
        return new MongoClientRegistry(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AtomicBoolean[] mongoHealthStatus(MongoProperties properties) {
        int size = properties.getInstances().size();
        AtomicBoolean[] status = new AtomicBoolean[size];
        for (int i = 0; i < size; i++) status[i] = new AtomicBoolean(true);
        return status;
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoRouter mongoRouter(MongoClientRegistry registry,
                                   MongoProperties properties,
                                   AtomicBoolean[] mongoHealthStatus) {
        return new MongoRouter(registry, properties, mongoHealthStatus);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoRouter router) {
        return new MongoRouterMongoDatabaseFactory(router);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoHealthChecker mongoHealthChecker(MongoClientRegistry registry,
                                                  MongoRouter router,
                                                  MongoProperties properties,
                                                  AtomicBoolean[] mongoHealthStatus) {
        return new MongoHealthChecker(registry, router, properties, mongoHealthStatus);
    }
}
