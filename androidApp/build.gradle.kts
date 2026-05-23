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
        versionName = "1.0.1"
        val url = (gradle.extra["smart_home.base_url"] as? String)
            ?.takeIf { it.isNotBlank() }
            ?: error("Property 'smart_home.base_url' is required: set it in local.properties or via the SMART_HOME_BASE_URL environment variable.")
        buildConfigField("String", "BASE_URL", "\"$url\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    val keystorePath = System.getenv("KEYSTORE_PATH")
    if (!keystorePath.isNullOrBlank()) {
        val keystoreFile = file(keystorePath)
        if (!keystoreFile.exists()) error("Keystore file not found at $keystorePath")
        signingConfigs {
            create("release") {
                storeFile = keystoreFile
                storePassword = System.getenv("STORE_PASSWORD")?.takeIf { it.isNotBlank() } ?: error("STORE_PASSWORD env var is required")
                keyAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() } ?: error("KEY_ALIAS env var is required")
                keyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() } ?: error("KEY_PASSWORD env var is required")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}