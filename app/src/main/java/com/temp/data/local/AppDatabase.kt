package com.temp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.temp.data.local.dao.UserDao
import com.temp.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}