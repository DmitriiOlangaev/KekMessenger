package com.demo.kekmessenger.viewModels

import com.demo.kekmessenger.exceptions.ApplicationException
import kotlinx.coroutines.flow.Flow

interface Loader<Data> {
    class State<Data>(
        val data: Data,
        val isLoading: Boolean,
        val error: ApplicationException?
    )

    val state: Flow<State<Data>>


}