package com.example.template.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()

/** Base package of the app (gradle.properties `app.basePackage`). */
internal val Project.basePackage: String
    get() = providers.gradleProperty("app.basePackage").get()

/**
 * Android namespace derived from the Gradle path, so it never has to be written by hand:
 * `:feature:home:domain` -> `com.example.template.feature.home.domain`.
 */
internal val Project.derivedNamespace: String
    get() = basePackage + path.replace(':', '.').replace('-', '_')
