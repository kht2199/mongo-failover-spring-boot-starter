package io.github.kht2199.mongo.failover;

import io.github.kht2199.mongo.failover.core.MongoHealthStatus;
import io.github.kht2199.mongo.failover.core.MongoRouter;
import io.github.kht2199.mongo.failover.core.MongoRouterMongoDatabaseFactory;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = TestApplication.class)
class MongoAutoConfigurationIntegrationTest {

    @Container
    static MongoDBContainer mongo1 = new MongoDBContainer("mongo:7");

    @Container
    static MongoDBContainer mongo2 = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.instances[0].name", () -> "primary");
        registry.add("mongodb.instances[0].uri", mongo1::getConnectionString);
        registry.add("mongodb.instances[1].name", () -> "secondary");
        registry.add("mongodb.instances[1].uri", mongo2::getConnectionString);
        registry.add("mongodb.database", () -> "testdb");
        registry.add("mongodb.health-check-interval-ms", () -> "60000");
        registry.add("mongodb.connect-timeout-ms", () -> "3000");
        registry.add("mongodb.server-selection-timeout-ms", () -> "3000");
    }

    @Autowired
    MongoRouter mongoRouter;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoDatabaseFactory mongoDatabaseFactory;

    @Autowired
    MongoHealthStatus mongoHealthStatus;

    @Test
    void autoConfigurationRegistersAllBeans() {
        assertThat(mongoRouter).isNotNull();
        assertThat(mongoTemplate).isNotNull();
        assertThat(mongoDatabaseFactory).isInstanceOf(MongoRouterMongoDatabaseFactory.class);
    }

    @Test
    void mongoTemplateCanInsertAndFind() {
        mongoTemplate.getDb().getCollection("test").drop();

        Document doc = new Document("msg", "hello");
        mongoTemplate.getDb().getCollection("test").insertOne(doc);

        long count = mongoTemplate.getDb().getCollection("test").countDocuments();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void mongoRouterStartsOnPrimaryInstance() {
        assertThat(mongoRouter.getCurrentIndex()).isEqualTo(0);
    }

    @Test
    void failoverSwitchesToSecondaryWhenPrimaryMarkedDown() {
        assertThat(mongoRouter.getCurrentIndex()).isEqualTo(0);

        // Simulate primary becoming unhealthy
        mongoHealthStatus.get(0).set(false);
        mongoRouter.failover(0);

        assertThat(mongoRouter.getCurrentIndex()).isEqualTo(1);

        // Verify we can still insert via secondary
        Document doc = new Document("failover_test", true);
        mongoTemplate.getDb().getCollection("failover").drop();
        mongoTemplate.getDb().getCollection("failover").insertOne(doc);
        assertThat(mongoTemplate.getDb().getCollection("failover").countDocuments()).isEqualTo(1);

        // Restore health status
        mongoHealthStatus.get(0).set(true);
        mongoRouter.failover(1); // reset back if needed — or just leave as secondary for test isolation
    }
}
