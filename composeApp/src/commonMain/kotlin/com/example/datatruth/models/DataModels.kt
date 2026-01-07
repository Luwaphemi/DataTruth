@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.datatruth.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * Represents data usage measured directly from the device
 */
@Serializable
data class DataUsageModel(
    val id: String,
    val timestamp: Long,
    val mobileDataBytes: Long,
    val wifiDataBytes: Long,
    val totalBytes: Long = mobileDataBytes + wifiDataBytes,
    val source: DataSourceType = DataSourceType.DEVICE
)

/**
 * Represents data usage reported by the network provider
 */
@Serializable
data class ProviderReportModel(
    val id: String,
    val timestamp: Long,
    val reportedDataBytes: Long,
    val remainingDataBytes: Long? = null,
    val dataLimitBytes: Long? = null,
    val providerName: String,
    val source: DataSourceType = DataSourceType.PROVIDER
)

/**
 * Represents a discrepancy between device usage and provider report
 */
@Serializable
data class DiscrepancyModel(
    val id: String,
    val timestamp: Long,
    val deviceMeasurement: Long,
    val providerReport: Long,
    val differenceBytes: Long,
    val differencePercentage: Double,
    val severity: DiscrepancySeverityLevel,
    val note: String? = null
)

/**
 * User-configured data plan
 */
@Serializable
data class DataPlanModel(
    val id: String,
    val providerName: String,
    val dataLimitBytes: Long,
    val billingCycleStartDay: Int,
    val alertThresholdPercentage: Double = 80.0,
    val discrepancyThresholdPercentage: Double = 5.0
)

/**
 * Aggregated usage summary for UI display
 */
@Serializable
data class UsageSummaryModel(
    val currentCycleUsage: Long,
    val providerReportedUsage: Long?,
    val dataLimit: Long?,
    val percentageUsed: Double,
    val daysRemainingInCycle: Int,
    val averageDailyUsage: Long,
    val projectedEndOfCycleUsage: Long,
    val hasDiscrepancy: Boolean,
    val discrepancyAmount: Long = 0
)

/**
 * Source of data usage
 */
enum class DataSourceType {
    DEVICE,
    PROVIDER
}

/**
 * Severity levels for detected discrepancies
 */
enum class DiscrepancySeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * -------- Utility Functions --------
 */
fun Long.formatToMB(): Double = this / (1024.0 * 1024.0)

fun Long.formatToGB(): Double = this / (1024.0 * 1024.0 * 1024.0)

fun getDiscrepancySeverity(differencePercentage: Double): DiscrepancySeverityLevel =
    when {
        differencePercentage < 5.0 -> DiscrepancySeverityLevel.LOW
        differencePercentage < 15.0 -> DiscrepancySeverityLevel.MEDIUM
        differencePercentage < 30.0 -> DiscrepancySeverityLevel.HIGH
        else -> DiscrepancySeverityLevel.CRITICAL
    }

/**
 * Get current time in milliseconds - use this everywhere instead of Clock
 */
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()