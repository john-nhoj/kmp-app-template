package com.example.template.feature.home.data.datasource

import android.os.Build

internal actual fun devicePlatformInfoDataSource(): PlatformInfoDataSource = PlatformInfoDataSource {
    "Android ${Build.VERSION.RELEASE.orEmpty()}"
}
