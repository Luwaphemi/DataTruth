package com.example.datatruth

import androidx.compose.runtime.Composable
import com.example.datatruth.data.DataRepository
import com.example.datatruth.db.DataTruthDatabase
import com.example.datatruth.platform.IOSDataMonitor

@Composable
fun IosApp() {
    // Create database (in-memory or file-based depending on your setup)
    val database = DataTruthDatabase()

    val repository = DataRepository(database)
    val dataMonitor = IOSDataMonitor()

    AppNav(
        repository = repository,
        dataMonitor = dataMonitor
    )
}
