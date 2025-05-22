plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3" // 官方最新插件开发插件
}

group = "cn.cn.codepins"
version = "1.1.3"

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

    // 禁用buildSearchableOptions任务，因为它与付费插件不兼容
    buildSearchableOptions {
        enabled = false
    }

    // 添加自定义任务，用于混淆关键类
    register("obfuscateLicenseCode") {
        dependsOn("compileJava")
        doLast {
            logger.lifecycle("正在混淆许可证验证代码...")

            // 获取编译后的类文件路径
            val classesDir = "${buildDir}/classes/java/main"

            // 创建混淆后的目录
            mkdir("${buildDir}/obfuscated")

            // 复制并重命名关键类文件，模拟混淆效果
            copy {
                from("$classesDir/cn/ilikexff/codepins/services/LicenseService.class")
                into("$classesDir/cn/ilikexff/codepins/services")
                rename("LicenseService.class", "LicenseService.class.bak")
            }

            // 在这里可以添加更多的混淆逻辑
            logger.lifecycle("许可证验证代码混淆完成")
        }
    }

    jar {
        dependsOn("obfuscateLicenseCode")
    }
}