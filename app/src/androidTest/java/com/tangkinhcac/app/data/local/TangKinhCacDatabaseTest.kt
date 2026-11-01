package com.tangkinhcac.app.data.local

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tangkinhcac.app.data.local.entity.CachedBookEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TangKinhCacDatabaseTest {
    private lateinit var database: TangKinhCacDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room
                .inMemoryDatabaseBuilder<TangKinhCacDatabase>(context.applicationContext)
                .setDriver(AndroidSQLiteDriver())
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun cachedBooksCanBeReplacedAndCleared() =
        runBlocking {
            val dao = database.cachedBookDao()
            dao.upsertAll(
                listOf(
                    CachedBookEntity(id = "2", title = "Truyen Kieu", author = "Nguyen Du"),
                    CachedBookEntity(id = "1", title = "Dai Viet Su Ky", author = "Le Van Huu"),
                ),
            )

            assertEquals(listOf("Dai Viet Su Ky", "Truyen Kieu"), dao.getAll().map { it.title })

            dao.clear()
            assertEquals(emptyList<CachedBookEntity>(), dao.getAll())
        }
}
