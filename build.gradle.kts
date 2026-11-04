plugins {
    kotlin("jvm") version "2.4.21"
    application
}

repositories { mavenCentral() }

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk-server:0.16.0")
    testImplementation(kotlin("test"))
}

kotlin { jvmToolchain(21) }

application { mainClass = "MainKt" }

tasks.test { useJUnitPlatform() }
