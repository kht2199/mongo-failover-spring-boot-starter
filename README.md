# mongo-failover-spring-boot-starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kht2199/mongo-failover-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.kht2199/mongo-failover-spring-boot-starter)

Spring Boot Starter for multiple MongoDB connections with sticky failover.

[한국어 문서 보기](README.ko.md)

## Features

- **Multiple MongoDB instances** — configure N primary/secondary instances
- **Sticky routing** — all requests go to the current active instance
- **Automatic failover** — health checker detects failures and switches to the next healthy instance
- **Auto-configuration** — zero boilerplate, just add the dependency and configure properties
- **Spring Data MongoDB compatible** — registers `MongoDatabaseFactory` for `MongoTemplate` support

## Requirements

- Java 21+
- Spring Boot 3.x

## Installation

### Gradle

```kotlin
dependencies {
    implementation("io.github.kht2199:mongo-failover-spring-boot-starter:1.1.0")
    implementation("org.mongodb:mongodb-driver-sync:5.5.2")
}
```

### Maven

```xml
<dependency>
  <groupId>io.github.kht2199</groupId>
  <artifactId>mongo-failover-spring-boot-starter</artifactId>
  <version>1.1.0</version>
</dependency>
<dependency>
  <groupId>org.mongodb</groupId>
  <artifactId>mongodb-driver-sync</artifactId>
  <version>5.5.2</version>
</dependency>
```

## Configuration

Add to `application.yml`:

```yaml
mongodb:
  database: mydb
  instances:
    - name: primary
      uri: mongodb://localhost:27017
    - name: secondary
      uri: mongodb://localhost:27018
  connect-timeout-ms: 3000
  server-selection-timeout-ms: 3000
  health-check-interval-ms: 10000
```

Exclude Spring Boot's default MongoDB auto-configuration:

```java
@SpringBootApplication(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

## Usage

### Direct database access

```java
@Repository
@RequiredArgsConstructor
public class MyRepository {

    private final MongoRouter router;

    public void save(MyDocument doc) {
        router.getDatabase()
              .getCollection("my_collection")
              .insertOne(toBson(doc));
    }
}
```

### MongoTemplate (Spring Data MongoDB)

`MongoDatabaseFactory` is automatically registered, so `MongoTemplate` works out of the box:

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final MongoTemplate mongoTemplate;

    public MyDocument save(MyDocument doc) {
        return mongoTemplate.save(doc);
    }
}
```

## How Failover Works

1. `MongoHealthChecker` runs on a fixed interval (default 10s) and pings each instance.
2. When the **active** instance fails, `MongoRouter.failover()` is called.
3. The router advances `currentIndex` to the next healthy instance (CAS — thread-safe).
4. All subsequent requests are routed to the new active instance.
5. When the failed instance recovers, it is marked healthy again and may be used in future failovers.

```
Primary ──● (active)           Secondary ──○
         ↓ fails
Primary ──○                   Secondary ──● (active, after failover)
```

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `mongodb.database` | — | Database name (required) |
| `mongodb.instances` | — | List of MongoDB instances (required, min 1) |
| `mongodb.instances[].name` | — | Instance name for logging |
| `mongodb.instances[].uri` | — | MongoDB connection URI |
| `mongodb.connect-timeout-ms` | `3000` | Connection timeout (ms) |
| `mongodb.server-selection-timeout-ms` | `3000` | Server selection timeout (ms) |
| `mongodb.health-check-interval-ms` | `10000` | Health check interval (ms) |
| `mongodb.failover.scheduling.enabled` | `true` | Enable/disable automatic health checking |

## Startup Validation

If mandatory configuration is missing, the application will **fail at startup** with a clear error:

```
Binding validation errors on mongodb
 - Field error on field 'database': must not be blank
 - Field error on field 'instances': must not be empty
```

## Disabling the Scheduler

To disable automatic health checking (e.g., in tests):

```yaml
mongodb:
  failover:
    scheduling:
      enabled: false
```

Or set a very long interval:

```yaml
mongodb:
  health-check-interval-ms: 600000
```

## License

Apache-2.0
