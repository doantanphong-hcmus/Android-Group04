package com.tangkinhcac.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Locally cached book metadata; Firestore remains the authoritative source. */
@Entity(tableName = "cached_books")
data class CachedBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
)
