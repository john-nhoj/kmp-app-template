package com.example.template.feature.home.data.repository

import com.example.template.feature.home.data.datasource.PlatformInfoDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class DefaultGreetingRepositoryTest {
    @Test
    fun returnsPlatformNameFromDataSource() = runTest {
        val repository = DefaultGreetingRepository(
            platformInfo = PlatformInfoDataSource { "iOS 26.1" },
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        assertEquals("iOS 26.1", repository.getPlatformName())
    }
}
