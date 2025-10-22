import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.android") version "2.0.21"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            val version = requested.version
            if (version != null) {
                if (requested.id.id == "org.jetbrains.kotlin.android") {
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$version")
                }
            }
        }
    }
}

val ffmpegKitEnabled = providers.gradleProperty("enableFfmpegKit")
    .map(String::toBoolean)
    .orElse(false)
val isFfmpegEnabled = ffmpegKitEnabled.get()

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (isFfmpegEnabled) {
            maven(url = uri("https://maven.arthenica.com")) {
                content {
                    includeGroup("com.arthenica")
                }
            }
        }
    }
}

rootProject.name = "CumpleAna"
include(":app")
