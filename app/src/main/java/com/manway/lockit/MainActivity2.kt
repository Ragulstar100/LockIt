package com.manway.lockit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face6
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.room.Room
import com.manway.lockit.Database.Room.AppDatabase

import com.manway.lockit.Database.Room.PackageLockStatus

import com.manway.lockit.Database.Room.Settings
import com.manway.lockit.Database.Room.UserList
import com.manway.lockit.Database.Room.admin

import com.manway.lockit.Handler.PackageHandler
import com.manway.lockit.Handler.toBitmap
import com.manway.lockit.ui.theme.LockItTheme
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity2 : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockItTheme {
                val t =rememberModalBottomSheetState()
                val sheetState= rememberBottomSheetScaffoldState(t)
                val scope= rememberCoroutineScope()

                scope.launch {
                    t.expand()
                }

                BottomSheetScaffold({
                    Settings(this@MainActivity2)
                }, topBar ={
                    IconButton({
                        scope.launch {
                            t.expand()
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Settings")
                    }
                }, sheetSwipeEnabled = true, scaffoldState = sheetState, modifier = Modifier.fillMaxSize()) {
                    val packageStatus= Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build().packageLockStatusDao()
                        Spacer(Modifier.height(100.dp))
                        Column(Modifier.verticalScroll(rememberScrollState())) {


                    PackageHandler.getPackageNames(applicationContext).forEach {
                        var info by remember {
                            mutableStateOf<PackageLockStatus?>(null)
                        }
                            scope.launch {
                                info = packageStatus.getPackageLockStatus(it, admin)

                                if (info.toString() == "null") {
                                    packageStatus.insert(PackageLockStatus(it, admin,false,1, Date().time,-1));
                                    info = packageStatus.getPackageLockStatus(it, admin)
                                }
                            }


                        val icon = packageManager.getApplicationIcon(it).toBitmap().asImageBitmap()
                        val name= packageManager.getApplicationLabel(packageManager.getApplicationInfo(it, 0)).toString()


                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.width(20.dp))
                                Image(icon, "Apk Icons", Modifier.clip(CircleShape).padding(7.dp).clip(MaterialTheme.shapes.small).background(Color.DarkGray.copy(0.1f)).size(60.dp).padding(3.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(name, Modifier.fillMaxWidth(0.70f))
                                info?.let {
                                    Switch(it.isLocked, { bol ->
                                        scope.launch {
                                            info = it.copy(isLocked = bol)
                                            packageStatus.update(info!!)
                                        }
                                    })
                                }

                            }
                    }}
                }
                }

            }
        }
    }


@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun Settings(context: Context,userName:String="admin",packageName:String="all"){

    //Data From DataBase
    val roomDatabase= Room.databaseBuilder(context, AppDatabase::class.java, "database-name").build()
    val scope= rememberCoroutineScope()
    var setInfo by remember { mutableStateOf<Settings?>(null) }
    var userInfo by remember { mutableStateOf<UserList?>(null) }
    var selectUserInfo by remember { mutableStateOf<UserList?>(null) }

    scope.launch {
        setInfo=roomDatabase.settingsDao().getById(1)
        userInfo=roomDatabase.userListDao().getUserList(userName)
    }

    var retypepassword by remember { mutableStateOf<Int?>(null) }
    var tabScreen by remember { mutableStateOf(1) }

    //Dialog Open
    var dlg_open_addUser by remember { mutableStateOf(false) }
    var dlg_open_packageLimit by remember { mutableStateOf(false) }

    var tabScreen1:@Composable (Settings)->Unit={
        Row(Modifier.height(50.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            var i by remember { mutableStateOf((it.cooldownTime / 600000).toInt()-1) }
            var cooldownType by remember { mutableStateOf(it.individualCoolDown) }
            AssistChip({
                val k = (600000..3600000 step 600000).toList()

                scope.launch {
                    i++
                    i=i % k.size
                    roomDatabase.settingsDao()
                        .update(setInfo!!.copy(cooldownTime = k[i].toLong()))
                    setInfo = setInfo!!.copy(cooldownTime = k[i].toLong())
                }
            },
                {
                    Text("Cool Down")
                },
                modifier = Modifier.fillMaxWidth(0.4f), trailingIcon = { Text("${it.cooldownTime / (60 * 1000)} min") }, shape = MaterialTheme.shapes.extraSmall)

            Row(Modifier.clip(MaterialTheme.shapes.extraSmall).border(1.dp, Color.LightGray).fillMaxWidth(0.8f), verticalAlignment = Alignment.CenterVertically) {
                Text("Individual", Modifier.fillMaxWidth(0.5f).background(if (cooldownType) Color.LightGray else Color.Unspecified).padding(5.dp).clickable { cooldownType = true;scope.launch { roomDatabase.settingsDao().update(setInfo!!.copy(individualCoolDown = cooldownType)) } })
                Text("Common", Modifier.fillMaxWidth().background(if (!cooldownType) Color.LightGray else Color.Unspecified).padding(5.dp).clickable { cooldownType = false;scope.launch { roomDatabase.settingsDao().update(setInfo!!.copy(individualCoolDown = cooldownType)) } })

            }

        }
    }

    var tabScreen2:@Composable ()->Unit={
        val scrollState by animateDpAsState(if(retypepassword==null) 0.dp else -400.dp, tween(500, easing = LinearEasing))
        Box (Modifier.width(800.dp)) {
            NumPad({ Text("Password") },Modifier.absoluteOffset(x=scrollState+30.dp).width(400.dp)) {
                if (it.toString() != "null") { retypepassword = it } else { retypepassword = null }
            }
            NumPad({
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("RetypePassword")
                    IconButton({ retypepassword = null }) { Icon( Icons.Default.RestartAlt,"Reset" , tint = Color.LightGray, modifier = Modifier.scale(1.25f)) }
                }
            },Modifier.absoluteOffset(x=scrollState+430.dp).width(400.dp)) { pas->
                if(pas!=null){ if (retypepassword==pas) { scope.launch { userInfo?.let { roomDatabase.userListDao().update(it.copy(password = pas )) } }
                    Toast.makeText(context,"Password Reseted",Toast.LENGTH_SHORT).show() } else { Toast.makeText(context,"Enter Password Correctly",Toast.LENGTH_SHORT).show() }}

            }

        }
    }



    //new
        setInfo?.let {
            Scaffold(Modifier.fillMaxWidth().height(if(tabScreen==1) 200.dp else 800.dp), topBar = {}, floatingActionButton = { if(tabScreen==3)   Icon(Icons.Default.Add,"",Modifier.clip(RoundedCornerShape(10)).background(Color.White).scale(1.5f).padding(10.dp).clickable { dlg_open_addUser=true }) }){pad->
                Column {
                    //Tab Title
                    Row(Modifier.padding(6.dp).clip(MaterialTheme.shapes.extraSmall).border(1.dp, Color.LightGray).fillMaxWidth()) {
                        Text("Cool Down Settings", Modifier.fillMaxWidth(0.33f).background(if (tabScreen==1) Color.LightGray else Color.Unspecified).padding(5.dp).clickable { tabScreen=1 });Text("Change Password\n", Modifier.fillMaxWidth(0.5f).background(if (tabScreen==2) Color.LightGray else Color.Unspecified).padding(5.dp).clickable { tabScreen=2 })
                      if(userName== admin)  Text("Add Users\n", Modifier.fillMaxWidth().background(if (tabScreen==3) Color.LightGray else Color.Unspecified).padding(5.dp).clickable { tabScreen=3 })
                    }

             when(tabScreen){
                 1->{ tabScreen1(it) }
                 2->{ tabScreen2() }
                 3->{
                 if(userName== admin)    FlowRow (Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { roomDatabase.userListDao().getAll().collectAsState(listOf<UserList>()).value.forEach { if(it.userName!= admin)      AssistChip({ selectUserInfo=it;tabScreen=0 },{ Text(it.userName) }, modifier = Modifier.padding(10.dp).fillMaxWidth(0.5f), leadingIcon = { if(it.profilePitcture!=null) Image(it.profilePitcture!!.toBitmap().asImageBitmap(),"") else Icon(Icons.Default.Face6,"",Modifier.size(100.dp)) }, trailingIcon = { IconButton({ scope.launch { roomDatabase.userListDao().delete(it.userName) } }) { Icon(Icons.Default.Delete, "") } }, shape = MaterialTheme.shapes.extraSmall) } } }
                 else->{
                    Dialog({ }) {
                    if(userName== admin&&selectUserInfo==null)    Column(Modifier.width(450.dp).clip(MaterialTheme.shapes.extraSmall).background(Color.White)) {

                            var password by remember { mutableStateOf<Int?>(null) }

                            var userName by remember { mutableStateOf("") }

                            OutlinedTextField(userName, { userName = it }, modifier = Modifier.padding(10.dp).fillMaxWidth(), label = { Text("UserName") })
                            val scrollState by animateDpAsState(if (password == null) 0.dp else -400.dp, tween(500, easing = LinearEasing))

                            Box(Modifier.width(450.dp)) {
                                NumPad({ Text("Password") }, Modifier.absoluteOffset(x = scrollState ).scale(0.9f)) {
                                    if (it.toString() != "null") { password = it } else { password = null }
                                }
                                NumPad({
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("RetypePassword")
                                        IconButton({ password = null }) {
                                            Icon(Icons.Default.RestartAlt, "Reset", tint = Color.LightGray, modifier = Modifier.scale(1.25f))
                                        }
                                    }
                                }, Modifier.absoluteOffset(x = scrollState + 400.dp).scale(0.9f)) { pas ->
                                    if (pas != null) {
                                        if (password == pas) {
                                            Toast.makeText(context, "Press Ok Button Save Progress", Toast.LENGTH_SHORT).show()
                                        } else {
                                            password=null
                                            Toast.makeText(context, "Enter Password Correctly", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                }

                            }

                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(15.dp)) {
                                val users=roomDatabase.userListDao().getUserNameAll().collectAsState(listOf()).value
                                TextButton({
                                    if(password!=null)   if(!users.contains(userName)) {
                                        scope.launch {
                                            if (password != 0) roomDatabase.userListDao().upsert(UserList(userName, password))
                                            selectUserInfo=roomDatabase.userListDao().getUserList(userName)
                                        }
                                        dlg_open_addUser = false
                                        dlg_open_packageLimit=true
                                    } else {
                                        Toast.makeText(context, "UserName Already Exists", Toast.LENGTH_LONG).show()
                                    }
                                    else Toast.makeText(context,"Type Your Password",Toast.LENGTH_LONG).show()
                                }) {
                                    Text("Ok")
                                }
                                TextButton({ dlg_open_addUser = false }) { Text("Cancel")
                                }
                            }
                        }
                        selectUserInfo?.let {
                            val settingsId=1
                            var userCheckList=roomDatabase.packageLockStatusDao().getPackagesFromUser(it.userName).collectAsState(listOf()).value

                            //Add Package if not exists from into username from admin Name
                            roomDatabase.packageLockStatusDao().getPackageNamesNotInUser(it.userName).collectAsState(listOf()).value.forEach {pkg->
                                scope.launch {
                                    roomDatabase.packageLockStatusDao().insert(PackageLockStatus(pkg,it.userName,false,settingsId,Date().time,-1))
                                }
                            }


                            Column(Modifier.clip(MaterialTheme.shapes.extraSmall).width(450.dp).background(Color.White).verticalScroll(rememberScrollState(), reverseScrolling = true)) {
                                userCheckList.forEachIndexed { i,pkg->
                                    val icon = context.packageManager.getApplicationIcon(pkg.packageName).toBitmap().asImageBitmap()
                                    val name= context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg.packageName, 0)).toString()
                                    var check by remember { mutableStateOf(pkg.isLocked) }

                                    Row(Modifier.padding(5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Image(icon,"",Modifier.padding(5.dp).size(35.dp))
                                        Text(name,Modifier.padding(5.dp).fillMaxWidth(0.85f))
                                        Checkbox(check,{ scope.launch { check=it;roomDatabase.packageLockStatusDao().update(pkg.copy(isLocked =check)) } },Modifier.clip(CircleShape))
                                    }

                                }

                            }
                        }

                    }

                 }
             }

                }
            }
        }






}

@SuppressLint("SimpleDateFormat")
@Composable
private fun ViewStatus(context: Context,packageName: String){
    val icon = context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
    val name = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString()
    Row(Modifier.horizontalScroll(rememberScrollState(), reverseScrolling = true)) {
        (1..25).forEach{
          if(it%2==0)  Box(Modifier.width(80.dp).height(200.dp).padding(10.dp)) {
                Icon(Icons.Default.Image, "Test", Modifier.size(45.dp).absoluteOffset(x = 8.dp))
                Text(SimpleDateFormat("dd/MM/yy\nhh:mm").format(Date()), modifier = Modifier.absoluteOffset(y = 80.dp), textAlign = TextAlign.Center, lineHeight =20.sp)
                VerticalDivider(
                    Modifier.absoluteOffset(x = 28.dp, y = 45.dp).height(30.dp),
                    1.5.dp,
                    Color.Black
                )
            }
        else    Box(Modifier.width(80.dp).height(200.dp).padding(10.dp)) {
                Icon(
                    Icons.Default.Image,
                    "Test",
                    Modifier.size(45.dp).absoluteOffset(x = 8.dp, y = 80.dp)
                )
                Text(
                    SimpleDateFormat("dd/MM/yy\nhh:mm").format(Date()),
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    lineHeight =20.sp
                )
                VerticalDivider(
                    Modifier.absoluteOffset(x = 28.dp, y = 45.dp).height(30.dp),
                    1.5.dp,
                    Color.Black
                )
            }
        }
    }
}