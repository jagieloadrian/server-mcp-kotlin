plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jib)
    application
    jacoco
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.mcp.server)
    implementation(libs.ktor.server.cio)
    implementation(libs.kgit)
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.logstash.logback.encoder)
    runtimeOnly(libs.jgit.ssh.apache)
    runtimeOnly(libs.jgit.ssh.apache.agent)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mcp.client)
    testImplementation(libs.mcp.testing)
    testImplementation(libs.ktor.server.test.host)
}

kotlin {
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors = true }
}

application { mainClass = "MainKt" }

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/jagieloadrian/server-mcp-kotlin"
    }
    extraDirectories {
        paths {
            path {
                setFrom("standards")
                into = "/standards"
            }
        }
    }
    container {
        args = listOf("/standards")
        ports = listOf("8080")
        user = "10001:10001"
        jvmFlags =
            listOf(
                "-XX:MaxRAMPercentage=60",
                "-XX:+UseSerialGC",
                "-XX:+ExitOnOutOfMemoryError",
            )
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val coverageClasses =
    sourceSets.main
        .get()
        .output.classesDirs.asFileTree
        .matching { exclude("MainKt*") }

tasks.withType<JacocoReportBase>().configureEach { classDirectories.setFrom(coverageClasses) }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required = true }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
