import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("kapt") version "1.6.21"
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
    implementation("net.mamoe:mirai-core-api:2.12.3")
    runtimeOnly("net.mamoe:mirai-core:2.12.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
//    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("mysql:mysql-connector-java")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.32")
    implementation("cn.hutool:hutool-all:5.8.6")
    compileOnly("org.jsoup:jsoup:1.15.3")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    compileOnly("com.baomidou:mybatis-plus-generator:3.5.3")
    compileOnly("org.freemarker:freemarker:2.3.31")
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
