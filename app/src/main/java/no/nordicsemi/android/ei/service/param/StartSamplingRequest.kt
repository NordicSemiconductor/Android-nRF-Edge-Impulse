/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Start sampling request body params
 * @see https://docs.edgeimpulse.com/reference#startsampling
 *
 * @param label         Label to be used during sampling.
 * @param lengthMs      Requested length of the sample (in ms).
 * @param category      Which acquisition category to sample data into.
 * @param intervalMs    Interval between samples (can be calculated like 1/hz * 1000)
 * @param sensor        The sensor to sample from.
 */
data class StartSamplingRequest(
    val label: String,
    val lengthMs: Number,
    val category: String,
    val intervalMs: Number,
    val sensor: String
)
