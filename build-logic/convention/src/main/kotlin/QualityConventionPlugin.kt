import com.example.template.buildlogic.library
import com.example.template.buildlogic.libs
import com.example.template.buildlogic.version
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Static analysis for every Kotlin module:
 * - ktlint (formatting + Compose rules): `./gradlew ktlintCheck` / `ktlintFormat`
 * - detekt (code smells, complexity, Compose rules): `./gradlew detekt`
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<KtlintExtension> {
            version.set(libs.version("ktlint"))
            android.set(false)
            filter {
                exclude { it.file.path.contains("/build/") }
            }
        }

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            parallel = true
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            // Scan every source set (commonMain, androidMain, iosMain, tests...).
            source.setFrom(files("src"))
        }

        tasks.withType<Detekt>().configureEach {
            exclude("**/build/**")
            reports {
                html.required.set(true)
                sarif.required.set(true)
                xml.required.set(false)
                txt.required.set(false)
            }
        }

        // detekt 1.x embeds Kotlin 2.0.x; keep its classpath from being upgraded to the project's Kotlin.
        configurations.matching { it.name == "detekt" }.configureEach {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") useVersion(DETEKT_KOTLIN_VERSION)
            }
        }

        dependencies {
            add("detektPlugins", libs.library("compose-rules-detekt"))
            add("ktlintRuleset", libs.library("compose-rules-ktlint"))
        }
    }

    private companion object {
        const val DETEKT_KOTLIN_VERSION = "2.0.21"
    }
}
