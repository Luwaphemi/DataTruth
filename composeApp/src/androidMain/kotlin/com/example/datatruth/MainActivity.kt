package com.example.datatruth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.datatruth.data.DatabaseDriverFactory
import com.example.datatruth.db.DataTruthDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val database = DataTruthDatabase(driverFactory.createDriver())

        setContent {
            AndroidApp(
                context = this,
                database = database
            )
        }

    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Preview placeholder
}