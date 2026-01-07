package com.example.datatruth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datatruth.data.DataRepository
import com.example.datatruth.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentUsage: DataUsageModel? = null,
    val providerReport: ProviderReportModel? = null,
    val recentDiscrepancies: List<DiscrepancyModel> = emptyList(),
    val dataPlan: DataPlanModel? = null,
    val usageSummary: UsageSummaryModel? = null,
    val error: String? = null
)

class MainViewModel(private val repository: DataRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val dataPlan = repository.getDataPlan()
                val providerReport = repository.getLatestProviderReport()
                val discrepancies = repository.getAllDiscrepancies().take(5)

                // Calculate usage summary if we have data plan
                val summary = if (dataPlan != null) {
                    calculateUsageSummary(dataPlan, providerReport)
                } else null

                _uiState.value = HomeUiState(
                    isLoading = false,
                    providerReport = providerReport,
                    recentDiscrepancies = discrepancies,
                    dataPlan = dataPlan,
                    usageSummary = summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun recordDataUsage(usage: DataUsageModel) {
        viewModelScope.launch {
            try {
                repository.insertDataUsage(usage)
                _uiState.value = _uiState.value.copy(currentUsage = usage)

                // Check for discrepancies
                checkForDiscrepancies()
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to record usage: ${e.message}")
            }
        }
    }

    fun fetchMockProviderData(deviceUsage: Long) {
        viewModelScope.launch {
            try {
                val report = repository.generateMockProviderReport(deviceUsage)
                _uiState.value = _uiState.value.copy(providerReport = report)

                // Check for discrepancies after getting provider data
                checkForDiscrepancies()
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch provider data: ${e.message}")
            }
        }
    }

    fun saveDataPlan(plan: DataPlanModel) {
        viewModelScope.launch {
            try {
                repository.saveDataPlan(plan)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save data plan: ${e.message}")
            }
        }
    }

    private fun checkForDiscrepancies() {
        viewModelScope.launch {
            try {
                repository.detectDiscrepancies()
            } catch (e: Exception) {
                // Silent fail for discrepancy check
            }
        }
    }

    private suspend fun calculateUsageSummary(
        dataPlan: DataPlanModel,
        providerReport: ProviderReportModel?
    ): UsageSummaryModel {
        // Calculate billing cycle dates using current time in millis
        val now = currentTimeMillis()
        // Extract day from timestamp (simplified calculation)
        val currentDay = ((now / (24 * 60 * 60 * 1000)) % 30).toInt() + 1

        // Simple projection calculation
        val daysInCycle = 30
        val daysElapsed = if (currentDay >= dataPlan.billingCycleStartDay) {
            currentDay - dataPlan.billingCycleStartDay
        } else {
            (30 - dataPlan.billingCycleStartDay) + currentDay
        }
        val daysRemaining = daysInCycle - daysElapsed

        // Get device measured usage (mock for now)
        val deviceUsage = providerReport?.reportedDataBytes ?: 0L
        val percentageUsed = (deviceUsage.toDouble() / dataPlan.dataLimitBytes.toDouble()) * 100.0

        val averageDailyUsage = if (daysElapsed > 0) deviceUsage / daysElapsed else 0L
        val projectedUsage = averageDailyUsage * daysInCycle

        val hasDiscrepancy = providerReport != null &&
                kotlin.math.abs(deviceUsage - providerReport.reportedDataBytes) >
                (dataPlan.dataLimitBytes * dataPlan.discrepancyThresholdPercentage / 100)

        return UsageSummaryModel(
            currentCycleUsage = deviceUsage,
            providerReportedUsage = providerReport?.reportedDataBytes,
            dataLimit = dataPlan.dataLimitBytes,
            percentageUsed = percentageUsed,
            daysRemainingInCycle = daysRemaining,
            averageDailyUsage = averageDailyUsage,
            projectedEndOfCycleUsage = projectedUsage,
            hasDiscrepancy = hasDiscrepancy,
            discrepancyAmount = if (providerReport != null) deviceUsage - providerReport.reportedDataBytes else 0L
        )
    }
}