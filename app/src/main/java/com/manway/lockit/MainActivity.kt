package com.manway.lockit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.room.Room
import com.manway.lockit.Database.Room.AppDatabase
import com.manway.lockit.Database.Room.UserList
import com.manway.lockit.Handler.CameraPreview
import com.manway.lockit.Handler.FaceListener
import com.manway.lockit.Handler.FaceListenerScope
import com.manway.lockit.Handler.RVideoCapture
import com.manway.lockit.Handler.takePhoto
import com.manway.lockit.ui.theme.LockItTheme
import kotlinx.coroutines.launch


class MainActivity() : ComponentActivity(),FaceListener {



    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState",
        "CoroutineCreationDuringComposition"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockItTheme {
                val roomDatabase= Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build()

                var dlgOpen by remember {
                    mutableStateOf(true)
                }

                var lockList by remember {
                    mutableStateOf<UserList?>(null)
                }

                val scope= rememberCoroutineScope()

                var active = remember { mutableStateOf(arrayListOf(checkAccessibilityServicePermission(),checkUsageAccessPermission(),checkOverlayPermission())) }

                Scaffold(modifier = Modifier.fillMaxSize()) {

                        var faceList by remember { mutableStateOf(ArrayList<Bitmap>()) }
                        val videoRecord= RVideoCapture(this)


                       if(!dlgOpen) Column{

//                            FaceResignationColumn(this@MainActivity,Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
//
//                            }

                       }


                    if(dlgOpen) Dialog(onDismissRequest = { dlgOpen = false }) {

                        val size by animateSizeAsState(targetValue = if (active.value.contains(false)) Size(450.dp.value, 350.dp.value) else if(!dlgOpen) Size(0.dp.value, 0.dp.value) else Size(450.dp.value, 600.dp.value), animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

                        Column (Modifier.size(size.width.dp, size.height.dp).background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium), horizontalAlignment = Alignment.CenterHorizontally) {
                            var password by remember{
                                mutableStateOf<Int?>(null)
                            }

                            if(active.value.contains(false)){
                                PermissionColumn(this@MainActivity,active)
                            }

                            else if(roomDatabase.userListDao().getAll().collectAsState(listOf()).value.isEmpty()){
                                if(password==null){
                                    NumPad("Enter The Password",modifier = Modifier.fillMaxWidth().scale(0.9f)){ password=it }
                                }
                               else{

                                    var isPasswordMatch by remember {
                                        mutableStateOf(false)
                                    }
                                    NumPad("Password Confirmation\n",modifier = Modifier.fillMaxWidth().scale(0.9f)){

                                        isPasswordMatch=(it==password&&password!=null)
                                        if(isPasswordMatch) scope.launch {
                                            roomDatabase.userListDao().upsert(UserList("admin", it))
                                            roomDatabase.settingsDao().insert(
                                                com.manway.lockit.Database.Room.Settings(
                                                    20 * 60 * 1000L,
                                                    false,
                                                    true,
                                                    true,
                                                )
                                            )

                                        }
                                        if(!isPasswordMatch) Toast.makeText(applicationContext,"Password Not Match",Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }
                            else{
                                dlgOpen=false
                                scope.launch {
                                    lockList = roomDatabase.userListDao().getUserList("admin")
                                }
                                startActivity(Intent(this@MainActivity,SplashActivity::class.java))

                            }


                        }
                    }
                }
            }


        }
    }

}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun PermissionColumn(activity:Activity,active:MutableState<ArrayList<Boolean>>) {

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        active.value= arrayListOf(activity.checkAccessibilityServicePermission(),activity.checkUsageAccessPermission(),activity.checkOverlayPermission())
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

    Text("Give Some Permission",Modifier.padding(15.dp))
                Row(
                    Modifier
                        .fillMaxWidth(0.90f)
                        .clickable {
                            activity.requestAccessibilityServicePermission()
                        }, verticalAlignment = Alignment.CenterVertically) {
                    Text("AccessibilityService",Modifier.padding(15.dp))
                    Spacer(Modifier.fillMaxWidth(0.6f))
                  if(active.value[0])
                   Icon(Icons.Default.Check,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFF00E676)) })
                   else Icon(Icons.Default.Clear,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFFEF5350)) })
                }
                Row(
                    Modifier
                        .fillMaxWidth(0.90f)
                        .clickable { activity.requestUsageAccessPermission() }, verticalAlignment = Alignment.CenterVertically) {
                    Text("UsageAccessService",Modifier.padding(15.dp))
                    Spacer(Modifier.fillMaxWidth(0.6f))
                    if(active.value[1])  Icon(Icons.Default.Check,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFF00E676)) })
                    else Icon(Icons.Default.Clear,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFFEF5350)) })
                }
                Row(
                    Modifier
                        .fillMaxWidth(0.90f)
                        .clickable { activity.requestOverlayPermission() }, verticalAlignment = Alignment.CenterVertically) {
                    Text("OverLayService",Modifier.padding(15.dp))
                    Spacer(Modifier.fillMaxWidth(0.72f))
                    if(active.value[2])  Icon(Icons.Default.Check,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFF00E676)) })
                    else Icon(Icons.Default.Clear,"Wrong", tint = Color.White, modifier = Modifier.drawBehind { drawCircle(Color(0xFFEF5350)) })
                }

            }

@Composable
public fun FaceResignationColumn(context: Context,modifier: Modifier,faceListener: (ArrayList<Bitmap>)->Unit){
    val controller= remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
        }
    }
    val faceList=ArrayList<Bitmap>()

   // controller.cameraSelector=CameraSelector.DEFAULT_FRONT_CAMERA

    CameraPreview(controller,modifier)
    Button({
        takePhoto(context,controller){
            val countDownTimer= object : CountDownTimer(5000,1000){
                override fun onTick(millisUntilFinished: Long) {
                    FaceListenerScope {

                        it.getOneFaceData { r ->
                                faceList.add(it.cropBitmap(r))
                                faceListener(faceList)
                        }
                    }
                }
                override fun onFinish() {

                }
            }
            countDownTimer.start()
        }
    }){ Text("Start") }
}







