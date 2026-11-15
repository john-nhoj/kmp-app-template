import com.example.template.buildlogic.library
import com.example.template.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Data layer: repository implementations and data sources (network, database, platform APIs).
 * Depends on its feature's domain module; exposes a Koin module.
 */
class FeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("template.kmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project.dependencies.platform(libs.library("koin-bom")))
                implementation(libs.library("koin-core"))
                implementation(libs.library("kotlinx-coroutines-core"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(project(":core:testing"))
            }
        }
    }
}
