plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.kht2199"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
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
    }
    repositories {
        maven {
            name = "central"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload")
            credentials {
                username = providers.gradleProperty("sonatype.username").orNull
                    ?: System.getenv("SONATYPE_USERNAME")
                password = providers.gradleProperty("sonatype.password").orNull
                    ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signing.key").orNull
        ?: System.getenv("SIGNING_KEY")
    val signingPassword = providers.gradleProperty("signing.password").orNull
        ?: System.getenv("SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["mavenJava"])
}
