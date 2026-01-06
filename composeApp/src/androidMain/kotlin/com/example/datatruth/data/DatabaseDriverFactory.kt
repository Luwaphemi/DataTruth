package com.example.datatruth.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.datatruth.db.DataTruthDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = DataTruthDatabase.Schema,
            context = context,
            name = "dataguard.db"
        )
    }
}