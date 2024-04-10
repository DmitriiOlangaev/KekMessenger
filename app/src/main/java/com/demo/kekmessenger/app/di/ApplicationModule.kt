package com.demo.kekmessenger.app.di

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.room.Room
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.demo.kekmessenger.BuildConfig
import com.demo.kekmessenger.data.imagesRepo.CoilImagesDataSource
import com.demo.kekmessenger.data.imagesRepo.ImagesLocalDataSource
import com.demo.kekmessenger.data.imagesRepo.ImagesRemoteDataSource
import com.demo.kekmessenger.data.messagesRepo.DataBase
import com.demo.kekmessenger.data.messagesRepo.MessageDao
import com.demo.kekmessenger.data.messagesRepo.MessagesLocalDataSource
import com.demo.kekmessenger.data.messagesRepo.MessagesRemoteDataSource
import com.demo.kekmessenger.data.messagesRepo.RetrofitMessagesRemoteDataSource
import com.demo.kekmessenger.data.messagesRepo.RoomMessagesLocalDataSource
import com.demo.kekmessenger.data.preferencesRepo.UserPreferences
import com.demo.kekmessenger.data.preferencesRepo.UserPreferencesSerializer
import com.demo.kekmessenger.exceptions.ClientInternetException
import com.demo.kekmessenger.network.MockServer
import com.demo.kekmessenger.network.MoshiParser
import com.demo.kekmessenger.network.Parser
import com.demo.kekmessenger.network.ServerDao
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.sync.Mutex
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Module
interface ApplicationModule {
    companion object {
        private const val BASE_URL = "http://127.0.0.1:8008"


        @ApplicationScope
        @Provides
        @Host(HostMode.Prod)
        fun retrofitServerDao(): ServerDao =
            Retrofit.Builder().baseUrl(BASE_URL).build().create(ServerDao::class.java)

        @ApplicationScope
        @Provides
        fun serverDao(
            @Host(HostMode.Debug) debugServerDao: ServerDao,
            @Host(HostMode.Prod) prodServerDao: ServerDao
        ): ServerDao = if (BuildConfig.DEBUG) debugServerDao else prodServerDao

        @ApplicationScope
        @Provides
        fun moshi(): Moshi = Moshi.Builder().build()

        @Provides
        fun imageLoader(context: Context): ImageLoader =
            ImageLoader.Builder(context.applicationContext).diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.DISABLED).networkCachePolicy(CachePolicy.ENABLED)
                .build()

        @Provides
        fun imageRequestBuilder(context: Context): ImageRequest.Builder =
            ImageRequest.Builder(context)

        @Provides
        fun concurrentStringToMutexMap(): ConcurrentMap<String, Mutex> = ConcurrentHashMap()

        @Provides
        fun imagesRemoteDataSource(): ImagesRemoteDataSource = object : ImagesRemoteDataSource {
            override suspend fun getImage(key: String): Result<Drawable> =
                Result.failure(ClientInternetException())
        }


        @ApplicationScope
        @Provides
        fun messagesDatabase(context: Context): MessageDao =
            Room.databaseBuilder(context.applicationContext, DataBase::class.java, "Messages.db")
                .build().messageDao()


        @ApplicationScope
        @OptIn(DelicateCoroutinesApi::class)
        @Provides
        fun dataStore(context: Context): DataStore<UserPreferences> =
            DataStoreFactory.create(UserPreferencesSerializer, null, emptyList(), GlobalScope) {
                File(context.applicationContext.dataDir, "UserPreferences")
            }

        @ApplicationScope
        @OptIn(DelicateCoroutinesApi::class)
        @Provides
        fun coroutineScope(): CoroutineScope = GlobalScope

    }

    @ApplicationScope
    @Binds
    fun coilImagesDataSource(coilImagesDataSource: CoilImagesDataSource): ImagesLocalDataSource

    @ApplicationScope
    @Binds
    fun messagesLocalDataSource(roomMessagesLocalDataSource: RoomMessagesLocalDataSource): MessagesLocalDataSource

    @ApplicationScope
    @Binds
    fun messagesRemoteDataSource(retrofitMessagesRemoteDataSource: RetrofitMessagesRemoteDataSource): MessagesRemoteDataSource

    @Binds
    @Host(HostMode.Debug)
    fun mockServer(mockServer: MockServer): ServerDao

    @ApplicationScope
    @Binds
    fun parser(moshiParser: MoshiParser): Parser
}