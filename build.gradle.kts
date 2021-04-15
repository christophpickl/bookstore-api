import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
}

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    fun spring(artifactSuffix: String) {
        implementation("org.springframework.boot:spring-boot-starter-$artifactSuffix")
    }
    spring("web")
    spring("security")
    implementation("com.auth0:java-jwt:3.11.0")
    // data-jpa, jdbc

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    // not working properly with kotlin ... testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23")
    testImplementation("org.mockito:mockito-core:2.+")
    testImplementation("org.mockito:mockito-junit-jupiter:2.18.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.1.0")
}


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
