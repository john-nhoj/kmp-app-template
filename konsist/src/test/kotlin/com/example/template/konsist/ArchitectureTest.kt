package com.example.template.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withParentNamed
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * Clean-architecture rules, checked on every build (`./gradlew :konsist:test`).
 *
 * Layers per feature: `domain` <- `data`, `domain` <- `presentation`. The domain is pure Kotlin;
 * presentation never sees data; only `:shared` (the composition root) knows every layer.
 * See docs/ARCHITECTURE.md.
 */
class ArchitectureTest {
    private val productionFiles = Konsist.scopeFromProduction().files.filter { it.isAppCode() }

    private fun KoFileDeclaration.isAppCode() = packagee?.name?.startsWith(BASE_PACKAGE) == true &&
        !packagee!!.name.startsWith("$BASE_PACKAGE.buildlogic")

    private fun KoFileDeclaration.layer(): String? = packagee?.name
        ?.removePrefix("$BASE_PACKAGE.feature.")
        ?.split('.')
        ?.getOrNull(1)
        ?.takeIf { packagee!!.name.startsWith("$BASE_PACKAGE.feature.") }

    private fun KoFileDeclaration.importsFeatureLayer(layer: String) = imports.any {
        it.name.startsWith("$BASE_PACKAGE.feature.") && it.name.split('.').contains(layer)
    }

    @Test
    fun `domain layer is pure Kotlin`() {
        productionFiles.filter { it.layer() == "domain" }.assertFalse { file ->
            file.imports.any { import -> FORBIDDEN_IN_DOMAIN.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `domain layer does not depend on data or presentation`() {
        productionFiles.filter { it.layer() == "domain" }.assertFalse {
            it.importsFeatureLayer("data") || it.importsFeatureLayer("presentation")
        }
    }

    @Test
    fun `presentation layer does not depend on data`() {
        productionFiles.filter { it.layer() == "presentation" }.assertFalse { it.importsFeatureLayer("data") }
    }

    @Test
    fun `data layer does not depend on presentation`() {
        productionFiles.filter { it.layer() == "data" }.assertFalse { it.importsFeatureLayer("presentation") }
    }

    @Test
    fun `shared common code has no platform imports`() {
        productionFiles.filter { "/commonMain/" in it.path }.assertFalse { file ->
            file.imports.any { it.name.startsWith("android.") || it.name.startsWith("platform.") }
        }
    }

    @Test
    fun `features do not depend on other features`() {
        productionFiles.filter { it.layer() != null }.assertFalse { file ->
            val ownFeature = file.featureName()
            file.imports.any { import ->
                import.name.startsWith("$BASE_PACKAGE.feature.") &&
                    import.name.removePrefix("$BASE_PACKAGE.feature.").substringBefore('.') != ownFeature
            }
        }
    }

    @Test
    fun `ViewModels live in presentation and are named ViewModel`() {
        Konsist.scopeFromProduction().classes().withParentNamed("ViewModel").assertTrue {
            it.name.endsWith("ViewModel") && it.resideInPackage("..presentation..")
        }
    }

    @Test
    fun `ViewModels do not expose mutable state`() {
        Konsist.scopeFromProduction().classes().withParentNamed("ViewModel").assertFalse { viewModel ->
            viewModel.properties().any { property ->
                !property.hasPrivateModifier && property.type?.name?.startsWith("Mutable") == true
            }
        }
    }

    @Test
    fun `UI state classes are immutable data classes in presentation`() {
        Konsist.scopeFromProduction().classes().withNameEndingWith("UiState").assertTrue { state ->
            state.hasDataModifier &&
                state.resideInPackage("..presentation..") &&
                state.properties().none { it.isVar }
        }
    }

    @Test
    fun `use cases live in domain and expose a single operator invoke`() {
        Konsist.scopeFromProduction().classes().withNameEndingWith("UseCase").assertTrue { useCase ->
            val publicFunctions = useCase.functions().filter { !it.hasPrivateModifier && !it.hasInternalModifier }
            useCase.resideInPackage("..domain.usecase..") &&
                publicFunctions.size == 1 &&
                publicFunctions.single().let { it.name == "invoke" && it.hasOperatorModifier }
        }
    }

    @Test
    fun `repository contracts live in domain`() {
        Konsist.scopeFromProduction().interfaces().withNameEndingWith("Repository").assertTrue {
            it.resideInPackage("..domain.repository..")
        }
    }

    @Test
    fun `repository implementations are internal to the data layer`() {
        Konsist.scopeFromProduction().classes().withNameEndingWith("Repository").assertTrue {
            it.resideInPackage("..data.repository..") && it.hasInternalModifier
        }
    }

    @Test
    fun `Koin modules live in di packages`() {
        Konsist.scopeFromProduction().properties()
            .filter { it.name.endsWith("Module") && it.isTopLevel }
            .assertTrue { it.resideInPackage("..di..") }
    }

    @Test
    fun `every use case and ViewModel has a test`() {
        val testClassNames = Konsist.scopeFromTest().classes().map { it.name }.toSet()
        Konsist.scopeFromProduction().classes()
            .filter { it.name.endsWith("UseCase") || it.name.endsWith("ViewModel") }
            .assertTrue { "${it.name}Test" in testClassNames }
    }

    private fun KoFileDeclaration.featureName(): String? = packagee?.name
        ?.takeIf { it.startsWith("$BASE_PACKAGE.feature.") }
        ?.removePrefix("$BASE_PACKAGE.feature.")
        ?.substringBefore('.')

    private companion object {
        const val BASE_PACKAGE = "com.example.template"

        val FORBIDDEN_IN_DOMAIN = listOf(
            "android.",
            "androidx.",
            "platform.",
            "org.koin.",
            "io.ktor.",
            "kotlinx.serialization.",
            "androidx.room.",
            "app.cash.sqldelight.",
        )
    }
}
