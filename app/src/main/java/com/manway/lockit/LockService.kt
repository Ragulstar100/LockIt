package com.manway.lockit

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face6
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import com.compose.overlay.ComposeOverlayViewService
import com.manway.lockit.Database.Room.AppDatabase

import com.manway.lockit.Database.Room.PackageLockStatus
import com.manway.lockit.Database.Room.Settings
import com.manway.lockit.Database.Room.UserList
import com.manway.lockit.Database.Room.admin
import com.manway.lockit.Handler.toBitmap
import com.manway.lockit.services.bootService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class LockService() :ComposeOverlayViewService() {

    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
       bootService.packageName?.let {
           var unlock by remember {
               mutableStateOf(false)
           }

           //Animation
           val height by animateDpAsState(if(!unlock) 700.dp else 100.dp, tween(500,100, LinearEasing))

           val textOpacity by animateFloatAsState(if(!unlock) 1f else 0.01f, tween(500,100, LinearEasing))


           val userList= Room.databaseBuilder(this, AppDatabase::class.java, "database-name").build().userListDao()
           val packageLockList = Room.databaseBuilder(this, AppDatabase::class.java, "database-name").build().packageLockStatusDao()
           var settings = Room.databaseBuilder(this, AppDatabase::class.java, "database-name").build().settingsDao()
           var info by remember { mutableStateOf<PackageLockStatus?>(null) }
           var passwordInfo by remember { mutableStateOf<UserList?>(null) }
           var settingsInfo by remember { mutableStateOf<Settings?>(null) }
           var showSnackBar by remember { mutableStateOf(false) }
           var showUserIst by remember { mutableStateOf(false) }
           val scope= rememberCoroutineScope()
           scope.launch {
               passwordInfo = userList.getUserList("admin")
               info=packageLockList.getPackageLockStatus(it,passwordInfo!!.userName)
               settingsInfo = settings.getById(1)
           }
           Scaffold (Modifier.clip(MaterialTheme.shapes.medium).size(500.dp,height).background(Color.White).width(250.dp),
               topBar = {
                   Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                       passwordInfo?.let {
                           AssistChip(
                               {
                                   showUserIst=!showUserIst
                               },
                               { Text(it.userName) },
                               modifier = Modifier.padding(10.dp),
                               leadingIcon = {
                                   if (it.profilePitcture != null) Image(it.profilePitcture!!.toBitmap().asImageBitmap(), "") else Icon(Icons.Default.Face6, "", Modifier.size(30.dp))
                               },
                               shape = MaterialTheme.shapes.extraSmall
                           )
                       }
                       AnimatedVisibility(showUserIst) {
                           Column(Modifier.padding(10.dp).clip(MaterialTheme.shapes.small).widthIn(100.dp).heightIn(100.dp).background(Color.White)) {

                               userList.getAll().collectAsState(listOf<UserList>()).value.forEach {
                                   val userContainsPackage=packageLockList.getPackagesFromUser(it.userName).collectAsState(
                                       listOf()
                                   ).value.filter { it.packageName==bootService.packageName&&it.isLocked  }.isNotEmpty()

                                   if(userContainsPackage)    Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                           passwordInfo=it
                                           showUserIst=false
                                       }) {
                                           if(it.profilePitcture!=null) Image(it.profilePitcture!!.toBitmap().asImageBitmap(),"") else Icon(Icons.Default.Face6,"",Modifier.size(50.dp))
                                           Text(it.userName,Modifier.padding(15.dp), maxLines = 1)
                                       }
                               }
                           }
                       }

                   }
               },
               snackbarHost = {
            if(showSnackBar)   Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(MaterialTheme.shapes.small).background(
                Color.White).padding(20.dp)) {
                   Text("Your Password Wrong")
                   TextButton({
                       showSnackBar=false
                   }) {
                       Text("Ok", color = Color.Blue)
                   }

               }
           }) { pad->
               Column(Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                   Spacer(Modifier.height(60.dp))
                   val icon = packageManager.getApplicationIcon(it).toBitmap().asImageBitmap()
                   val name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(it, 0)).toString()

                   AnimatedMsg(it,this@LockService,!unlock, "Unlock"," mis offset"){
                       bootService.stopLockService(this@LockService)
                   }
                   Spacer(Modifier.height(20.dp))
                   Text(name, Modifier.fillMaxWidth(), color = Color.Unspecified.copy(textOpacity), textAlign = TextAlign.Center)
                 if(!unlock)  NumPad {password->
                        passwordInfo?.let {
                          if(it.password==password&&it.password!=null){
                              info = info?.copy(actionTime = Date().time)
                              unlock=true
                          }else if (password!=null){
                              showSnackBar=true
                          }
                        }

                   }
               }
           }
       }
    }

}

@Composable
fun AnimatedMsg(packageName:String, context: Context, start:Boolean, msg1:String, msg2: String, animationFinishListener:()->Unit){
    var msg by remember {
        mutableStateOf("")
    }
    val textScroll by animateDpAsState(if(msg.isEmpty()) 0.dp else -90.dp, tween(500,1000, LinearEasing)){
        animationFinishListener()
    }
    val widthIcon by animateFloatAsState(if(start) 0.01f else 0.9f, tween(500,100, LinearEasing) ){
        msg=msg1
    }
    val icon = context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()

    Row(Modifier.fillMaxWidth().height(75.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

        Image(icon, "Apk Icon", Modifier.padding(7.dp).clip(MaterialTheme.shapes.small).background(Color.DarkGray.copy(0.1f)).size(60.dp).padding(3.dp))
        Column(Modifier.fillMaxHeight()) {
            Text(msg,Modifier.fillMaxWidth(widthIcon).absoluteOffset(y=textScroll+10.dp), color = Color.Green, textAlign = TextAlign.Center)
            Text(msg2,Modifier.fillMaxWidth(widthIcon).absoluteOffset(y=100.dp+textScroll), color = Color.Green, textAlign = TextAlign.Center)
        }
    }
}