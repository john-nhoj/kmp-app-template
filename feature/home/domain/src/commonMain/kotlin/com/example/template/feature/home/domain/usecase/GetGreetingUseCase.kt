package com.example.template.feature.home.domain.usecase

import com.example.template.feature.home.domain.model.Greeting
import com.example.template.feature.home.domain.repository.GreetingRepository

/** Builds the [Greeting] shown on the home screen, normalizing the raw platform data. */
class GetGreetingUseCase(private val repository: GreetingRepository) {
    suspend operator fun invoke(): Greeting {
        val platformName = repository.getPlatformName().trim().ifEmpty { null }
        return Greeting(platformName = platformName)
    }
}
