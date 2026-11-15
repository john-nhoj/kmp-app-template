plugins {
    alias(libs.plugins.template.kmp.library)
    alias(libs.plugins.skie)
}

/*
 * Umbrella module:
 * - Composition root: lists every feature's Koin modules (AppModules.kt).
 * - Android: `:androidApp` depends on it.
 * - iOS: packaged as the `Shared` framework that the Xcode project links.
 *   Only `export`ed modules are visible to Swift without a name prefix.
 */
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.feature.home.presentation)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.feature.home.presentation)
            implementation(projects.feature.home.data)

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.koin.test)
        }
    }
}
