plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kht2199"
version = "1.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM - compile/annotation 시에만 사용, 출판된 POM에 버전이 박히지 않음
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.5.10"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:3.5.10"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.10"))

    // Spring Boot 의존성 - 항상 사용자 프로젝트에서 제공됨
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-starter-data-mongodb")

    // 런타임에 필요 - 사용자 POM에 명시적으로 포함
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.kht2199", "mongo-failover-spring-boot-starter", version.toString())

    pom {
        name = "mongo-failover-spring-boot-starter"
        description = "Spring Boot Starter for multiple MongoDB connections with sticky failover"
        url = "https://github.com/kht2199/mongo-failover-spring-boot-starter"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "kht2199"
                name = "Taek Kim"
                email = "kht2199@gmail.com"
            }
        }
        scm {
            url = "https://github.com/kht2199/mongo-failover-spring-boot-starter"
            connection = "scm:git:git://github.com/kht2199/mongo-failover-spring-boot-starter.git"
            developerConnection = "scm:git:ssh://github.com/kht2199/mongo-failover-spring-boot-starter.git"
        }
    }
}
