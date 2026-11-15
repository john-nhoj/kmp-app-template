package com.example.template.feature.home.presentation

import app.cash.turbine.test
import com.example.template.core.testing.runTestWithMainDispatcher
import com.example.template.feature.home.domain.repository.GreetingRepository
import com.example.template.feature.home.domain.usecase.GetGreetingUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    private val repository = FakeGreetingRepository()

    private fun createViewModel() = HomeViewModel(GetGreetingUseCase(repository))

    @Test
    fun emitsLoadingThenContent() = runTestWithMainDispatcher {
        repository.platformName = "Android 16"

        createViewModel().uiState.test {
            assertEquals(HomeUiState(isLoading = true), awaitItem())
            assertEquals(HomeUiState(isLoading = false, platformName = "Android 16"), awaitItem())
        }
    }

    @Test
    fun emitsErrorWhenRepositoryFails() = runTestWithMainDispatcher {
        repository.failure = IllegalStateException("boom")

        createViewModel().uiState.test {
            assertEquals(HomeUiState(isLoading = true), awaitItem())
            assertEquals(HomeUiState(isLoading = false, isError = true), awaitItem())
        }
    }

    @Test
    fun refreshRecoversFromError() = runTestWithMainDispatcher {
        repository.failure = IllegalStateException("boom")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2) // loading, error
            repository.failure = null
            repository.platformName = "iOS 26"

            viewModel.onRefresh()

            assertEquals(HomeUiState(isLoading = true), awaitItem())
            assertEquals(HomeUiState(isLoading = false, platformName = "iOS 26"), awaitItem())
        }
    }

    private class FakeGreetingRepository : GreetingRepository {
        var platformName = ""
        var failure: Throwable? = null

        override suspend fun getPlatformName(): String {
            failure?.let { throw it }
            return platformName
        }
    }
}
