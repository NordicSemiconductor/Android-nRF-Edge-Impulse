/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

/**
 * Classification class used for inferencing results
 * @param label Type of classification
 * @param value Value of classification, defines the certainty
 */
data class Classification(val label: String, val value: Double)