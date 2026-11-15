package com.example.template.feature.home.domain.usecase

import com.example.template.feature.home.domain.model.Greeting
import com.example.template.feature.home.domain.repository.GreetingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GetGreetingUseCaseTest {
    @Test
    fun returnsTrimmedPlatformName() = runTest {
        val useCase = GetGreetingUseCase(FakeGreetingRepository(platformName = "  Android 16 "))

        assertEquals(Greeting(platformName = "Android 16"), useCase())
    }

    @Test
    fun returnsNullPlatformNameWhenBlank() = runTest {
        val useCase = GetGreetingUseCase(FakeGreetingRepository(platformName = "   "))

        assertEquals(Greeting(platformName = null), useCase())
    }

    private class FakeGreetingRepository(private val platformName: String) : GreetingRepository {
        override suspend fun getPlatformName(): String = platformName
    }
}
