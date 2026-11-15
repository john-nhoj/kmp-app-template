import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.template.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.template.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.template.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("featureDomain") {
            id = libs.plugins.template.feature.domain.get().pluginId
            implementationClass = "FeatureDomainConventionPlugin"
        }
        register("featureData") {
            id = libs.plugins.template.feature.data.get().pluginId
            implementationClass = "FeatureDataConventionPlugin"
        }
        register("featurePresentation") {
            id = libs.plugins.template.feature.presentation.get().pluginId
            implementationClass = "FeaturePresentationConventionPlugin"
        }
        register("quality") {
            id = libs.plugins.template.quality.get().pluginId
            implementationClass = "QualityConventionPlugin"
        }
    }
}
