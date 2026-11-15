// Root build file: declare plugins once (apply false) so every module shares a single classloader.
plugins {
    base // gives the root a `check` task, so `./gradlew check` also runs the global coverage gate
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
}

dependencies {
    // Aggregate coverage of every module that has tests into the root Kover report.
    kover(projects.shared)
    kover(projects.feature.home.domain)
    kover(projects.feature.home.data)
    kover(projects.feature.home.presentation)
}

kover {
    reports {
        filters {
            excludes {
                // Generated code, DI wiring and platform entry points carry no logic worth measuring.
                classes("*.di.*", "*.BuildConfig", "*ComposableSingletons*")
                annotatedBy("*Generated*")
            }
        }
        verify {
            rule("Minimum line coverage") {
                minBound(80)
            }
        }
    }
}
