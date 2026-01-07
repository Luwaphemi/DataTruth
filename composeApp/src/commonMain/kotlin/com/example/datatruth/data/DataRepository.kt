package com.example.datatruth.data

import com.example.datatruth.db.DataTruthDatabase
import com.example.datatruth.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DataRepository(private val database: DataTruthDatabase) {

    private val queries = database.dataTruthDatabaseQueries

    // Insert data usage record
    suspend fun insertDataUsage(usage: DataUsageModel) = withContext(Dispatchers.Default) {
        queries.insertDataUsage(
            id = usage.id,
            timestamp = usage.timestamp,
            mobileDataBytes = usage.mobileDataBytes,
            wifiDataBytes = usage.wifiDataBytes,
            totalBytes = usage.totalBytes
        )
    }

    // Insert provider report
    suspend fun insertProviderReport(report: ProviderReportModel) = withContext(Dispatchers.Default) {
        queries.insertProviderReport(
            id = report.id,
            timestamp = report.timestamp,
            reportedDataBytes = report.reportedDataBytes,
            remainingDataBytes = report.remainingDataBytes,
            dataLimitBytes = report.dataLimitBytes,
            providerName = report.providerName
        )
    }

    // Insert discrepancy
    suspend fun insertDiscrepancy(discrepancy: DiscrepancyModel) = withContext(Dispatchers.Default) {
        queries.insertDiscrepancy(
            id = discrepancy.id,
            timestamp = discrepancy.timestamp,
            deviceMeasurement = discrepancy.deviceMeasurement,
            providerReport = discrepancy.providerReport,
            differenceBytes = discrepancy.differenceBytes,
            differencePercentage = discrepancy.differencePercentage,
            severity = discrepancy.severity.name,
            note = discrepancy.note
        )
    }

    // Get all data usage records
    suspend fun getAllDataUsage(): List<DataUsageModel> = withContext(Dispatchers.Default) {
        queries.getAllDataUsage().executeAsList().map {
            DataUsageModel(
                id = it.id,
                timestamp = it.timestamp,
                mobileDataBytes = it.mobileDataBytes,
                wifiDataBytes = it.wifiDataBytes,
                totalBytes = it.totalBytes
            )
        }
    }

    // Get latest provider report
    suspend fun getLatestProviderReport(): ProviderReportModel? = withContext(Dispatchers.Default) {
        queries.getLatestProviderReport().executeAsOneOrNull()?.let {
            ProviderReportModel(
                id = it.id,
                timestamp = it.timestamp,
                reportedDataBytes = it.reportedDataBytes,
                remainingDataBytes = it.remainingDataBytes,
                dataLimitBytes = it.dataLimitBytes,
                providerName = it.providerName
            )
        }
    }

    // Get all discrepancies
    suspend fun getAllDiscrepancies(): List<DiscrepancyModel> = withContext(Dispatchers.Default) {
        queries.getAllDiscrepancies().executeAsList().map {
            DiscrepancyModel(
                id = it.id,
                timestamp = it.timestamp,
                deviceMeasurement = it.deviceMeasurement,
                providerReport = it.providerReport,
                differenceBytes = it.differenceBytes,
                differencePercentage = it.differencePercentage,
                severity = DiscrepancySeverityLevel.valueOf(it.severity),
                note = it.note
            )
        }
    }

    // Save or update data plan
    suspend fun saveDataPlan(plan: DataPlanModel) = withContext(Dispatchers.Default) {
        queries.insertOrUpdateDataPlan(
            id = plan.id,
            providerName = plan.providerName,
            dataLimitBytes = plan.dataLimitBytes,
            billingCycleStartDay = plan.billingCycleStartDay.toLong(),
            alertThresholdPercentage = plan.alertThresholdPercentage,
            discrepancyThresholdPercentage = plan.discrepancyThresholdPercentage
        )
    }

    // Get data plan
    suspend fun getDataPlan(): DataPlanModel? = withContext(Dispatchers.Default) {
        queries.getDataPlan().executeAsOneOrNull()?.let {
            DataPlanModel(
                id = it.id,
                providerName = it.providerName,
                dataLimitBytes = it.dataLimitBytes,
                billingCycleStartDay = it.billingCycleStartDay.toInt(),
                alertThresholdPercentage = it.alertThresholdPercentage,
                discrepancyThresholdPercentage = it.discrepancyThresholdPercentage
            )
        }
    }

    // Get total usage in date range
    suspend fun getTotalUsageInRange(startTime: Long, endTime: Long): Long =
        withContext(Dispatchers.Default) {
            queries.getTotalUsageInRange(startTime, endTime).executeAsOneOrNull()?.totalUsage ?: 0L
        }

    // Calculate and detect discrepancies
    suspend fun detectDiscrepancies(): DiscrepancyModel? = withContext(Dispatchers.Default) {
        val deviceUsage = queries.getLatestDataUsage().executeAsOneOrNull()
        val providerReport = queries.getLatestProviderReport().executeAsOneOrNull()

        if (deviceUsage != null && providerReport != null) {
            val difference = deviceUsage.totalBytes - providerReport.reportedDataBytes
            val percentageDiff = (difference.toDouble() / providerReport.reportedDataBytes.toDouble()) * 100.0

            if (kotlin.math.abs(percentageDiff) > 5.0) {
                val now = currentTimeMillis()
                val discrepancy = DiscrepancyModel(
                    id = "disc_$now",
                    timestamp = now,
                    deviceMeasurement = deviceUsage.totalBytes,
                    providerReport = providerReport.reportedDataBytes,
                    differenceBytes = difference,
                    differencePercentage = percentageDiff,
                    severity = when {
                        kotlin.math.abs(percentageDiff) < 5.0 -> DiscrepancySeverityLevel.LOW
                        kotlin.math.abs(percentageDiff) < 15.0 -> DiscrepancySeverityLevel.MEDIUM
                        kotlin.math.abs(percentageDiff) < 30.0 -> DiscrepancySeverityLevel.HIGH
                        else -> DiscrepancySeverityLevel.CRITICAL
                    },
                    note = if (difference > 0) "Device measured more data" else "Provider reported more data"
                )
                insertDiscrepancy(discrepancy)
                return@withContext discrepancy
            }
        }
        null
    }

    // Generate mock provider data (for testing)
    suspend fun generateMockProviderReport(actualDeviceUsage: Long): ProviderReportModel = withContext(Dispatchers.Default) {
        val discrepancyFactor = Random.nextDouble(0.9, 1.1)
        val providerReported = (actualDeviceUsage * discrepancyFactor).toLong()

        val now = currentTimeMillis()
        val report = ProviderReportModel(
            id = "provider_$now",
            timestamp = now,
            reportedDataBytes = providerReported,
            remainingDataBytes = 5_000_000_000L - providerReported,
            dataLimitBytes = 5_000_000_000L,
            providerName = "Mock Provider"
        )
        insertProviderReport(report)
        report
    }
}