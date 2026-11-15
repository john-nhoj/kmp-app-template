package com.example.template.buildlogic

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/** JDK used to compile (auto-provisioned by Gradle if missing). */
internal const val JDK_VERSION = 21

/** JVM bytecode level for every Android/JVM compilation in the project. */
internal val JAVA_VERSION = JavaVersion.VERSION_17
internal val JVM_TARGET = JvmTarget.JVM_17
