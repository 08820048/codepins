plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3" // 官方最新插件开发插件
}

group = "cn.cn.codepins"
version = "1.1.2"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.jetbrains.team/maven/p/intellij-plugin-verifier/intellij-plugin-structure") }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.xmlgraphics:batik-all:1.17") // SVG处理
    implementation("org.apache.pdfbox:pdfbox:2.0.29") // PDF处理
    implementation("com.google.zxing:core:3.5.2") // 二维码生成
    implementation("com.google.zxing:javase:3.5.2") // 二维码生成Java实现
    implementation("org.kohsuke:github-api:1.314") // GitHub API

    // 我们将使用自己的许可证实现
}

intellij {
    version.set("2024.1") // IntelliJ IDEA 最新稳定版本
    type.set("IC")        // IC = Community Edition, IU = Ultimate
    plugins.set(listOf()) // 可在此添加平台插件依赖（如 terminal、git 等）
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks {
    patchPluginXml {
        sinceBuild.set("241") // 支持 IntelliJ 2024.1 起
        untilBuild.set("252.*") // 支持到 IntelliJ 2025.2
    }
}