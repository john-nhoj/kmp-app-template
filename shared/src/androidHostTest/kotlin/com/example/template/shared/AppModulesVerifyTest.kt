package com.example.template.shared

import kotlin.test.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/** Statically checks that every constructor dependency in the graph has a definition. */
class AppModulesVerifyTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun graphIsComplete() {
        module { includes(appModules) }.verify()
    }
}
