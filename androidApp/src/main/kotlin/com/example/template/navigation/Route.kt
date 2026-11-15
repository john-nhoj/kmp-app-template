package com.example.template.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe destinations (Navigation 3). Add one `@Serializable` entry per screen; pass
 * arguments as properties, e.g. `@Serializable data class Detail(val id: String) : Route`.
 */
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route
}
