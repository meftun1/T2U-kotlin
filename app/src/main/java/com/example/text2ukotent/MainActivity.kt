package com.example.text2ukotent

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.tween
import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation.toRoute
import com.example.text2ukotent.ui.theme.Text2UKotEntTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


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

@Serializable
object Giris

@Serializable
data class SohbetOdasi(val kAdi: String?)

@Serializable
data class MesajPaketi(val kadi: String = "", val mesaj: String = "", val zamanDamgasi: String? = "")

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Giris) {
        composable<Giris> {
            val font2 = FontFamily(Font(R.font.pixel))
            val font = FontFamily.Default
            var kAdi by remember {
                mutableStateOf("")
            }
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(32.dp, 24.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "T2U", fontSize = 40.sp, fontFamily = font, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Kullanıcı Adı", fontSize = 12.sp, fontFamily = font, modifier = Modifier.padding(10.dp, 0.dp))
                    OutlinedTextField(value = kAdi, onValueChange = { text ->
                        kAdi = text
                    }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                ModernButon(onClick = { GirisButon(kAdi, navController) }, modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp), "Giriş")
            }
        }
        composable<SohbetOdasi> {
            val argumanlar = it.toRoute<SohbetOdasi>()
            var mesaj by remember { mutableStateOf("") }
            val context = LocalContext.current

            var cikisDiyalog by remember { mutableStateOf(false) }
            BackHandler(enabled = true) {
                cikisDiyalog = true
            }
            if (cikisDiyalog) {
                AlertDialog(onDismissRequest = { cikisDiyalog = false }, title = { Text(text = "Çıkış Yap") }, text = { Text(text = "Kullanıcıdan çıkış yapılacak") }, confirmButton = {
                    Button(onClick = {
                        cikisDiyalog = false
                        val ref = FirebaseDatabase.getInstance().getReference().child("Packages").child("OnlineUsers").child(argumanlar.kAdi.toString())
                        ref.removeValue().addOnSuccessListener {
                            Toast.makeText(context, "Çıkış Yapıldı", Toast.LENGTH_SHORT).show()
                        }
                        navController.navigateUp()
                    }) { Text("Çıkış") }
                }, dismissButton = {
                    Button(onClick = {
                        cikisDiyalog = false
                    }) { Text("İptal") }
                })
            }

            val dbRef = FirebaseDatabase.getInstance().getReference()
            val paketler = remember { mutableStateListOf<MesajPaketi>() }


            dbRef.child("Packages").child("MessageLog").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    paketler.clear()
                    if (paketler.isEmpty()) {
                        for (childSnapshot in snapshot.children) {
                            val alinanPaket = childSnapshot.getValue(MesajPaketi::class.java)
                            if (alinanPaket != null) {
                                paketler.add(alinanPaket)
                            }
                        }
                    } else {
                        val alinanPaket = snapshot.children.lastOrNull()?.getValue(MesajPaketi::class.java)
                        if (alinanPaket != null) {
                            paketler.add(alinanPaket)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            val listState = rememberLazyListState()
            LaunchedEffect(paketler.size) {
                if (paketler.isNotEmpty()) {
                    listState.animateScrollToItem(paketler.size - 1, scrollOffset = 0

                    )

                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 0.dp, 16.dp, 0.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Box(Modifier
                    .fillMaxSize()
                    .weight(0.07f)
                    .background(Color.Green)) {
                    IconButton(onClick = {
                        cikisDiyalog = true
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Çıkış", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                    Text(text = argumanlar.kAdi.toString(), Modifier
                        .align(Alignment.BottomEnd)
                        .padding(0.dp, 0.dp, 16.dp, 8.dp))
                }
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .weight(0.9f), state = listState, verticalArrangement = Arrangement.Bottom) {
                    items(paketler) { paket ->
                        if (paket.kadi != "") {
                            MesajSatiri(paket, argumanlar.kAdi)
                        }
                    }
                }
                Row(Modifier
                    .weight(0.1f)
                    .padding(0.dp, 0.dp, 0.dp, 16.dp)) {
                    OutlinedTextField(value = mesaj, singleLine = true, onValueChange = { text ->
                        mesaj = text
                    }, modifier = Modifier
                        .weight(0.80f)
                        .fillMaxHeight()
                        .padding(0.dp, 0.dp, 8.dp, 0.dp))
                    IconButton(onClick = {
                        if (mesaj.isNotBlank()) {
                            val zamanDamgasi = Instant.now()
                            val zamanDilimiAyarlanmisDamga = ZonedDateTime.ofInstant(zamanDamgasi, ZoneId.of("Europe/Istanbul"))
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'|T|'HH:mm:ss|SSSX")
                            val formatlanmisDamga = formatter.format(zamanDilimiAyarlanmisDamga)
                            val paket = MesajPaketi(argumanlar.kAdi.toString(), mesaj, formatlanmisDamga.toString())
                            MesajGonder(paket)
                            mesaj = ""
                        }
                    }, modifier = Modifier
                        .background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))))
                        .weight(0.20f)
                        .fillMaxSize()) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp))
                    }
                }
            }
        }
    }
}

fun MesajGonder(mesajPaketi: MesajPaketi) {
    val dataBaseRef = FirebaseDatabase.getInstance().getReference()
    dataBaseRef.child("Packages").child("MessageLog").child(mesajPaketi.zamanDamgasi.toString()).setValue(mesajPaketi)

}

fun GirisButon(kAdi: String, navController: NavController) {
    val databaseReference = FirebaseDatabase.getInstance().getReference()
    var yeniGirisYap = true
    if (kAdi.length < 3 || kAdi.isEmpty()) {
        Toast.makeText(navController.context, "En az 3 karakterden oluşan bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
        return
    } else {
        databaseReference.child("Packages").child("OnlineUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {
                    databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                    navController.navigate(SohbetOdasi(kAdi))
                } else {
                    for (incelenenKullanici in snapshot.children) {
                        if (incelenenKullanici.value?.equals(kAdi) == true) {
                            Toast.makeText(navController.context, "Kullanıcı online", Toast.LENGTH_SHORT).show()
                            yeniGirisYap = false
                            return
                        } else {
                            yeniGirisYap = true
                        }
                    }
                    if (yeniGirisYap) {
                        databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                        navController.navigate(SohbetOdasi(kAdi))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}

@Composable
fun ModernButon(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier.background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)))),
    ) { Text(text = text) }
}

@Composable
fun MesajSatiri(paket: MesajPaketi, aktifKullanici: String?) {
    Row(modifier = Modifier
        .padding(8.dp)
        .then((if (paket.kadi == aktifKullanici) {
            Modifier.background(Color.Red)
        } else {
            Modifier.background(Color.Yellow)
        })), verticalAlignment = Alignment.CenterVertically) {
        Text(text = paket.kadi, color = Color.Black, modifier = Modifier.weight(0.22f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = paket.mesaj, color = Color.Gray, modifier = Modifier.weight(0.63f))
        Spacer(modifier = Modifier.width(8.dp))
        val zamanDamgasiParcala = paket.zamanDamgasi!!.split("|")
        val SDS = zamanDamgasiParcala[2]
        val SD = SDS.split(":")
        Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.CenterEnd) {
            Text(text = SD[0] + ":" + SD[1], color = Color.Magenta, fontSize = 8.sp)
        }

    }
}


