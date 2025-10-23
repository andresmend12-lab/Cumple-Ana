import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.android") version "1.9.22"
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CumpleAna"
include(":app")
