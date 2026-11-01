package com.tangkinhcac.app.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.tangkinhcac.app.data.local.dao.CachedBookDao
import com.tangkinhcac.app.data.local.entity.CachedBookEntity

/**
 * Local cache database. Feature-specific entities and DAOs are added only with a concrete offline requirement.
 */
@Database(
    entities = [CachedBookEntity::class],
    version = 1,
)
abstract class TangKinhCacDatabase : RoomDatabase() {
    abstract fun cachedBookDao(): CachedBookDao

    companion object {
        private const val DATABASE_NAME = "tang_kinh_cac.db"

        fun create(context: Context): TangKinhCacDatabase =
            Room
                .databaseBuilder<TangKinhCacDatabase>(
                    context = context.applicationContext,
                    name = DATABASE_NAME,
                ).setDriver(AndroidSQLiteDriver())
                .build()
    }
}
