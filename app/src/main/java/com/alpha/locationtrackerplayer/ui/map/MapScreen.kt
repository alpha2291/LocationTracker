package com.alpha.locationtrackerplayer.ui.map

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.LocationHistory
import com.alpha.locationtrackerplayer.navigation.LocalNavigator
import com.alpha.locationtrackerplayer.ui.theme.AppColors
import io.github.xilinjia.krdb.ext.query
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapScreen(userId: String) {
    val context   = LocalContext.current
    val navigator = LocalNavigator.current
    val realm     = RealmManager.realm

    val locations = remember {
        realm.query<LocationHistory>("userId == $0 SORT(timestamp ASC)", userId).find().toList()
    }

    var isPlaying    by remember { mutableStateOf(false) }
    var playbackIndex by remember { mutableIntStateOf(0) }

    val segmentColors = listOf(
        0xFFE53935.toInt(), 0xFF1E88E5.toInt(), 0xFF43A047.toInt(), 0xFFFF8F00.toInt(),
        0xFF8E24AA.toInt(), 0xFF00ACC1.toInt(), 0xFFFF5722.toInt(), 0xFF3949AB.toInt()
    )

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
        }
    }

    fun redrawMap(visibleUpTo: Int) {
        mapView.overlays.clear()
        if (locations.isEmpty()) { mapView.invalidate(); return }

        val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        val count = minOf(visibleUpTo + 1, locations.size)

        for (i in 0 until count - 1) {
            val polyline = Polyline(mapView).apply {
                setPoints(listOf(
                    GeoPoint(locations[i].latitude, locations[i].longitude),
                    GeoPoint(locations[i + 1].latitude, locations[i + 1].longitude)
                ))
                outlinePaint.color = segmentColors[i % segmentColors.size]
                outlinePaint.strokeWidth = 12f
                outlinePaint.isAntiAlias = true
            }
            mapView.overlays.add(polyline)
        }

        for (i in 0 until count) {
            val loc = locations[i]
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(loc.latitude, loc.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title   = "Point ${i + 1}"
                snippet = dateFormat.format(Date(loc.timestamp))
                alpha   = if (i == visibleUpTo) 1.0f else 0.7f
            })
        }

        val current = locations[minOf(visibleUpTo, locations.size - 1)]
        mapView.controller.animateTo(GeoPoint(current.latitude, current.longitude))
        mapView.invalidate()
    }

    LaunchedEffect(Unit) {
        if (locations.isNotEmpty()) {
            mapView.controller.setZoom(16.0)
            mapView.controller.setCenter(GeoPoint(locations.first().latitude, locations.first().longitude))
            redrawMap(locations.size - 1)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying && locations.size > 1) {
            if (playbackIndex >= locations.size - 1) { playbackIndex = 0; redrawMap(0); delay(600L) }
            while (playbackIndex < locations.size - 1 && isPlaying) {
                playbackIndex++
                redrawMap(playbackIndex)
                delay(800L)
            }
            isPlaying = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Back button (top left)
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding()) {
            IconButton(
                onClick = { navigator.pop() },
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(AppColors.NavyCard.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary, modifier = Modifier.size(20.dp))
            }
        }

        // Top info chip
        if (locations.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).statusBarsPadding()) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(AppColors.NavyCard.copy(alpha = 0.92f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isPlaying) AppColors.Teal else AppColors.TextMuted.copy(alpha = 0.4f)))
                    Text(
                        text = if (isPlaying) "Playing  ${playbackIndex + 1} / ${locations.size}"
                               else "${locations.size} locations recorded",
                        color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom controls panel
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (locations.size > 1) {
                // Progress bar
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppColors.NavyCard.copy(alpha = 0.92f)).padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Point ${playbackIndex + 1}", color = AppColors.Teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("of ${locations.size}", color = AppColors.TextMuted, fontSize = 12.sp)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(AppColors.TextMuted.copy(alpha = 0.15f))) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (locations.size > 1) playbackIndex.toFloat() / (locations.size - 1).toFloat() else 1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.horizontalGradient(listOf(AppColors.Teal, AppColors.TealDim)))
                            )
                        }
                    }
                }

                // Playback buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Show All
                    OutlinedButton(
                        onClick = { isPlaying = false; playbackIndex = locations.size - 1; redrawMap(locations.size - 1) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextMuted),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        enabled = !isPlaying
                    ) { Text("Show All", fontSize = 13.sp) }

                    // Play / Pause / Replay
                    Button(
                        onClick = {
                            if (isPlaying) { isPlaying = false }
                            else if (playbackIndex >= locations.size - 1) { playbackIndex = 0; redrawMap(0); isPlaying = true }
                            else { isPlaying = true }
                        },
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(AppColors.Teal, AppColors.TealDim))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    isPlaying -> "⏸  Pause"
                                    playbackIndex >= locations.size - 1 -> "↺  Replay"
                                    playbackIndex > 0 -> "▶  Resume"
                                    else -> "▶  Play Route"
                                },
                                color = AppColors.NavyDark, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                }

            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(AppColors.NavyCard.copy(alpha = 0.92f)).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (locations.isEmpty()) "No locations recorded yet." else "Need 2+ locations to play route.",
                        color = AppColors.TextMuted, fontSize = 14.sp
                    )
                }
            }
        }
    }
}
