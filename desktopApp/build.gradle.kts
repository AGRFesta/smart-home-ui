import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.gmazzoBuildConfig)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

buildConfig {
    packageName("org.agrfesta.sh.ui")
    val url = (gradle.extra["smart_home.base_url"] as? String)
        ?.takeIf { it.isNotBlank() }
        ?: error("Property 'smart_home.base_url' is required in local.properties. See README.md.")
    buildConfigField("String", "BASE_URL", "\"$url\"")
}

compose.desktop {
    application {
        mainClass = "org.agrfesta.sh.ui.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.agrfesta.sh.ui"
            packageVersion = "1.0.0"

            linux { iconFile.set(project.file("src/main/resources/icon.png")) }
            windows { iconFile.set(project.file("src/main/resources/icon.ico")) }
            macOS { iconFile.set(project.file("src/main/resources/icon.icns")) }
        }
    }
}