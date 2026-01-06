package com.example.datatruth.platform

import com.example.datatruth.models.DataUsageModel
import kotlinx.datetime.Clock
import kotlin.random.Random

class IOSDataMonitor : DataMonitor {
    
    override suspend fun getCurrentUsage(): DataUsageModel {
        val mobileBytes = Random.nextLong(
            from = 300_000_000L,
            until = 1_500_000_000L
        )
        return DataUsageModel(
            id = "ios_usage_${Random.nextInt()}",
            timestamp = Clock.System.now(),
            mobileDataBytes = mobileBytes,
            wifiDataBytes = 0L,
            totalBytes = mobileBytes
        )
    }
    
    override suspend fun getUsageInRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): DataUsageModel? {
        return getCurrentUsage()
    }
    
    override suspend fun getCurrentMonthUsage(): DataUsageModel? {
        return getCurrentUsage()
    }
    
    override fun getOperatorName(): String {
        return "iOS Carrier"
    }
    
    override fun hasMobileDataCapability(): Boolean {
        return true
    }
}