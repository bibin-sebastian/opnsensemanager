package com.bibin.opnsense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bibin.opnsense.data.local.entity.DeviceAliasEntity

@Database(
    entities = [DeviceAliasEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceAliasDao(): DeviceAliasDao

    companion object {
        // Drops the schedule table that existed in v1/v2
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS schedule")
            }
        }
        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS schedule")
            }
        }
    }
}
