plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.template.quality)
}

// Architecture tests: they read the source code of the whole repository, so always re-run them.
tasks.test {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(kotlin("test-junit5"))
}

kotlin {
    jvmToolchain(21)
}
