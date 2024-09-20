package com.manway.lockit.Database.Room

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [UserList::class,Settings::class,PackageLockStatus::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userListDao():UserListDao
    abstract fun packageLockStatusDao():PackageLockStatusDao
    abstract fun settingsDao():SettingsDao
}

const val  admin="admin"

enum class EntryState{
    In,Out,None
}

enum class TempLockAction{
    TempLocked,TempUnlocked,None
}








