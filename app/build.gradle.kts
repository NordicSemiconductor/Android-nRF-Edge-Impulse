/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt
    alias(libs.plugins.nordic.application.compose)
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidHiltConventionPlugin.kt
    alias(libs.plugins.nordic.hilt)
}


android {
    namespace = "no.nordicsemi.android.ei"
    defaultConfig {
        applicationId = "no.nordicsemi.android.nrfei"
    }
}

dependencies {
    implementation(libs.nordic.ui)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.ble.ktx)
    implementation(libs.nordic.mcumgr.ble)
    // Added as a workaround for missing class when using slf4j from the current mcumgr library
    implementation("org.slf4j:slf4j-nop:1.7.30")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(libs.paging.common)

    implementation(libs.kotlinx.datetime)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.accompanist.permissions)

    implementation(libs.coil.kt.compose)
    implementation(libs.gson)
    testImplementation("com.google.truth:truth:1.4.2")

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.espresso.core)
}
