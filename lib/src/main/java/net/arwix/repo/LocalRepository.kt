package net.arwix.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocalRepository<R : Request, D> {
    val state: StateFlow<LoadState<R>>
    fun requestLoad(request: R): Flow<LoadState<out R>>
    suspend fun saveData(request: R, data: D): Boolean
}