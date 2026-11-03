package com.madinmusic.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLEncoder
import java.util.Locale

private val Pink = Color(0xFFFF5FA2)
private val Deep = Color(0xFF160D1C)
private val Panel = Color(0xFF28172D)
private val Muted = Color(0xFFC9B5C9)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("ru", "RU")
        }
        setContent { MadinApp() }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    @Composable
    private fun MadinApp() {
        var query by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf("Все источники") }
        var saved by remember { mutableStateOf(false) }
        val context = LocalContext.current

        MaterialTheme(colorScheme = darkColorScheme(
            primary = Pink, background = Deep, surface = Panel,
            onBackground = Color.White, onSurface = Color.White
        )) {
            Surface(Modifier.fillMaxSize(), color = Deep) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .background(Brush.verticalGradient(listOf(Color(0xFF32152F), Deep, Deep)))
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("MADIN MUSIC", color = Pink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                            Text("Найди свой звук ✦", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        }
                        Box(Modifier.size(48.dp).background(Pink.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, null, tint = Pink, modifier = Modifier.size(27.dp))
                        }
                    }

                    // Cute mascot panel; tapping speaks the requested phrase.
                    Row(
                        Modifier.fillMaxWidth().background(
                            Brush.horizontalGradient(listOf(Color(0xFF642B59), Color(0xFF311B3B))),
                            RoundedCornerShape(26.dp)
                        ).clickable {
                            tts?.speak("Очень очень очень много мадин", TextToSpeech.QUEUE_FLUSH, null, "madin-line")
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(100.dp).background(Color(0xFFFFD7E9), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("૮₍ ˶ᵔ ᵕ ᵔ˶ ₎ა\n🎀", color = Color(0xFF9C326D), fontSize = 20.sp, lineHeight = 27.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Твой музыкальный фембой", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(5.dp))
                            Text("Нажми на меня — и я скажу секретную фразу", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(9.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, null, tint = Pink, modifier = Modifier.size(16.dp))
                                Text(" ОЧЕНЬ МНОГО МАДИН", color = Pink, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Text("ПОИСК МУЗЫКИ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Row(
                        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Pink)
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f).padding(vertical = 13.dp)) {
                            if (query.isEmpty()) Text("Название трека или исполнитель…", color = Muted, fontSize = 14.sp)
                            BasicTextField(
                                value = query, onValueChange = { query = it },
                                singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (query.isNotBlank()) Text("✕", color = Muted, modifier = Modifier.clickable { query = "" }.padding(6.dp))
                    }

                    Text("ГДЕ ИСКАТЬ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    val sources = listOf("Все источники", "YouTube", "SoundCloud", "Bandcamp", "Jamendo", "Internet Archive")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sources.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { source ->
                                    FilterChip(
                                        selected = selected == source,
                                        onClick = { selected = source },
                                        label = { Text(source, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Pink.copy(alpha = .24f),
                                            selectedLabelColor = Pink,
                                            labelColor = Muted
                                        )
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (query.isNotBlank()) {
                                val q = URLEncoder.encode(query.trim(), "UTF-8")
                                val url = when (selected) {
                                    "YouTube" -> "https://www.youtube.com/results?search_query=$q"
                                    "SoundCloud" -> "https://soundcloud.com/search?q=$q"
                                    "Bandcamp" -> "https://bandcamp.com/search?q=$q"
                                    "Jamendo" -> "https://www.jamendo.com/search?q=$q"
                                    "Internet Archive" -> "https://archive.org/search?query=$q"
                                    else -> "https://www.google.com/search?q=$q+music+listen+download"
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        },
                        enabled = query.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ИСКАТЬ ТРЕК", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }

                    Row(
                        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Скачивание без сюрпризов", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Открывай страницу трека и используй Download, если автор или сайт разрешает загрузку.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                        Icon(Icons.Default.Favorite, null, tint = Pink, modifier = Modifier.size(25.dp))
                    }

                    Text("БЫСТРЫЕ ПОДСКАЗКИ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("• Ищи по названию + исполнителю для точного результата\n• Jamendo и Internet Archive часто содержат музыку с разрешённым скачиванием\n• Защищённые стриминги не дают скачивать аудио напрямую", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
                    Text("Madin Music • версия 1.0", color = Color(0xFF806B83), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}
