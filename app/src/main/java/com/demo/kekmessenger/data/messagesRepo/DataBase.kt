package com.demo.kekmessenger.data.messagesRepo

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MessageTable::class], version = 1)
@TypeConverters(MessageTypeConverter::class)
abstract class DataBase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

