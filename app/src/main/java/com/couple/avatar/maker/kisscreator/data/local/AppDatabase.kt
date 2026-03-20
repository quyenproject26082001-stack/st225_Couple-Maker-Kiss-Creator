package com.couple.avatar.maker.kisscreator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.couple.avatar.maker.kisscreator.data.local.dao.UserDao
import com.couple.avatar.maker.kisscreator.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}