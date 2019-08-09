plugins {
    kotlin("jvm") version "1.3.41"
    application
}

application.mainClassName = "io.ktor.server.netty.EngineMain"

repositories { jcenter() }

dependencies {
    val ktorVersion = "1.2.3"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.neelkamath.kwikipedia:kwikipedia:0.1.3")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

kotlin.sourceSets {
    getByName("main").kotlin.srcDirs("src/main")
    getByName("test").kotlin.srcDirs("src/test")
}