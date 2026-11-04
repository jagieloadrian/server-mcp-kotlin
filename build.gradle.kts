plugins {
    kotlin("jvm") version "2.4.21"
    application
}

repositories { mavenCentral() }

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk-server:0.16.0")
    implementation("com.github.sya-ri:kgit:1.2.2")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:7.7.1.202607240634-r")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ssh.apache.agent:7.7.1.202607240634-r")
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.kotest:kotest-assertions-core:6.2.6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("io.modelcontextprotocol:kotlin-sdk-client:0.16.0")
    testImplementation("io.modelcontextprotocol:kotlin-sdk-testing:0.16.0")
}

kotlin { jvmToolchain(21) }

application { mainClass = "MainKt" }

tasks.test { useJUnitPlatform() }
