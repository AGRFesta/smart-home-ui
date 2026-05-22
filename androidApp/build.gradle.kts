plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "org.agrfesta.sh.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures { buildConfig = true }

    defaultConfig {
        applicationId = "org.agrfesta.sh.ui"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        val url = (gradle.extra["smart_home.base_url"] as? String)
            ?.takeIf { it.isNotBlank() }
            ?: error("Property 'smart_home.base_url' is required in local.properties. See README.md.")
        buildConfigField("String", "BASE_URL", "\"$url\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}