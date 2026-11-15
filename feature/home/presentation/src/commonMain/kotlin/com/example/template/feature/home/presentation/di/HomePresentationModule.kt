package com.example.template.feature.home.presentation.di

import com.example.template.feature.home.domain.usecase.GetGreetingUseCase
import com.example.template.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Use cases + ViewModels of the home feature. Registered in `:shared` (`AppModules.kt`). */
val homePresentationModule = module {
    factoryOf(::GetGreetingUseCase)
    viewModelOf(::HomeViewModel)
}
