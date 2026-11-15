package com.example.template.feature.home.domain.model

/**
 * What the home screen greets the user with.
 *
 * @property platformName Human-readable OS name and version, or `null` when unknown.
 * The UI turns it into a localized sentence, so the domain never builds user-facing text.
 */
data class Greeting(val platformName: String?)
