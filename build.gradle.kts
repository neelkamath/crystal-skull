import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.breadmoirai.github-release") version "2.2.12"
    id("com.github.ben-manes.versions") version "0.33.0"
}

version = 0
application.mainClassName = "io.ktor.server.netty.EngineMain"

repositories { jcenter() }

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.neelkamath.kwikipedia:kwikipedia:0.7.2")
    implementation("com.github.javafaker:javafaker:1.0.2")
    val ktorVersion = "1.4.1"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
}

kotlin.sourceSets {
    getByName("main").kotlin.srcDirs("src/main")
    getByName("test").kotlin.srcDirs("src/test", "src/intTest")
}

tasks {
    named<Test>("test") { useJUnitPlatform() }
    withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }
    withType<Jar> {
        manifest { attributes(mapOf("Main-Class" to application.mainClassName)) }
    }
    withType<ShadowJar> {
        archiveBaseName.set("crystal-skull")
        archiveVersion.set("")
    }
}

if (gradle.startParameter.taskNames.contains("githubRelease"))
    githubRelease {
        token(property("GITHUB_TOKEN") as String)
        owner("neelkamath")
        body("Open the release asset, `redoc-static.html`, in your browser to view the HTTP API documentation.")
        overwrite(true)
        prerelease(project.version == 0)
        releaseAssets("redoc-static.html")
    }