plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.jooq) apply false
    alias(libs.plugins.ktlint) apply false
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

tasks.register("intTest") {
    description = "Runs integration tests."
    group = "verification"
    dependsOn(":infra:intTest")
}
