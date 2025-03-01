package com.example.text2ukotent

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.text2ukotent.ui.theme.Text2UKotEntTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text2UKotEntTheme {
                MainScreen()
            }

        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Giris") {
        composable("Giris") { Giris(navController) }
        composable("ChatRoom") { ChatRoom(navController) }

    }
}

@Composable
//@Preview
fun Giris(navController: NavController) {

    val font2 = FontFamily(Font(R.font.pixel))
    val font = FontFamily.Default
    var kAdi by remember {
        mutableStateOf("")
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp, 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "T2U",
            fontSize = 40.sp,
            fontFamily = font,
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Kullanıcı Adı",
                fontSize = 12.sp,
                fontFamily = font,
                modifier = Modifier.padding(10.dp, 0.dp)
            )
            OutlinedTextField(value = kAdi, onValueChange = { text ->
                kAdi = text
            })
        }
        Button(onClick = { navController.navigate("ChatRoom") }, modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp)) { Text(text = "Giriş") }
    }
}

@Composable
fun ChatRoom(navController: NavController) {
    var mesaj by remember { mutableStateOf("") }
    // val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Red)
        .padding(35.dp, 0.dp, 0.dp, 0.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .background(Color.Blue),
        ) {
        }
        Row (Modifier.background(Color.Gray)){
            OutlinedTextField(value = mesaj, onValueChange = { text ->
                mesaj = text
            }, )
            Button(onClick = { }) { Text(text = "Gönder") }
        }
    }

}

