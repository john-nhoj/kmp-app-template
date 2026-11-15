package com.example.template.feature.home.data.datasource

/** Reads information about the device the app runs on. Platform-specific (see `actual`s). */
internal fun interface PlatformInfoDataSource {
    fun platformName(): String
}

/** Platform implementation of [PlatformInfoDataSource]. */
internal expect fun devicePlatformInfoDataSource(): PlatformInfoDataSource
