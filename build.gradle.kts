import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.allopen") version "1.9.22"
    kotlin("plugin.noarg") version "1.9.22"
    kotlin("kapt") version "1.9.22"
}

group = "com.hsik0225.ai"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
}

ext["log4j2.version"] = "2.20.0"

dependencies {
    // spring boot
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow") {
        exclude("io.undertow", "undertow-websockets-jsr")
    }

    // Spring AI
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
    implementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
}

noArg {
    invokeInitializers = true
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
