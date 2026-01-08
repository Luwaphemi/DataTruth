@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.datatruth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datatruth.data.DataRepository
import com.example.datatruth.models.*
import com.example.datatruth.platform.DataMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class EnhancedUiState(
    val isLoading: Boolean = false,
    val currentDeviceUsage: DataUsageModel? = null,
    val providerReport: ProviderReportModel? = null,
    val recentDiscrepancies: List<DiscrepancyModel> = emptyList(),
    val dataPlan: DataPlanModel? = null,
    val usageSummary: UsageSummaryModel? = null,
    val allUsageHistory: List<DataUsageModel> = emptyList(),
    val operatorName: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class EnhancedViewModel(
    private val repository: DataRepository,
    private val dataMonitor: DataMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedUiState())
    val uiState: StateFlow<EnhancedUiState> = _uiState.asStateFlow()

    init {
        loadOperator()
        loadAllData()
        captureLiveUsage()
    }

    fun captureLiveUsage() {
        viewModelScope.launch {
            try {
                val usage = dataMonitor.getCurrentUsage()
                repository.insertDataUsage(usage)

                _uiState.value = _uiState.value.copy(
                    currentDeviceUsage = usage,
                    successMessage = "Live usage updated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to capture live usage"
                )
            }
        }
    }

    fun loadMonthlyUsage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val usage = dataMonitor.getCurrentMonthUsage()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentDeviceUsage = usage,
                error = if (usage == null)
                    "Usage permission required"
                else null
            )
        }
    }

    fun loadUsageForDateRange(
        startMillis: Long,
        endMillis: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val usage = dataMonitor.getUsageInRange(startMillis, endMillis)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentDeviceUsage = usage,
                error = if (usage == null)
                    "Usage permission required"
                else null
            )
        }
    }

    private fun loadOperator() {
        _uiState.value = _uiState.value.copy(
            operatorName = dataMonitor.getOperatorName()
        )
    }

    fun fetchProviderData(mockUsage: Long? = null) {
        viewModelScope.launch {
            try {
                val deviceUsage = mockUsage
                    ?: _uiState.value.currentDeviceUsage?.totalBytes
                    ?: 2_000_000_000L

                val report = repository.generateMockProviderReport(deviceUsage)

                _uiState.value = _uiState.value.copy(
                    providerReport = report,
                    successMessage = "Provider data fetched"
                )

                checkDiscrepancies()
                loadAllData()

            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch provider data"
                )
            }
        }
    }

    private fun checkDiscrepancies() {
        viewModelScope.launch {
            try {
                repository.detectDiscrepancies()
            } catch (_: Exception) { }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            try {
                val dataPlan = repository.getDataPlan()
                val providerReport = repository.getLatestProviderReport()
                val history = repository.getAllDataUsage().take(10)
                val discrepancies = repository.getAllDiscrepancies().take(5)

                val summary =
                    if (dataPlan != null && providerReport != null)
                        calculateUsageSummary(dataPlan, providerReport)
                    else null

                _uiState.value = _uiState.value.copy(
                    dataPlan = dataPlan,
                    providerReport = providerReport,
                    usageSummary = summary,
                    allUsageHistory = history,
                    recentDiscrepancies = discrepancies
                )

            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load data"
                )
            }
        }
    }

    private suspend fun calculateUsageSummary(
        plan: DataPlanModel,
        report: ProviderReportModel
    ): UsageSummaryModel {

        val usage = report.reportedDataBytes
        val percentage = (usage.toDouble() / plan.dataLimitBytes) * 100

        val deviceUsage = repository.getAllDataUsage().firstOrNull()
        val discrepancy = deviceUsage?.let {
            abs(it.totalBytes - usage)
        } ?: 0L

        return UsageSummaryModel(
            currentCycleUsage = usage,
            providerReportedUsage = usage,
            dataLimit = plan.dataLimitBytes,
            percentageUsed = percentage,
            daysRemainingInCycle = 30,
            averageDailyUsage = usage / 30,
            projectedEndOfCycleUsage = usage,
            hasDiscrepancy =
                discrepancy > (plan.dataLimitBytes * plan.discrepancyThresholdPercentage / 100),
            discrepancyAmount = discrepancy
        )
    }

    fun captureCurrentUsage() {
        captureLiveUsage()
    }

    fun setupDataPlan(plan: DataPlanModel) {
        viewModelScope.launch {
            repository.saveDataPlan(plan)
            _uiState.value = _uiState.value.copy(
                dataPlan = plan,
                successMessage = "Data plan configured"
            )
            loadAllData()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}