plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3" // 官方最新插件开发插件
}

group = "cn.cn.codepins"
version = "1.1.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1") // IntelliJ IDEA 最新稳定版本
    type.set("IC")        // IC = Community Edition, IU = Ultimate
    plugins.set(listOf()) // 可在此添加平台插件依赖（如 terminal、git 等）
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    patchPluginXml {
        sinceBuild.set("231") // 支持 IntelliJ 2023.1 起
        untilBuild.set("252.*")
    }
}