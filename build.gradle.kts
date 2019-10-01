plugins {
    application
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("com.github.breadmoirai.github-release") version "2.2.9"
}

version = 0

application.mainClassName = "io.ktor.server.netty.EngineMain"

repositories { jcenter() }

dependencies {
    val ktorVersion = "1.2.4"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.neelkamath.kwikipedia:kwikipedia:0.4.1")
    implementation("org.apache.opennlp:opennlp-tools:1.9.1")
    implementation("com.github.javafaker:javafaker:1.0.0")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
}

val test by tasks.getting(Test::class) { useJUnitPlatform() }

kotlin.sourceSets {
    getByName("main").kotlin.srcDirs("src/main")
    getByName("test").kotlin.srcDirs("src/test")
}

tasks.withType<Jar> {
    manifest { attributes(mapOf("Main-Class" to application.mainClassName)) }
}

if (gradle.startParameter.taskNames.contains("githubRelease")) {
    githubRelease {
        token(property("GITHUB_TOKEN") as String)
        owner("neelkamath")
        body("Download and open the release asset, `redoc-static.html`, in your browser to view the HTTP API documentation.")
        overwrite(true)
        prerelease(project.version.toString().startsWith("0"))
        releaseAssets("redoc-static.html")
    }
}