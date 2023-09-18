import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"
    kotlin("kapt") version "1.8.20"
}

group = "org.hff"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

springBoot {
    mainClass.set("org.hff.miraiultimatrix.MiraiUltimatrixApplicationKt")
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // springboot
    implementation("org.springframework.boot:spring-boot-starter")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // mirai
    val miraiVersion = "2.16.0-RC"
    implementation("net.mamoe:mirai-core-api:$miraiVersion")
    runtimeOnly("net.mamoe:mirai-core:$miraiVersion")
    compileOnly("net.mamoe:mirai-core-utils:$miraiVersion")

    // ktor
    val ktorVersion = "2.3.4"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")

    // db
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.3.1")
    compileOnly("com.baomidou:mybatis-plus-generator:3.5.3.1")
    compileOnly("org.freemarker:freemarker:2.3.32")

    // tools
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.39")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.openpnp:opencv:4.7.0-0")

    // js
    implementation("org.javadelight:delight-nashorn-sandbox:0.2.5")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    // 微软语音
    implementation("com.microsoft.cognitiveservices.speech:client-sdk:1.32.1@jar")

    // skiko
    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X" -> "macos"
        osName.startsWith("Win") -> "windows"
        osName.startsWith("Linux") -> "linux"
        else -> error("Unsupported OS: $osName")
    }
    val targetArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: $osArch")
    }
    implementation("org.jetbrains.skiko:skiko-awt-runtime-${targetOs}-${targetArch}:0.7.80")

    // 修复插件
    implementation(files("lib/fix-protocol-version-1.11.0.mirai2.jar"))
    // 设备信息生成器插件
    implementation(files("lib/mirai-device-generator-1.3.0.mirai2.jar"))
    // 签名服务
    implementation("top.mrxiaom:qsign:1.1.0-beta")

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
