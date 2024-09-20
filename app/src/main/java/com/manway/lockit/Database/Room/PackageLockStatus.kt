package com.manway.lockit.Database.Room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

@Entity
data class PackageLockStatus(val packageName: String,var userName:String,var isLocked:Boolean,var settingsId: Int, var actionTime:Long ,var endTime:Long, @PrimaryKey(autoGenerate =true) val i:Int=0 ){

}

@Dao
interface PackageLockStatusDao{
    @Query("SELECT * FROM PackageLockStatus ")
    fun getAll(): Flow<List<PackageLockStatus>>

    @Query("SELECT packageName FROM PackageLockStatus")
    fun getPackageNameAll(): Flow<List<String>>

    @Query("SELECT * FROM PackageLockStatus WHERE packageName=:packageName AND userName=:userName ")
    suspend fun getPackageLockStatus(packageName: String,userName: String):PackageLockStatus

    @Query("SELECT * FROM PackageLockStatus WHERE userName=:userName And isLocked=1")
    fun getLockedPackagesFromUser(userName: String):Flow<List<PackageLockStatus>>

    @Query("SELECT * FROM PackageLockStatus WHERE userName=:userName")
    fun getPackagesFromUser(userName: String):Flow<List<PackageLockStatus>>



    @Query("SELECT packageName FROM PackageLockStatus WHERE userName=\"admin\" And isLocked=1 except SELECT packageName FROM PackageLockStatus WHERE userName=:userName")
    fun getPackageNamesNotInUser(userName: String):Flow<List<String>>


    @Insert
    suspend fun insert(lockList:PackageLockStatus)

    @Update
    suspend fun update(lockList: PackageLockStatus)

    @Delete
    suspend fun delete(lockList: PackageLockStatus)



}

