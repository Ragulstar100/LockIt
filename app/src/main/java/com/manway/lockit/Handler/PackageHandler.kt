package com.manway.lockit.Handler

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.widget.Toast
import java.util.Date

object PackageHandler {
    private val packageNames = ArrayList<String>()
    @Throws(IndexOutOfBoundsException::class)
    fun getCurrentApk(context: Context): String {
        val list = ArrayList<String>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - (20 * 60 * 1000)
        var result = ""
        val event = UsageEvents.Event()
        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                result = event.packageName
                list.add(result)
            }
        }
        if (!TextUtils.isEmpty(result)) {
            return list[list.size - 1]
        }
        list.add("")
        Toast.makeText(context, list[0], Toast.LENGTH_SHORT).show()
        return list[list.size - 1]
    }

    fun getPackageNames(context: Context): ArrayList<String> {
        val list = ArrayList<String>()
        val packageManager = context.packageManager

        // Get the list of installed packages
        val packages = packageManager.getInstalledPackages(0)

        // Iterate through the list of packages
        for (packageInfo in packages) {
            list.add(packageInfo.packageName)
        }

        return list
    }

    fun getInstalledPackageNames(context: Context): ArrayList<String> {
        val list = ArrayList<String>()
        val packageManager = context.packageManager

        // Get the list of installed packages
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        // Iterate through the list of packages
        for (packageInfo in packages) {
            list.add(packageInfo.packageName)
        }

        return list
    }

   public abstract  class PackageChangeListener(var context: Context) {


       abstract fun packageAction(date: Date, packageName: String?)

        public fun start() {

            val packageName = arrayOf("")
            val i: InfiniteTimer = object : InfiniteTimer() {
                override fun timerAction(currentTimeMills: Long) {
                    if (packageName[0] != getCurrentApk(context)) {
                        packageName[0] = getCurrentApk(context)
                        packageAction(Date(currentTimeMills), packageName[0])
                    }
                }
            }
            i.start()
        }
    }
}
