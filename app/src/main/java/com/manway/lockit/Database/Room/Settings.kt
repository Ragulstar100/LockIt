package com.manway.lockit.Database.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity
data class Settings(var cooldownTime:Long, var individualCoolDown:Boolean=false,var refersh:Boolean=false, var showNotification:Boolean=true, @PrimaryKey(autoGenerate =true ) val i:Int=0 ){

}

@Dao
interface SettingsDao{
    @Query("SELECT * FROM Settings ")
    fun getAll(): Flow<List<Settings>>

    @Query("SELECT * FROM Settings WHERE i=:id")
    suspend fun getById(id:Int):Settings

//    @Query("SELECT * FROM Settings WHERE packageName=:packageName")
//    fun getSettingsByPackageName(packageName: String): Flow<List<Settings>>
//
//    @Query("SELECT * FROM Settings WHERE userName=:userName")
//    fun getSettingsByUserName(userName: String): Flow<List<Settings>>
//
//    @Query("SELECT * FROM Settings WHERE userName=:userName AND packageName=:packageName")
//    fun getSettingsByUserNameAndPackageName(userName: String,packageName: String): Flow<List<Settings>>
//
//    @Query("SELECT * FROM Settings WHERE userName=:userName AND packageName=:packageName")
//    suspend fun getOneSettingsByUserNameAndPackageName(userName: String,packageName: String):Settings



    @Insert
    suspend fun insert(settings: Settings)

    @Update
    suspend fun update(settings: Settings)

    @Delete
    suspend fun delete(settings: Settings)


}