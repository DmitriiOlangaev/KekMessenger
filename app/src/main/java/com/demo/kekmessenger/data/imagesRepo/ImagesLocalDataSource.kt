package com.demo.kekmessenger.data.imagesRepo

import android.graphics.drawable.Drawable

interface ImagesLocalDataSource : ImagesDataSource {

    suspend fun save(key: String, data: Drawable): Result<Unit>
    suspend fun clearCachedImages(): Result<Unit>

}
