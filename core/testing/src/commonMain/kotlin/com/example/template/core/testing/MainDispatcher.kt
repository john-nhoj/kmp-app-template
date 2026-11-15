package com.example.template.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Runs [testBody] with `Dispatchers.Main` replaced by a [TestDispatcher].
 *
 * Required whenever the code under test uses `viewModelScope`, which dispatches on Main.
 * Common code cannot use JUnit rules, so this is the multiplatform equivalent of a `MainDispatcherRule`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runTestWithMainDispatcher(
    dispatcher: TestDispatcher = StandardTestDispatcher(),
    testBody: suspend TestScope.() -> Unit,
) {
    Dispatchers.setMain(dispatcher)
    try {
        runTest(dispatcher) { testBody() }
    } finally {
        Dispatchers.resetMain()
    }
}
