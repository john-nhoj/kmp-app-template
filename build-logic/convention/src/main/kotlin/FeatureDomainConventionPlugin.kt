import com.example.template.buildlogic.library
import com.example.template.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Domain layer: entities, repository contracts, use cases.
 * Pure Kotlin. Only coroutines are allowed; no Android, no DI framework, no data/presentation.
 */
class FeatureDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("template.kmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                api(libs.library("kotlinx-coroutines-core"))
            }
        }
    }
}
