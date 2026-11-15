package com.example.template.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.template.feature.home.domain.usecase.GetGreetingUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Shared by Android (Compose) and iOS (SwiftUI). Exposes one [StateFlow] of [HomeUiState]. */
class HomeViewModel(private val getGreeting: GetGreetingUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** User asked to reload the greeting (pull-to-refresh, refresh button...). */
    fun onRefresh() = load()

    // The ViewModel is the boundary where any failure becomes UI state, hence the broad catch.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun load() {
        _uiState.update { it.copy(isLoading = true, isError = false) }
        viewModelScope.launch {
            try {
                val greeting = getGreeting()
                _uiState.update { HomeUiState(isLoading = false, platformName = greeting.platformName) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }
}
