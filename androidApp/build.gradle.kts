import java.util.Properties

plugins {
    alias(libs.plugins.template.android.application)
    alias(libs.plugins.kotlin.serialization)
}

val appVersion = rootProject.file("version.txt").readText().trim()

android {
    namespace = providers.gradleProperty("app.basePackage").get()

    defaultConfig {
        applicationId = providers.gradleProperty("app.basePackage").get()
        versionName = appVersion
        // CI passes a monotonically increasing build number (github.run_number); local builds use 1.
        versionCode = providers.environmentVariable("VERSION_CODE").orNull?.toInt() ?: 1
    }

    signingConfigs {
        // Release signing comes from the environment (CI secrets) or an untracked keystore.properties.
        create("release") {
            val keystoreProperties = Properties().apply {
                rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
            }
            fun secret(env: String, property: String): String? =
                providers.environmentVariable(env).orNull ?: keystoreProperties.getProperty(property)

            secret("ANDROID_KEYSTORE_PATH", "storeFile")?.let { storeFile = file(it) }
            storePassword = secret("ANDROID_KEYSTORE_PASSWORD", "storePassword")
            keyAlias = secret("ANDROID_KEY_ALIAS", "keyAlias")
            keyPassword = secret("ANDROID_KEY_PASSWORD", "keyPassword")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
}
