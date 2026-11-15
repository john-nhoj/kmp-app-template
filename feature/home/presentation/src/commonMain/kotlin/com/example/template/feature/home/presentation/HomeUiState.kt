package com.example.template.feature.home.presentation

/**
 * Everything the home screen needs to render. Immutable; the UI never mutates it.
 *
 * @property platformName `null` means "unknown": show a generic greeting.
 */
data class HomeUiState(val isLoading: Boolean = true, val platformName: String? = null, val isError: Boolean = false)
