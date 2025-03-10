package com.example.text2ukotent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Text2UKotEntTheme {
                AnaEkran()
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
fun AnaEkran() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Giris,
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1000)) },
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1000)) }
    ) {
        composable<Giris> {
            // Kullanıcı adını tutacak state
            var kAdi by remember {
                mutableStateOf("")
            }
            var onlineSayisi by remember {
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
                    //Uygulama logosu
                    Image(
                       painter = painterResource(id=R.drawable.logo_transparent),
                       contentDescription = "Logo şeffaf",

                    )
                    // Kullanıcı adı girişi
                    OutlinedTextField(
                        value = kAdi,
                        //Girilen değer her değiştiğinde çağrılıyor eğer boşluk gelirse kabul etmiyor
                        onValueChange = {
                            if (!it.contains(" ")) kAdi = it
                        },
                        label = { Text(text = "Kullanıcı Adı", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    //Giriş tuşu tasarımı
                    Button(
                        onClick = {
                            if (kAdi.isNotBlank()) {
                                GirisButon(kAdi, navController, onlineSayisi )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
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
            YasamDongusuGozlemcisi(navController, argumanlar.kAdi.toString())
            //Diyalog değeri her değer değiştirdiği zaman ekranda çıkışın belirmesini sağlar
            var cikisDiyalog by remember { mutableStateOf(false) }
            //Geri tuşuna basıldığında çalışır
            BackHandler(enabled = true) {
                cikisDiyalog = true
            }
            if (cikisDiyalog) {
                AlertDialog(
                    onDismissRequest = { cikisDiyalog = false },
                    title = { Text(fontWeight = FontWeight.Bold, text = "Çıkış Yap") },
                    text = { Text(text = "Kullanıcıdan çıkış yapılacak") },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = {
                                cikisDiyalog = false
                                //Çıkış onaylanırsa kullanıcıyı OnlineUsers içinden siler ve giriş ekranına aktarır
                                val ref = FirebaseDatabase.getInstance().getReference().child("Packages").child("OnlineUsers").child(argumanlar.kAdi.toString())
                                ref.removeValue().addOnSuccessListener {
                                    Toast.makeText(context, "Çıkış Yapıldı", Toast.LENGTH_SHORT).show()
                                }
                                navController.navigateUp()
                            }) { Text("Çıkış") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            cikisDiyalog = false
                        }) { Text("İptal") }
                    },
                )
            }

            val dbRef = FirebaseDatabase.getInstance().getReference()
            //Mesaj paketlerinin hatırlanması
            val paketler = remember { mutableStateListOf<MesajPaketi>() }
            dbRef.child("Packages").child("MessageLog").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val alinanPaket = snapshot.getValue(MesajPaketi::class.java)
                    //Paketin boş olmadığı ve gelen son paketin zaten ekli olup olmadığı kontrol edilir. Zaten ekli paket yeniden eklenmez
                    if (alinanPaket != null && !paketler.contains(alinanPaket)) {
                        paketler.add(alinanPaket)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })

            //Paket sayısı değiştiğinde sohbetin otomatik olarak en alta kayması
            val listState = rememberLazyListState()
            LaunchedEffect(paketler.size) {
                if (paketler.isNotEmpty()) {
                    listState.animateScrollToItem(paketler.size - 1, scrollOffset = 0
                    )

                }
            }
            //Mesaj yazma ve görüntüleme bölümlerini içeren container
            Column(modifier = Modifier
                .background(Brush.verticalGradient(listOf( Color(0xFF2F80ED),Color(0xFF56FCA2))))
                .fillMaxSize(),
                horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                //Üst bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF2F80ED))
                )
                {
                    //Sol üstteki çıkış ikonu
                    IconButton(onClick = {
                        cikisDiyalog = true
                    }, modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Çıkış", tint = Color.White)
                    }
                    //Sağ alttaki kullanıcı adı
                    Text(
                        text = argumanlar.kAdi.toString(),
                        color = Color.White,
                        letterSpacing = 1.2.sp,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 8.dp))
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
                //Mesajların listelenmesi için container
                LazyColumn(modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight()
                    .weight(0.9f), state = listState, verticalArrangement = Arrangement.Bottom) {
                    //Her mesaj geldiğinde pakete eklenir ve mesaj için satır oluşturulur
                    items(paketler) { paket ->
                        if (paket.kadi != "") {
                            MesajSatiri(paket, argumanlar.kAdi)
                        }
                    }
                }
                //Alt taraftaki mesaj gönderme kısmı
                Row(Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 5.dp)
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {

                    //Mesaj girilen bölüm
                    OutlinedTextField(
                        value = mesaj,
                        onValueChange = { mesaj = it },
                        placeholder = { Text(text = "Mesajınızı yazın...") },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .fillMaxWidth(.85f)
                    )
                    //Mesaj gönderme tuşu
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send,tint=Color.White, contentDescription = "Gönder",
                            modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

//Mesajı veri tabanına yaz
fun MesajGonder(mesajPaketi: MesajPaketi) {
    val dataBaseRef = FirebaseDatabase.getInstance().getReference()
    dataBaseRef.child("Packages").child("MessageLog").child(mesajPaketi.zamanDamgasi.toString()).setValue(mesajPaketi)
}

fun GirisButon(kAdi: String, navController: NavController,onlineSayisi:String) {
    val databaseReference = FirebaseDatabase.getInstance().getReference()
    var yeniGirisYap = true
    //Kullanıcı adı kontrolü 3-10 karakter
    if (kAdi.length > 10 || kAdi.length < 3 || kAdi.isEmpty()) {
        Toast.makeText(navController.context, "3-10 karakterden oluşan bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
        return
    } else {
        //Veri tabanındaki OnlineUsers kısmına giriş yapan kullanıcının adı yazılıyor
        databaseReference.child("Packages").child("OnlineUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Online kullanıcı yoksa kontrol yapılmaz doğrudan eklenir
                if (!snapshot.hasChildren()) {
                    databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                    navController.navigate(SohbetOdasi(kAdi))
                } else {
                    for (incelenenKullanici in snapshot.children) {
                        //Kullanıcı zaten OnlineUsers içindeyse girişe izin verilmiyor
                        if (incelenenKullanici.value?.equals(kAdi) == true) {
                            Toast.makeText(navController.context, "Kullanıcı online", Toast.LENGTH_SHORT).show()
                            yeniGirisYap = false
                            return
                        } else {
                            yeniGirisYap = true
                        }
                    }
                    //Kullanıcı girişine izin veriliyor ve veritabanı güncelleniyor
                    if (yeniGirisYap) {
                        databaseReference.child("Packages").child("OnlineUsers").child(kAdi).setValue(kAdi)
                        databaseReference.child("Packages").child("OnlineUsers").addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                               // onlineSayisi=snapshot.children.count().toString()
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                        navController.navigate(SohbetOdasi(kAdi))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}

@Composable
fun MesajSatiri(
    paket: MesajPaketi, aktifKullanici: String?
) {
    //Mesajların zaman damgası parçalanıyor
    val zamanDamgasiParcala = paket.zamanDamgasi!!.split("|")
    val SDS = zamanDamgasiParcala[2]
    val SD = SDS.split(":")
    val YGA = zamanDamgasiParcala[0]
    val AG = YGA.split("-")
    var zaman by remember { mutableStateOf(SD[0] + ":" + SD[1]) }
    //En dış container
    Row(
        modifier = Modifier.fillMaxWidth(),
        // Eklenecek mesaj aktif olan kullanıcıdansa sağa yaslamasını sağlıyor
        horizontalArrangement = if (aktifKullanici == paket.kadi) Arrangement.End else Arrangement.Start
    ) {
        //Mesaj kutusunun özellikleri
        Column(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = if (aktifKullanici == paket.kadi) Color(0xFF315D8D) else Color(0xFFEDEEDA),
                )
                .padding(12.dp)
                .widthIn(min = 80.dp, max = 265.dp)
        ) {
            Text( //Kullanıcı adı
                text = paket.kadi,
                color = if (aktifKullanici == paket.kadi) Color.LightGray else Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                //Mesaj
                text = paket.mesaj,
                color = if (aktifKullanici == paket.kadi) Color.White else Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
            Text( //Zaman damgasi
                text = zaman,
                color = if (aktifKullanici == paket.kadi) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .clickable {
                        zaman = AG[1] + "/" + AG[2] + "|" + SD[0] + ":" + SD[1]
                    }
                    .align(Alignment.End)
                    .padding(top = 4.dp)

            )

        }
    }
}

@Composable
fun YasamDongusuGozlemcisi(navController: NavController, kadi: String) {
    var coRoutine: Job? = null
    val donguSahibi = ProcessLifecycleOwner.get()
    val donguGozlemcisi = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {}
            //Uygulama doğrudan kapatılırsa çıkış yapar
            Lifecycle.Event.ON_START -> {
                FirebaseDatabase.getInstance().getReference().child("Packages").child("OnlineUsers").child(kadi).onDisconnect().removeValue()
            }
            Lifecycle.Event.ON_RESUME -> {
                Log.println(Log.INFO, "ismet", "devam etti")
                coRoutine?.cancel()
            }
            Lifecycle.Event.ON_PAUSE -> {}
            Lifecycle.Event.ON_STOP -> {
                // Uygulama arka plandaysa bekler ve zaman aşımında çıkış yapar
                coRoutine = CoroutineScope(Dispatchers.IO).launch {
                    Log.println(Log.INFO, "ismet", "içerideyim")
                    delay(300000)
                    FirebaseDatabase.getInstance().getReference().child("Packages").child("OnlineUsers").child(kadi).removeValue()
                    navController.navigate(Giris)
                    Log.println(Log.INFO, "ismet", "bekledim ve bitirdim")
                }
            }
            Lifecycle.Event.ON_DESTROY -> {}
            Lifecycle.Event.ON_ANY -> {}
        }

    }
    DisposableEffect(donguSahibi) {
        donguSahibi.lifecycle.addObserver(donguGozlemcisi)
        onDispose {
            donguSahibi.lifecycle.removeObserver(donguGozlemcisi)
        }
    }
}






