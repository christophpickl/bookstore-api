import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
    id("com.github.ben-manes.versions").version("0.38.0")
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
    implementation("com.auth0:java-jwt:3.15.0")

    // dont upgrade to 2.12.3 as spring boot will have binary incompatibilities
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")


    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    // not working properly with kotlin ... testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.9.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.1.0")
}


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("InlineClasses")
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
