package com.manway.lockit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NumPad(title:String="",modifier: Modifier=Modifier,numpadAction:(Int?)->Unit){
    var showPassword by remember { mutableStateOf("") }
    var hidePassword by remember { mutableStateOf("_ _ _ _") }
    val numClick:(Int)->Unit ={n->
        if(showPassword.length<4) { showPassword=StringBuffer(showPassword).append(n).toString()
            hidePassword= StringBuffer(hidePassword).append(" *").removeRange(0..1).toString()
        }
    }


//    val nextPage={
//        if(showPassword.length==4){
//            PasswordPrimaryFragment.password=showPassword;
//            OneTimeRunActivity.fragmentActivity?.let {
//                OneTimeRunActivity.adapter = OneTimeRunActivity.OneTimeRunAdapter(it, PasswordConformFragment(contex))
//                OneTimeRunActivity.pager?.adapter = OneTimeRunActivity.adapter
//                if(sqlHelper.getDataString(SqlHelper.UserList,"user","admin","password")[0].equals("null")||sqlHelper.getDataString(SqlHelper.UserList,"user","admin","password")[0].equals("")){
//                    startActivity(Intent(requireContext(),OneTimeRunActivity::class.java))
//                }else{
//                    startActivity(Intent(requireContext(),PasswordChangeActivity::class.java))
//                }
//
//            }
//
//        }
//    }



    @Composable
    fun numButton(n:Int){
        val shape: Shape = RoundedCornerShape(10.dp)
        Button(onClick = { numClick(n) }, modifier = Modifier.width(75.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray, shape), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) {
            Text(text ="$n")
        }

    }

    fun statusColor(): Color {
//        if(retypePasswordEnable){
//            if(retypePassword.toString().length<4||!retypePassword.toString().equals(showPassword.toString())){
//                return Color.Red
//            }else{
//                return Color(0xFF1DE9B6)
//            }
//        }
//
//        else
        return Color(0xFF1DE9B6)

    }



    Spacer(modifier = Modifier.height(25.dp))
    var off=50

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontWeight = FontWeight.W400,textAlign = TextAlign.Center, fontSize = 15.sp)
        Text(text =StringBuffer(hidePassword).reverse().toString(), fontSize = 40.sp, color = if(isSystemInDarkTheme()) Color.DarkGray else Color.LightGray)
        Spacer(modifier = Modifier.fillMaxWidth().height(25.dp))
        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {

            numButton(n = 1)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 2)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 3)

        }
        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {
            numButton(n = 4)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 5)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 6)

        }

        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {
            numButton(n = 7)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 8)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 9)

        }

        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row (modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)){

            val shape: Shape = RoundedCornerShape(10.dp)
            Button(onClick = {showPassword="";hidePassword="_ _ _ _";numpadAction(null)}, modifier = Modifier.width(110.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, Color.LightGray, shape), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF), contentColor =if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) { Text(text ="Backspace", fontSize =11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.W400) }

            Spacer(modifier = Modifier.fillMaxHeight().width(15.dp))
            numButton(n = 0)
            Spacer(modifier = Modifier.fillMaxHeight().width(20.dp))

            Button(onClick = {
             if(showPassword.length==4) numpadAction(showPassword.toInt())
            }, modifier = Modifier.width(110.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, Color.LightGray, MaterialTheme.shapes.extraSmall), colors = ButtonDefaults.buttonColors(containerColor =statusColor(), contentColor =if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) { Text(text =if(showPassword.length!=4) "Enter the Password" else "Next", color = Color(0xFF2979FF), fontSize =11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.W400)
            }

        }

    }
    Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
}

@Composable
fun NumPad(titleContent:@Composable ()->Unit,modifier: Modifier=Modifier,numpadAction:(Int?)->Unit){
    var showPassword by remember { mutableStateOf("") }
    var hidePassword by remember { mutableStateOf("_ _ _ _") }
    val numClick:(Int)->Unit ={n->
        if(showPassword.length<4) { showPassword=StringBuffer(showPassword).append(n).toString()
            hidePassword= StringBuffer(hidePassword).append(" *").removeRange(0..1).toString()
        }
    }

//    val nextPage={
//        if(showPassword.length==4){
//            PasswordPrimaryFragment.password=showPassword;
//            OneTimeRunActivity.fragmentActivity?.let {
//                OneTimeRunActivity.adapter = OneTimeRunActivity.OneTimeRunAdapter(it, PasswordConformFragment(contex))
//                OneTimeRunActivity.pager?.adapter = OneTimeRunActivity.adapter
//                if(sqlHelper.getDataString(SqlHelper.UserList,"user","admin","password")[0].equals("null")||sqlHelper.getDataString(SqlHelper.UserList,"user","admin","password")[0].equals("")){
//                    startActivity(Intent(requireContext(),OneTimeRunActivity::class.java))
//                }else{
//                    startActivity(Intent(requireContext(),PasswordChangeActivity::class.java))
//                }
//
//            }
//
//        }
//    }



    @Composable
    fun numButton(n:Int){
        val shape: Shape = RoundedCornerShape(10.dp)
        Button(onClick = { numClick(n) }, modifier = Modifier.width(75.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray, shape), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) {
            Text(text ="$n")
        }

    }

    fun statusColor(): Color {
//        if(retypePasswordEnable){
//            if(retypePassword.toString().length<4||!retypePassword.toString().equals(showPassword.toString())){
//                return Color.Red
//            }else{
//                return Color(0xFF1DE9B6)
//            }
//        }
//
//        else
        return Color(0xFF1DE9B6)

    }



    Spacer(modifier = Modifier.height(25.dp))
    var off=50

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        titleContent()
        Text(text =StringBuffer(hidePassword).reverse().toString(), fontSize = 40.sp, color = if(isSystemInDarkTheme()) Color.DarkGray else Color.LightGray)
        Spacer(modifier = Modifier.fillMaxWidth().height(25.dp))
        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {

            numButton(n = 1)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 2)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 3)

        }
        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {
            numButton(n = 4)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 5)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 6)

        }

        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row(modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)) {
            numButton(n = 7)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 8)
            Spacer(modifier = Modifier.fillMaxHeight().width(off.dp))
            numButton(n = 9)

        }

        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Row (modifier = Modifier.width(350.dp).height(100.dp).background(color = Color.Transparent)){

            val shape: Shape = RoundedCornerShape(10.dp)
            Button(onClick = {showPassword="";hidePassword="_ _ _ _";numpadAction(null)}, modifier = Modifier.width(110.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, Color.LightGray, shape), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF), contentColor =if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) { Text(text ="Backspace", fontSize =11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.W400) }

            Spacer(modifier = Modifier.fillMaxHeight().width(15.dp))
            numButton(n = 0)
            Spacer(modifier = Modifier.fillMaxHeight().width(20.dp))

            Button(onClick = {
                if(showPassword.length==4) numpadAction(showPassword.toInt())
            }, modifier = Modifier.width(110.dp).fillMaxHeight().absoluteOffset(x = 10.dp).border(1.dp, Color.LightGray, MaterialTheme.shapes.extraSmall), colors = ButtonDefaults.buttonColors(containerColor =statusColor(), contentColor =if(isSystemInDarkTheme()) darkColorScheme().primary else lightColorScheme().primary), shape = shape) { Text(text =if(showPassword.length!=4) "Enter the Password" else "Next", color = Color(0xFF2979FF), fontSize =11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.W400)
            }

        }

    }
    Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
}

