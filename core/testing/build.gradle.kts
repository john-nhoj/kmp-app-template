plugins {
    alias(libs.plugins.template.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(kotlin("test"))
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}
