package io.github.kht2199.mongo.failover;

import com.mongodb.client.MongoClient;
import io.github.kht2199.mongo.failover.config.MongoProperties;
import io.github.kht2199.mongo.failover.core.MongoClientRegistry;
import io.github.kht2199.mongo.failover.core.MongoHealthChecker;
import io.github.kht2199.mongo.failover.core.MongoHealthStatus;
import io.github.kht2199.mongo.failover.core.MongoRouter;
import io.github.kht2199.mongo.failover.core.MongoRouterMongoDatabaseFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@ConditionalOnClass(MongoClient.class)
@EnableConfigurationProperties(MongoProperties.class)
public class MongoFailoverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MongoClientRegistry mongoClientRegistry(MongoProperties properties) {
        return new MongoClientRegistry(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoHealthStatus mongoHealthStatus(MongoProperties properties) {
        return new MongoHealthStatus(properties.getInstances().size());
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoRouter mongoRouter(MongoClientRegistry registry,
                                   MongoProperties properties,
                                   MongoHealthStatus mongoHealthStatus) {
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
                                                  MongoHealthStatus mongoHealthStatus) {
        return new MongoHealthChecker(registry, router, properties, mongoHealthStatus);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "mongodb.failover.scheduling.enabled", matchIfMissing = true)
    @EnableScheduling
    static class MongoFailoverSchedulingConfiguration {
    }
}
