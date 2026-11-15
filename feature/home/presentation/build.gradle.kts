plugins {
    alias(libs.plugins.template.feature.presentation)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `api` so Swift sees domain models referenced by the exported ViewModel.
            api(projects.feature.home.domain)
        }
    }
}
