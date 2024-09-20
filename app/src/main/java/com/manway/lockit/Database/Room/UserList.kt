package com.manway.lockit.Database.Room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity
data class UserList(@PrimaryKey val userName:String, var password:Int?=null, var profilePitcture:ByteArray?=null,var face:ByteArray?=null)

@Dao
interface UserListDao{
    @Query("SELECT * FROM UserList ")
    fun getAll(): Flow<List<UserList>>

    @Query("SELECT * FROM UserList WHERE userName=:userName")
    suspend fun getUserList(userName: String):UserList

    @Query("SELECT userName FROM UserList")
    fun getUserNameAll(): Flow<List<String>>

    @Insert
    suspend fun upsert(UserList: UserList)

    @Update
    suspend fun update(UserList: UserList)

    @Query("SELECT face FROM UserList ")
    fun getFaceAll(): Flow<List<ByteArray?>>

    @Query("DELETE FROM UserList WHERE userName=:userName")
    suspend fun delete(userName: String)

}