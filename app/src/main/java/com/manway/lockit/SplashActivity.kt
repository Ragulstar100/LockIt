package com.manway.lockit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.manway.lockit.Database.Room.AppDatabase
import com.manway.lockit.ui.theme.LockItTheme

class SplashActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val roomDatabase= Room.databaseBuilder(this, AppDatabase::class.java, "database-name").build()
            val lockList = roomDatabase.userListDao().getAll().collectAsState(listOf()).value

            var active = remember { mutableStateOf(arrayListOf(checkAccessibilityServicePermission(),checkUsageAccessPermission(),checkOverlayPermission())) }

            LockItTheme {

                var splashScreenLogo by remember {
                    mutableStateOf(true)
                }
                val countDownTimer=object :CountDownTimer(10000,2000){
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                       splashScreenLogo=false
                    }

                }
                countDownTimer.start()

                if(splashScreenLogo) Box(Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {   Text("Welcome")}

               else  if (lockList.isEmpty()||active.value.contains(false)) startActivity(Intent(this,MainActivity::class.java))

               else    Scaffold(modifier = Modifier.fillMaxSize()) {
                        Column {
                            Spacer(Modifier.height(100.dp))
                        NumPad("Enter Your Password") {
                          if( lockList.find { it.userName=="admin" }?.password== it) startActivity(Intent(this@SplashActivity,MainActivity2::class.java)) else Toast.makeText(this@SplashActivity,"Password Incorrect",Toast.LENGTH_LONG).show()
                        }
                            }
                }
            }
        }
    }
}

