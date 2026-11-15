plugins {
    alias(libs.plugins.template.feature.data)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.home.domain)
        }
    }
}
