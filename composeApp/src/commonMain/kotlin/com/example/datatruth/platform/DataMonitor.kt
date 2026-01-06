package com.example.datatruth.platform

import com.example.datatruth.models.DataUsageModel

interface DataMonitor {

    suspend fun getCurrentUsage(): DataUsageModel

    suspend fun getUsageInRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): DataUsageModel?

    suspend fun getCurrentMonthUsage(): DataUsageModel?

    fun getOperatorName(): String

    fun hasMobileDataCapability(): Boolean
}
