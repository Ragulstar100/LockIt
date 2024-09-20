package com.manway.lockit.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.room.Room
import com.manway.lockit.Database.Room.AppDatabase
import com.manway.lockit.Database.Room.PackageLockStatus
import com.manway.lockit.Database.Room.admin
import com.manway.lockit.Handler.PackageHandler
import com.manway.lockit.LockService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Date


class bootService  constructor() : AccessibilityService() {

    companion object{
        var packageName:String? = null
        fun startLockService(context: Context) {
            try {
                context.startService(Intent(context, LockService::class.java))
                Log.e("BootService", "Enter")
            } catch (e: Exception) {
                Log.e("BootService", "Error starting LockService: ",e)
            }
        }

       fun stopLockService(context: Context) {
            try {
                context.stopService(Intent(context, LockService::class.java))
            } catch (e: Exception) {
                Log.e("BootService", "Error stopping LockService: ", e)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }

    override fun onInterrupt() {
    }

    public  override fun onServiceConnected() {
        val packageList= Room.databaseBuilder(this, AppDatabase::class.java, "database-name").build().packageLockStatusDao()
        var packageStatus:PackageLockStatus?=null

         val k=  object :PackageHandler.PackageChangeListener(this){
            override fun packageAction(date: Date, packageName: String?) {


               bootService.packageName=packageName
                bootService.packageName?.let {
                    runBlocking {
                        packageStatus=packageList.getPackageLockStatus(it,admin)
                        packageStatus?.let {

                       if (it.isLocked) {

                           if ((date.time - it.actionTime) <= (20 * 1000)) {
                               stopLockService(this@bootService)
                           } else {
                               startLockService(this@bootService)
                           }
                       } else {
                           stopLockService(this@bootService)
                       }

                       }
                        if(packageStatus==null) stopLockService(this@bootService)
                }

                }

                }
        }

        k.start()





//        object : InfiniteTimer() {
//            override fun timerAction(currentTimeMills: Long) {
//                try { bootService.packageName = PackageHandler.getCurrentApk(this@bootService) } catch (e: Exception) { }
//
//             bootService.packageName?.let {
//                    runBlocking {
//                        packageStatus=packageList.getLockPackageStatus(it)
//                   packageStatus?.let {
//                       if (it.isLocked) {
//
//                           if ((currentTimeMills - it.actionTime) <= (20 * 1000)) {
//                               stopLockService()
//                           } else {
//                               startLockService()
//                           }
//                       } else {
//                           stopLockService()
//                       }
//
//                       }
//                        if(packageStatus==null) stopLockService()
//                }
//
//                }
//
//                }
//        }.start()

    }

    private suspend fun packageName(){

            var packageName: String? = null
            try {
                packageName = PackageHandler.getCurrentApk(this@bootService)
            } catch (e: Exception) {
            }
            Log.e("BootService", "Package Name: $packageName")
            delay(1000)
            packageName()
    }


}

