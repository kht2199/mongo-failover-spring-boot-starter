plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kht2199"
version = "1.0.0"

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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.10")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.hibernate.validator:hibernate-validator")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
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
