package com.example.text2ukotent

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.text2ukotent.ui.theme.Text2UKotEntTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Text2UKotEntTheme {
                MainScreen()
            }

        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "ChatRoom") {
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

        ModernButon(onClick = { girisButon(kAdi, navController) }, modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp), "Giriş")
    }
}

fun girisButon(uName: String, navController: NavController) {
    val databaseReference = FirebaseDatabase.getInstance().getReference()

    if (uName.isNullOrEmpty()){
        Toast.makeText(navController.context, "En az 3 karakterden oluşan bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
    }
    else{
        databaseReference.child("Packages").child("OnlineUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()){
                    databaseReference.child("Packages").child("OnlineUsers").child(uName).setValue(uName)
                    navController.navigate("ChatRoom")
                }
                else{
                    for (incelenenKullanici in snapshot.children){
                        if (snapshot.child(uName).value?.equals(uName) == true){
                            Toast.makeText(navController.context, "Kullanıcı online", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            databaseReference.child("Packages").child("OnlineUsers").child(uName).setValue(uName)
                            navController.navigate("ChatRoom")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}


@Composable
fun ChatRoom(navController: NavController) {
    var mesaj by remember { mutableStateOf("") }
    // val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Red)
        .padding(35.dp, 0.dp, 35.dp, 0.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(0.9f)
                .padding(0.dp, 15.dp, 0.dp, 0.dp)
                .background(Color.Blue),
        ) {
        }
        Row(Modifier
            .background(Color.Gray)
            .weight(0.1f)
        ) {
            OutlinedTextField(
                value = mesaj,
                onValueChange = { text ->
                    mesaj = text
                }, modifier = Modifier
                    .weight(0.80f)
            )
            ModernButon(onClick = {}, modifier = Modifier.weight(0.20f), "Gönder")
        }
    }

}

@Composable
fun ModernButon(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))
                )
            ),
    ) { Text(text = text) }
}

