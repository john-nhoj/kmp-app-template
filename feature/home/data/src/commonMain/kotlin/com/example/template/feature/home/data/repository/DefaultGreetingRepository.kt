package com.example.template.feature.home.data.repository

import com.example.template.feature.home.data.datasource.PlatformInfoDataSource
import com.example.template.feature.home.domain.repository.GreetingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultGreetingRepository(
    private val platformInfo: PlatformInfoDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GreetingRepository {
    override suspend fun getPlatformName(): String = withContext(ioDispatcher) {
        platformInfo.platformName()
    }
}
