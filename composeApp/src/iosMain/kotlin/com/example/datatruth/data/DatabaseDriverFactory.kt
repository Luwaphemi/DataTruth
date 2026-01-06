package com.example.datatruth.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.datatruth.db.DataTruthDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = DataTruthDatabase.Schema,
            name = "datatruth.db"
        )
    }
}