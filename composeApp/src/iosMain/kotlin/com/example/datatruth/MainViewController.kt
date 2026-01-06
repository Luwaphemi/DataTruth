package com.example.datatruth

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import com.example.datatruth.data.DataRepository
import com.example.datatruth.db.DataTruthDatabase
import com.example.datatruth.platform.IOSDataMonitor

fun MainViewController(): androidx.compose.ui.platform.UIViewController {
    return ComposeUIViewController {
        IosApp()
    }
}
