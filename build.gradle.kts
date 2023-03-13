import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
    kotlin("kapt") version "1.7.20"
}

group = "org.hff"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

springBoot {
    mainClass.set("org.hff.miraiomnitrix.MiraiOmnitrixApplicationKt")
}

repositories {
    mavenCentral()
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "${targetOs}-${targetArch}"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("net.mamoe:mirai-core-api:2.14.0")
    runtimeOnly("net.mamoe:mirai-core:2.14.0")
    compileOnly("net.mamoe:mirai-core-utils:2.14.0")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    compileOnly("com.baomidou:mybatis-plus-generator:3.5.3")
    compileOnly("org.freemarker:freemarker:2.3.31")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.32")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.javadelight:delight-nashorn-sandbox:0.2.5")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("com.microsoft.cognitiveservices.speech:client-sdk:1.25.0@jar")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:0.7.54")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
