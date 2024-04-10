package com.demo.kekmessenger.data.imagesRepo

import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.ImageRequest
import com.demo.kekmessenger.exceptions.AccessStorageException
import com.demo.kekmessenger.exceptions.ClientInternetException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

class CoilImagesDataSource @Inject constructor(
    private val imageLoader: ImageLoader,
    private val imageRequestBuilder: ImageRequest.Builder,
    private val coroutineScope: CoroutineScope,
    private val busyFiles: ConcurrentMap<String, Mutex>,
) : ImagesRemoteDataSource, ImagesLocalDataSource {
    override suspend fun save(key: String, data: Drawable): Result<Unit> = Result.success(Unit)

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun clearCachedImages(): Result<Unit> =
        Result.runCatching {
            imageLoader.diskCache?.let { diskCache ->
                Files.newDirectoryStream(diskCache.directory.toNioPath())
                    .forEach {
                        coroutineScope.async(Dispatchers.IO) {
                            busyFiles.getOrPut(it.fileName.toString()) { Mutex() }.withLock {
                                Files.delete(it)
                            }
                        }.await()
                    }
            }
        }.fold(onSuccess = {
            Result.success(Unit)
        }, onFailure = {
            when (it) {
                is IOException -> Result.failure(AccessStorageException())
                else -> Result.failure(it)
            }
        }
        )

    override suspend fun getImage(key: String): Result<Drawable> =
        getImage(key, imageRequestBuilder.data(key).build())


    private suspend fun getImage(key: String, request: ImageRequest): Result<Drawable> =
        Result.runCatching {
            val result = withContext(Dispatchers.IO) {
                busyFiles.getOrPut(key) { Mutex() }
                    .withLock {
                        imageLoader.execute(request)
                    }
            }
            if (result.drawable != null) {
                return success(result.drawable!!)
            } else {
                return failure(ClientInternetException())
            }
        }
}