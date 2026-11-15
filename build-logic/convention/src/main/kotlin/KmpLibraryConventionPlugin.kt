import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.example.template.buildlogic.JDK_VERSION
import com.example.template.buildlogic.JVM_TARGET
import com.example.template.buildlogic.derivedNamespace
import com.example.template.buildlogic.library
import com.example.template.buildlogic.libs
import com.example.template.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base for every shared Kotlin module: Android + iOS targets, common test dependencies,
 * quality tooling and coverage.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        pluginManager.apply("org.jetbrains.kotlinx.kover")
        pluginManager.apply("template.quality")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(JDK_VERSION)

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = derivedNamespace
                compileSdk = libs.version("android-compileSdk").toInt()
                minSdk = libs.version("android-minSdk").toInt()
                compilerOptions { jvmTarget.set(JVM_TARGET) }
                withHostTest { isReturnDefaultValues = true }
            }

            iosArm64()
            iosSimulatorArm64()

            compilerOptions {
                allWarningsAsErrors.set(true)
            }

            sourceSets.getByName("commonTest").dependencies {
                implementation(kotlin("test"))
                implementation(libs.library("kotlinx-coroutines-test"))
                implementation(libs.library("turbine"))
            }
        }
    }
}
