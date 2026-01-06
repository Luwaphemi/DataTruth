package com.example.datatruth.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Represents data usage measured by the device
 */
@Serializable
data class DataUsageModel @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val timestamp: Instant,
    val mobileDataBytes: Long,
    val wifiDataBytes: Long,
    val totalBytes: Long = mobileDataBytes + wifiDataBytes,
    val source: DataSourceType = DataSourceType.DEVICE
)

/**
 * Represents data reported by the network provider
 */
@Serializable
data class ProviderReportModel(
    val id: String,
    val timestamp: Instant,
    val reportedDataBytes: Long,
    val remainingDataBytes: Long?,
    val dataLimitBytes: Long?,
    val providerName: String,
    val source: DataSourceType = DataSourceType.PROVIDER
)

/**
 * Represents a discrepancy between device measurement and provider report
 */
@Serializable
data class DiscrepancyModel(
    val id: String,
    val timestamp: Instant,
    val deviceMeasurement: Long,
    val providerReport: Long,
    val differenceBytes: Long,
    val differencePercentage: Double,
    val severity: DiscrepancySeverityLevel,
    val note: String? = null
)

/**
 * User's data plan settings
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
 * Summary statistics for display
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

enum class DataSourceType {
    DEVICE,
    PROVIDER
}

enum class DiscrepancySeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Helper functions
 */
fun Long.formatToMB(): Double = this / (1024.0 * 1024.0)
fun Long.formatToGB(): Double = this / (1024.0 * 1024.0 * 1024.0)

fun getDiscrepancySeverity(differencePercentage: Double): DiscrepancySeverityLevel {
    return when {
        differencePercentage < 5.0 -> DiscrepancySeverityLevel.LOW
        differencePercentage < 15.0 -> DiscrepancySeverityLevel.MEDIUM
        differencePercentage < 30.0 -> DiscrepancySeverityLevel.HIGH
        else -> DiscrepancySeverityLevel.CRITICAL
    }
}