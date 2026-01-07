package com.example.datatruth.models

import com.example.datatruth.serialization.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents data usage measured directly from the device
 */
@Serializable
data class DataUsageModel(
    val id: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
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
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
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
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
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
 * -------- Utility Extensions --------
 */

/**
 * Convert bytes to megabytes
 */
fun Long.formatToMB(): Double =
    this / (1024.0 * 1024.0)

/**
 * Convert bytes to gigabytes
 */
fun Long.formatToGB(): Double =
    this / (1024.0 * 1024.0 * 1024.0)

/**
 * Determine discrepancy severity from percentage difference
 */
fun getDiscrepancySeverity(
    differencePercentage: Double
): DiscrepancySeverityLevel =
    when {
        differencePercentage < 5.0 -> DiscrepancySeverityLevel.LOW
        differencePercentage < 15.0 -> DiscrepancySeverityLevel.MEDIUM
        differencePercentage < 30.0 -> DiscrepancySeverityLevel.HIGH
        else -> DiscrepancySeverityLevel.CRITICAL
    }
