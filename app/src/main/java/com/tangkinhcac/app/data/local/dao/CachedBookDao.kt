package com.tangkinhcac.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.tangkinhcac.app.data.local.entity.CachedBookEntity

@Dao
interface CachedBookDao {
    @Query("SELECT * FROM cached_books ORDER BY title COLLATE NOCASE")
    suspend fun getAll(): List<CachedBookEntity>

    @Upsert
    suspend fun upsertAll(books: List<CachedBookEntity>)

    @Query("DELETE FROM cached_books")
    suspend fun clear()
}
