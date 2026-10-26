package com.tangkinhcac.app.feature.library

import com.tangkinhcac.app.data.model.Book

data class BookListUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
