package com.example.template.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.template.feature.home.presentation.HomeViewModel
import org.koin.mp.KoinPlatform

/**
 * Owns the ViewModels of one SwiftUI screen. Swift calls [clear] when the screen goes away,
 * which cancels each ViewModel's `viewModelScope` (same lifecycle as on Android).
 */
class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun clear() = viewModelStore.clear()
}

/**
 * Swift-facing ViewModel factory. Add one function per ViewModel:
 * `ViewModels.shared.home(owner: owner)`.
 */
object ViewModels {
    fun home(owner: IosViewModelStoreOwner): HomeViewModel = owner.resolve()

    private inline fun <reified VM : ViewModel> IosViewModelStoreOwner.resolve(): VM {
        val factory = viewModelFactory { initializer { KoinPlatform.getKoin().get<VM>() } }
        return ViewModelProvider.create(this, factory)[VM::class]
    }
}
