package com.example.text2ukotent

import android.content.Context
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
    NavHost(navController = navController, startDestination = "Giris") {
        composable("Giris") { Giris(navController) }
        composable("SohbetOdasi") { SohbetOdasi(navController, "") }
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

        ModernButon(onClick = { GirisButon(kAdi, navController) }, modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp), "Giriş")
    }
}

fun GirisButon(kAdi: String, navController: NavController) {
    val databaseReference = FirebaseDatabase.getInstance().getReference()

    if (kAdi.isNullOrEmpty()) {
        Toast.makeText(navController.context, "En az 3 karakterden oluşan bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
    } else {
        databaseReference.child("Packages").child("OnlineUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {
                    databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                    navController.navigate("SohbetOdasi/$kAdi")
                } else {
                    for (incelenenKullanici in snapshot.children) {
                        if (snapshot.child(kAdi).value?.equals(kAdi) == true) {
                            Toast.makeText(navController.context, "Kullanıcı online", Toast.LENGTH_SHORT).show()
                        } else {
                            databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                            navController.navigate("SohbetOdasi/$kAdi")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}


fun cikisYap(cikisDiyalog: Boolean) {
    TODO("Not yet implemented")
}

@Composable
fun SohbetOdasi(navController: NavController, kAdi: String) {
    var mesaj by remember { mutableStateOf("") }
    val context = LocalContext.current
    var cikisDiyalog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        cikisDiyalog = true
    }
    if (cikisDiyalog) {
        AlertDialog(
            onDismissRequest = { cikisDiyalog = false },
            title = { Text(text = "Çıkış Yap") },
            text = { Text(text = "Kullanıcıdan çıkış yapılacak") },
            confirmButton = {
                Button(onClick = {
                    cikisDiyalog = false
                    navController.popBackStack()
                }) { Text("Çıkış") }
            },
            dismissButton = {
                Button(onClick = {
                    cikisDiyalog = false
                }) { Text("İptal") }
            }
        )
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(35.dp, 0.dp, 35.dp, 0.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Çıkış",
                tint = Color.Magenta,
                modifier = Modifier.size(20.dp)
            )
        }
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

    DisposableEffect(Unit) {
        onDispose {
            KullaniciCikis(kAdi, context)
        }
    }
}

fun KullaniciCikis(kAdi: String, cont: Context) {
    val ref = FirebaseDatabase.getInstance().getReference().child("Packages").child("OnlineUsers").child(kAdi)
    ref.removeValue().addOnSuccessListener {
        Toast.makeText(cont, "Çıkış Yapıldı", Toast.LENGTH_SHORT).show()
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


