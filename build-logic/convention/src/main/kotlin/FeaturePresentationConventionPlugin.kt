import com.example.template.buildlogic.library
import com.example.template.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Presentation layer: shared ViewModels + UI state consumed by Compose (Android) and SwiftUI (iOS).
 * Depends on its feature's domain module only (never on data).
 */
class FeaturePresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("template.kmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project.dependencies.platform(libs.library("koin-bom")))
                implementation(libs.library("koin-core"))
                implementation(libs.library("koin-core-viewmodel"))
                api(libs.library("androidx-lifecycle-viewmodel"))
                api(libs.library("kotlinx-coroutines-core"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(project(":core:testing"))
            }
        }
    }
}
