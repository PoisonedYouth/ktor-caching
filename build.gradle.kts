val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val koinKtorVersion: String by project
val postgresqlVersion: String by project
val hikaricpVersion: String by project
val ehcacheVersion: String by project
val redisVersion: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}

group = "com.poisonedyouth"
version = "0.0.1"
application {
    mainClass.set("com.poisonedyouth.caching.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")
    implementation("io.insert-koin:koin-ktor:$koinKtorVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinKtorVersion")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation(platform("io.arrow-kt:arrow-stack:1.2.0-RC"))
    implementation("io.arrow-kt:arrow-core")

    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("org.ehcache:ehcache:$ehcacheVersion")

    implementation("org.redisson:redisson:$redisVersion")


    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.testcontainers:postgresql:1.18.3")
    testImplementation("org.testcontainers:testcontainers:1.18.3")
    testImplementation("org.testcontainers:junit-jupiter:1.18.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
