import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
    kotlin("kapt") version "1.7.20"
}

group = "org.hff"
version = "0.0.1-SNAPSHOT"
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("net.mamoe:mirai-core-api:2.13.3")
    runtimeOnly("net.mamoe:mirai-core:2.13.3")
    runtimeOnly("mysql:mysql-connector-java")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    compileOnly("com.baomidou:mybatis-plus-generator:3.5.3")
    compileOnly("org.freemarker:freemarker:2.3.31")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.32")
    implementation("cn.hutool:hutool-all:5.8.11")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.javadelight:delight-nashorn-sandbox:0.2.5")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")

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
