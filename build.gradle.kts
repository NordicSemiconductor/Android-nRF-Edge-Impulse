/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false

    // Nordic plugins are defined in https://github.com/NordicSemiconductor/Android-Gradle-Plugins
    alias(libs.plugins.nordic.application) apply false
    alias(libs.plugins.nordic.application.compose) apply false
    alias(libs.plugins.nordic.hilt) apply false
}
