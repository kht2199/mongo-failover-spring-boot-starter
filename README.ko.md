# mongo-failover-spring-boot-starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kht2199/mongo-failover-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.kht2199/mongo-failover-spring-boot-starter)

다중 MongoDB 연결과 스티키 페일오버를 지원하는 Spring Boot Starter입니다.

[English Documentation](README.md)

## 주요 기능

- **다중 MongoDB 인스턴스** — N개의 primary/secondary 인스턴스 구성 가능
- **스티키 라우팅** — 모든 요청이 현재 활성 인스턴스로 전달
- **자동 페일오버** — 헬스체커가 장애를 감지하고 다음 정상 인스턴스로 전환
- **자동 설정** — 의존성 추가 후 properties 설정만으로 동작
- **Spring Data MongoDB 호환** — `MongoTemplate` 사용을 위한 `MongoDatabaseFactory` 자동 등록

## 요구 사항

- Java 21+
- Spring Boot 3.x

## 호환성

| 라이브러리 버전 | Spring Boot | Java |
|---|---|---|
| 1.x | 3.0 ~ 3.x | 21+ |

Spring Boot 2.x는 지원하지 않습니다 (Spring Boot 3.0에서 도입된 `@AutoConfiguration`과 `MongoDatabaseFactory`를 사용합니다).

## 설치

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

## 설정

`application.yml`에 추가:

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

Spring Boot의 기본 MongoDB 자동 설정 제외:

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

## 사용법

### 직접 데이터베이스 접근

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

`MongoDatabaseFactory`가 자동 등록되므로 `MongoTemplate`을 그대로 사용할 수 있습니다:

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

## 페일오버 동작 방식

1. `MongoHealthChecker`가 설정된 주기(기본 10초)마다 각 인스턴스에 ping을 보냅니다.
2. **활성** 인스턴스에 장애가 발생하면 `MongoRouter.failover()`가 호출됩니다.
3. 라우터가 `currentIndex`를 다음 정상 인스턴스로 전진시킵니다 (CAS — 스레드 안전).
4. 이후 모든 요청은 새로운 활성 인스턴스로 라우팅됩니다.
5. 장애 인스턴스가 복구되면 다시 healthy로 표시되어 이후 페일오버 후보가 됩니다.

```
Primary ──● (활성)             Secondary ──○
         ↓ 장애 발생
Primary ──○                   Secondary ──● (페일오버 후 활성)
```

## 설정 속성

| 속성 | 기본값 | 설명 |
|---|---|---|
| `mongodb.database` | — | 데이터베이스 이름 (필수) |
| `mongodb.instances` | — | MongoDB 인스턴스 목록 (필수, 최소 1개) |
| `mongodb.instances[].name` | — | 로그용 인스턴스 이름 |
| `mongodb.instances[].uri` | — | MongoDB 연결 URI |
| `mongodb.connect-timeout-ms` | `3000` | 연결 타임아웃 (ms) |
| `mongodb.server-selection-timeout-ms` | `3000` | 서버 선택 타임아웃 (ms) |
| `mongodb.health-check-interval-ms` | `10000` | 헬스체크 주기 (ms) |
| `mongodb.failover.scheduling.enabled` | `true` | 자동 헬스체크 활성화 여부 |

## 시작 시 유효성 검사

필수 설정이 없으면 **애플리케이션 시작 시 즉시 실패**합니다:

```
Binding validation errors on mongodb
 - Field error on field 'database': must not be blank
 - Field error on field 'instances': must not be empty
```

## 스케줄러 비활성화

자동 헬스체크를 비활성화하려면 (예: 테스트 환경):

```yaml
mongodb:
  failover:
    scheduling:
      enabled: false
```

또는 주기를 매우 길게 설정:

```yaml
mongodb:
  health-check-interval-ms: 600000
```

## 라이선스

Apache-2.0
