import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    kotlin("plugin.jpa") version "1.4.32"
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
    id("com.github.ben-manes.versions").version("0.38.0")
}

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    fun spring(artifactSuffix: String) {
        implementation("org.springframework.boot:spring-boot-starter-$artifactSuffix")
    }
    spring("web")
    spring("data-jpa")
    spring("security")
    implementation("com.auth0:java-jwt:3.15.0")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.2")
    runtimeOnly("com.h2database:h2")
    implementation("javax.annotation:jsr250-api:1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

    // jackson: dont upgrade to 2.12.3 as spring boot will have binary incompatibilities
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    // logging
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    // not working properly with kotlin ... testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.9.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.1.0")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.xmlunit:xmlunit-core:2.8.2")
    testImplementation("org.xmlunit:xmlunit-matchers:2.8.2")
    // spring's TestRestTemplate uses default JDK http client, which is not able to read body from 401 responses
    testRuntimeOnly("org.apache.httpcomponents:httpclient:4.5.13")
}


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

detekt {
    config = files("src/config/detekt.yml")
    reports {
        html {
            enabled = true
        }
        xml {
            enabled = false
        }
        txt {
            enabled = false
        }
    }
}

// ./gradlew dependencyUpdates
tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = false
}
