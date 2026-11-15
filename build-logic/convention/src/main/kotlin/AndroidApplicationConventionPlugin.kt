import com.android.build.api.dsl.ApplicationExtension
import com.example.template.buildlogic.JAVA_VERSION
import com.example.template.buildlogic.JDK_VERSION
import com.example.template.buildlogic.JVM_TARGET
import com.example.template.buildlogic.libs
import com.example.template.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Android app module: AGP (with built-in Kotlin), Jetpack Compose, quality tooling. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("template.quality")

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.version("android-compileSdk").toInt()
            defaultConfig {
                minSdk = libs.version("android-minSdk").toInt()
                targetSdk = libs.version("android-targetSdk").toInt()
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            buildFeatures {
                compose = true
                buildConfig = true
            }
            lint {
                warningsAsErrors = true
                abortOnError = true
                checkDependencies = true
                // Dependency/SDK freshness is handled by Renovate, not by failing builds.
                disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(JDK_VERSION)
            compilerOptions {
                jvmTarget.set(JVM_TARGET)
                allWarningsAsErrors.set(true)
            }
        }
    }
}
