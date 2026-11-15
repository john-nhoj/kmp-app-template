pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Downloads the JDK declared by `jvmToolchain(...)` when it is not installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "Template"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Apps
include(":androidApp")
include(":shared") // Umbrella module: composition root + iOS framework ("Shared")

// Core
include(":core:testing")

// Features: one folder per feature, split into clean-architecture layers
include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:presentation")

// Architecture tests
include(":konsist")
