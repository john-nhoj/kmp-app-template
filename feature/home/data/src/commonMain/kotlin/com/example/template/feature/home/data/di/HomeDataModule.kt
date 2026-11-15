package com.example.template.feature.home.data.di

import com.example.template.feature.home.data.datasource.devicePlatformInfoDataSource
import com.example.template.feature.home.data.repository.DefaultGreetingRepository
import com.example.template.feature.home.domain.repository.GreetingRepository
import org.koin.dsl.module

/** Data-layer bindings for the home feature. Registered in `:shared` (`AppModules.kt`). */
val homeDataModule = module {
    single<GreetingRepository> { DefaultGreetingRepository(platformInfo = devicePlatformInfoDataSource()) }
}
