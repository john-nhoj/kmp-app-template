package com.example.template.feature.home.data.datasource

import platform.UIKit.UIDevice

internal actual fun devicePlatformInfoDataSource(): PlatformInfoDataSource = PlatformInfoDataSource {
    val device = UIDevice.currentDevice
    "${device.systemName} ${device.systemVersion}"
}
