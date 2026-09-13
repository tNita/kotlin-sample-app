import nu.studer.gradle.jooq.JooqEdition
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("nu.studer.jooq")
}

description = "Book Manager application"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":shared"))
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.aws.sdk.bom))
    implementation(platform(libs.spring.cloud.aws.dependencies))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.cloud.aws.starter.integration.sqs)
    implementation(libs.spring.cloud.aws.starter.s3)
    implementation(libs.aws.sfn)
    implementation(libs.spring.integration.jdbc)
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.uuid.generator)
    jooqGenerator(libs.jooq.meta.extensions)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

jooq {
    version.set(libsCatalog.findVersion("jooq").get().requiredVersion)
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = Logging.WARN
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties =
                            listOf(
                                Property().withKey("scripts").withValue("src/main/resources/db/migration"),
                                Property().withKey("sort").withValue("semantic"),
                                Property().withKey("defaultNameCase").withValue("lower"),
                            )
                    }
                    generate.apply {
                        isDaos = false
                        isPojos = false
                        isKotlinNotNullRecordAttributes = true
                    }
                    target.apply {
                        packageName = "com.example.bookmanager.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

kotlin {
    sourceSets.named("main") {
        kotlin.srcDir(layout.buildDirectory.dir("generated-src/jooq/main"))
    }
}

ktlint {
    filter {
        // Generated jOOQ sources use the generator's formatting conventions.
        exclude { it.file.invariantSeparatorsPath.contains("/build/generated-src/jooq/") }
    }
}

val intTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

configurations.named(intTest.implementationConfigurationName) {
    extendsFrom(configurations.implementation.get())
}

configurations.named(intTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {
    add(intTest.implementationConfigurationName, platform(libs.spring.boot.dependencies))
    add(intTest.implementationConfigurationName, platform(libs.aws.sdk.bom))
    add(intTest.implementationConfigurationName, platform(libs.spring.cloud.aws.dependencies))
    add(intTest.implementationConfigurationName, libs.spring.boot.starter.test)
    add(intTest.implementationConfigurationName, libs.spring.boot.starter.flyway.test)
    add(intTest.implementationConfigurationName, libs.spring.boot.starter.jooq.test)
    add(intTest.implementationConfigurationName, libs.kotlin.test.junit5)
    add(intTest.runtimeOnlyConfigurationName, libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

tasks.register<Test>("intTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = intTest.output.classesDirs
    classpath = intTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

springBoot {
    mainClass.set("com.example.bookmanager.BookmanagerApplicationKt")
}

tasks.register<BootRun>("bootRunBatch") {
    description = "Runs the book update batch without a web server."
    group = "application"
    mainClass.set("com.example.bookmanager.bootstrap.batch.BatchApplicationKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<BootJar>("bootJarBatch") {
    description = "Builds the executable book update batch jar."
    group = "build"
    mainClass.set("com.example.bookmanager.bootstrap.batch.BatchApplicationKt")
    archiveClassifier.set("batch")
    targetJavaVersion.set(java.targetCompatibility)
    classpath(sourceSets.main.get().runtimeClasspath)
}
