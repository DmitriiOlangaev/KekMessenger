package com.demo.kekmessenger.data.imagesRepo

import android.graphics.drawable.Drawable

interface ImagesDataSource {
    suspend fun getImage(key: String): Result<Drawable>
}