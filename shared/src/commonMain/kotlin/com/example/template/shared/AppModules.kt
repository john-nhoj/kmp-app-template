package com.example.template.shared

import com.example.template.feature.home.data.di.homeDataModule
import com.example.template.feature.home.presentation.di.homePresentationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module of the app. Add each new feature's data + presentation modules here. */
val appModules: List<Module> = listOf(
    homeDataModule,
    homePresentationModule,
)

/**
 * Starts dependency injection. Call exactly once at app start:
 * Android `TemplateApplication.onCreate`, iOS `AppInitializer.start()`.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(appModules)
}
