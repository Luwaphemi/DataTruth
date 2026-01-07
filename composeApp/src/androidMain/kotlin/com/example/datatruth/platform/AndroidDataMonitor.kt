package com.example.datatruth.platform

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import com.example.datatruth.models.DataUsageModel
import java.util.Calendar
import java.util.UUID

class AndroidDataMonitor(
    private val context: Context
) : DataMonitor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    /**
     * Total usage since device boot (TrafficStats)
     */
    override suspend fun getCurrentUsage(): DataUsageModel {
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()
        val totalRx = TrafficStats.getTotalRxBytes()
        val totalTx = TrafficStats.getTotalTxBytes()

        val mobileDataBytes =
            if (mobileRx != TrafficStats.UNSUPPORTED.toLong()
                && mobileTx != TrafficStats.UNSUPPORTED.toLong()
            ) {
                mobileRx + mobileTx
            } else 0L

        val totalDataBytes = totalRx + totalTx
        val wifiDataBytes = totalDataBytes - mobileDataBytes

        return DataUsageModel(
            id = "usage_${UUID.randomUUID()}",
            timestamp = System.currentTimeMillis(),
            mobileDataBytes = mobileDataBytes,
            wifiDataBytes = wifiDataBytes,
            totalBytes = totalDataBytes
        )
    }

    /**
     * Usage from start of current month
     */
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override suspend fun getCurrentMonthUsage(): DataUsageModel? {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        return getUsageInRange(
            startTimeMillis = cal.timeInMillis,
            endTimeMillis = System.currentTimeMillis()
        )
    }

    /**
     * Accurate usage for a date range (NetworkStatsManager)
     */
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override suspend fun getUsageInRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): DataUsageModel? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null

        return try {
            val manager =
                context.getSystemService(Context.NETWORK_STATS_SERVICE)
                        as NetworkStatsManager

            val subscriberId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) null
                else @Suppress("DEPRECATION") telephonyManager.subscriberId

            var mobileRx = 0L
            var mobileTx = 0L

            subscriberId?.let {
                val mobileStats = manager.querySummary(
                    ConnectivityManager.TYPE_MOBILE,
                    it,
                    startTimeMillis,
                    endTimeMillis
                )

                val bucket = NetworkStats.Bucket()
                while (mobileStats.hasNextBucket()) {
                    mobileStats.getNextBucket(bucket)
                    mobileRx += bucket.rxBytes
                    mobileTx += bucket.txBytes
                }
                mobileStats.close()
            }

            val wifiStats = manager.querySummary(
                ConnectivityManager.TYPE_WIFI,
                "",
                startTimeMillis,
                endTimeMillis
            )

            var wifiRx = 0L
            var wifiTx = 0L
            val wifiBucket = NetworkStats.Bucket()

            while (wifiStats.hasNextBucket()) {
                wifiStats.getNextBucket(wifiBucket)
                wifiRx += wifiBucket.rxBytes
                wifiTx += wifiBucket.txBytes
            }
            wifiStats.close()

            val mobile = mobileRx + mobileTx
            val wifi = wifiRx + wifiTx

            DataUsageModel(
                id = "usage_${UUID.randomUUID()}",
                timestamp = System.currentTimeMillis(),
                mobileDataBytes = mobile,
                wifiDataBytes = wifi,
                totalBytes = mobile + wifi
            )

        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    override fun getOperatorName(): String =
        telephonyManager.networkOperatorName ?: "Unknown"

    override fun hasMobileDataCapability(): Boolean =
        telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_NONE
}