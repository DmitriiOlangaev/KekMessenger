package com.demo.kekmessenger.data.imagesRepo

import android.graphics.drawable.Drawable
import com.demo.kekmessenger.data.RepoUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesRepository @Inject constructor(
    private val imagesLocalDataSource: ImagesLocalDataSource,
    private val imagesRemoteDataSource: ImagesRemoteDataSource,
    private val coroutineScope: CoroutineScope
) {

    suspend fun getImage(key: String): Result<Drawable> {
        val newKey = key.removePrefix("thumb/").removePrefix("img/")
        return RepoUtilities.repoGet(
            { imagesLocalDataSource.getImage(newKey) },
            { imagesRemoteDataSource.getImage(newKey) },
            { false },
            { coroutineScope.launch { imagesLocalDataSource.save(newKey, it.getOrThrow()) } }
        )
    }

    suspend fun clearCachedImages(): Result<Unit> =
        imagesLocalDataSource.clearCachedImages()
}