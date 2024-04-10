package com.demo.kekmessenger.data.preferencesRepo

import androidx.datastore.core.DataStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val userPreferencesStore: DataStore<UserPreferences>,
) {
    private val _error: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    val error: StateFlow<Throwable?> = _error
    val userPreferencesFlow: Flow<UserPreferences> = userPreferencesStore.data.catch {
        _error.value = it
        emit(userPreferencesStore.data.lastOrNull() ?: UserPreferences.getDefaultInstance())
    }

    suspend fun changeName(newName: String): Result<Unit> =
        update { userPreferences ->
            userPreferences.toBuilder().setName(newName).build()
        }

    suspend fun changeTheme(theme: UserPreferences.Theme): Result<Unit> =
        update { userPreferences ->
            userPreferences.toBuilder().setTheme(theme).build()
        }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun update(updateFun: (UserPreferences) -> (UserPreferences)): Result<Unit> =
        Result.runCatching {
            GlobalScope.async(Dispatchers.IO) {
                userPreferencesStore.updateData { userPreferences ->
                    updateFun(userPreferences)
                }
            }.await()
        }

}