val localProps = java.util.Properties().apply {
    rootDir.resolve("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
gradle.extra["smart_home.base_url"] = localProps.getProperty("smart_home.base_url")
    ?: System.getenv("SMART_HOME_BASE_URL")
    ?: ""

rootProject.name = "smart-home-ui"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")
include(":shared")