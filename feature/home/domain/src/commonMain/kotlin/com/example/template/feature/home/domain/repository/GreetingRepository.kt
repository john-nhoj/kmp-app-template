package com.example.template.feature.home.domain.repository

/** Contract implemented by the data layer. The domain only knows this interface. */
interface GreetingRepository {
    /** Raw platform description as reported by the device (may be blank). */
    suspend fun getPlatformName(): String
}
