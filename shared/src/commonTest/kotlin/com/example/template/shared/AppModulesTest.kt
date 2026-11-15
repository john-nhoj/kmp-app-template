package com.example.template.shared

import com.example.template.feature.home.domain.repository.GreetingRepository
import com.example.template.feature.home.domain.usecase.GetGreetingUseCase
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.dsl.koinApplication

/**
 * Resolves the real graph on every platform (Android host + iOS simulator), catching a missing
 * binding or a broken `actual` before it crashes the app. ViewModels are covered by
 * `AppModulesVerifyTest` (they start work on creation, so they are verified statically).
 */
class AppModulesTest {
    @Test
    fun resolvesNonViewModelDependencies() {
        val koin = koinApplication { modules(appModules) }.koin

        assertNotNull(koin.get<GreetingRepository>())
        assertNotNull(koin.get<GetGreetingUseCase>())
    }
}
