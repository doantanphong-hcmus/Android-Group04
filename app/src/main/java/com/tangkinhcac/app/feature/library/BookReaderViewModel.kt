package com.tangkinhcac.app.feature.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tangkinhcac.app.data.repository.BookReaderRepository
import com.tangkinhcac.app.data.repository.ReaderChapter
import com.tangkinhcac.app.data.repository.ReaderFailure
import com.tangkinhcac.app.data.repository.ReaderLoadException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReaderState {
    data object Loading : ReaderState

    data class Ready(
        val chapters: List<ReaderChapter>,
    ) : ReaderState

    data class Error(
        val reason: ReaderFailure,
    ) : ReaderState
}

class BookReaderViewModel(
    private val repository: BookReaderRepository,
    private val bookId: String,
) : ViewModel() {
    private val _state = MutableStateFlow<ReaderState>(ReaderState.Error(ReaderFailure.SERVER))
    val state = _state.asStateFlow()

    init {
        retry()
    }

    fun retry() {
        if (_state.value == ReaderState.Loading) return
        _state.value = ReaderState.Loading
        viewModelScope.launch {
            _state.value =
                try {
                    ReaderState.Ready(repository.load(bookId))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: ReaderLoadException) {
                    if (error.reason == ReaderFailure.INVALID_FILE) {
                        Log.e("BookReader", "Cannot read EPUB for book=$bookId", error)
                    }
                    ReaderState.Error(error.reason)
                } catch (error: java.io.IOException) {
                    ReaderState.Error(ReaderFailure.NETWORK)
                } catch (error: Exception) {
                    ReaderState.Error(ReaderFailure.SERVER)
                }
        }
    }

    companion object {
        fun factory(
            repository: BookReaderRepository,
            bookId: String,
        ): ViewModelProvider.Factory = viewModelFactory { initializer { BookReaderViewModel(repository, bookId) } }
    }
}
