package com.example.datatruth

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.datatruth.data.DataRepository
import com.example.datatruth.db.DataTruthDatabase
import com.example.datatruth.platform.AndroidDataMonitor

@Composable
fun AndroidApp(
    context: Context,
    database: DataTruthDatabase
) {
    val repository = DataRepository(database)
    val dataMonitor = AndroidDataMonitor(context)

    AppNav(
        repository = repository,
        dataMonitor = dataMonitor   // PASS THE OBJECT
    )
}
