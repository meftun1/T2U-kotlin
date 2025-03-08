package com.example.text2ukotent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Giris) {
        composable<Giris> {
            // Kullanıcı adını tutacak state
            var kAdi by remember {
                mutableStateOf("")
            }
            // Arka plan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Uygulamanın Adı
                    Text(
                        text = "T2U",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Kullanıcı Adı Girişi
                    OutlinedTextField(
                        value = kAdi,
                        onValueChange = { kAdi = it },
                        label = { Text(text = "Kullanıcı Adı", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (kAdi.isNotBlank()) {
                                GirisButon(kAdi, navController)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F80ED)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Giriş", color = Color.White, fontSize = 18.sp)
                    }
                }
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
                AlertDialog(onDismissRequest = { cikisDiyalog = false },
                    title = { Text(fontWeight = FontWeight.Bold, text = "Çıkış Yap") },
                    text = { Text(text = "Kullanıcıdan çıkış yapılacak") },
                    confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = {
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
                },
                )
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
                .fillMaxSize(),
                horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF2F80ED)) // Üst barın arka plan rengi
                )
                {
                    IconButton(onClick = {
                        cikisDiyalog = true
                    }, modifier = Modifier
                        .align(Alignment.CenterStart) // Sol üstte konumlandırma
                        .padding(start = 8.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Çıkış", tint = Color.White)
                    }
                    Text(
                        text = argumanlar.kAdi.toString(),
                        color = Color.White,
                        letterSpacing = 1.2.sp,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 8.dp))
                }
                LazyColumn(modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight()
                    .weight(0.9f), state = listState, verticalArrangement = Arrangement.Bottom) {
                    items(paketler) { paket ->
                        if (paket.kadi != "") {
                            MesajSatiri(paket, argumanlar.kAdi)
                        }
                    }
                }
                Row(Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {

                    OutlinedTextField(
                        value = mesaj,
                        onValueChange = { mesaj = it },
                        placeholder = { Text(text = "Mesajınızı yazın...") },
                        /*colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            backgroundColor = Color.White
                        ),*/
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        modifier = Modifier.padding(end = 5.dp)
                            .fillMaxWidth(.85f)
                    )

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
                        .size(48.dp)
                        .background(Color(0xFF2F80ED), shape = CircleShape))
                    {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder",
                            modifier = Modifier.padding(4.dp))
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

/*
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
*/
@Composable
fun MesajSatiri(
    paket: MesajPaketi, aktifKullanici: String?
) {
    val zamanDamgasiParcala = paket.zamanDamgasi!!.split("|")
    val SDS = zamanDamgasiParcala[2]
    val SD = SDS.split(":")
    Row(
        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement = if (aktifKullanici == paket.kadi) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = if (aktifKullanici == paket.kadi) Color(0xFF2F80ED) else Color(0xFFEDEDED),
                )
                .padding(12.dp)
                .widthIn(min = 80.dp, max = 265.dp)
        ) {
            Text(
                text = paket.kadi,
                color = if (aktifKullanici == paket.kadi) Color.LightGray else Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = paket.mesaj,
                color = if (aktifKullanici == paket.kadi) Color.White else Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = SD[0] + ":" + SD[1],
                color = if (aktifKullanici == paket.kadi) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}

