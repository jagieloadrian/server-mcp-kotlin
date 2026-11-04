plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
    jacoco
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.mcp.server)
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
}

kotlin {
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors = true }
}

application { mainClass = "MainKt" }

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

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
