package com.alpha.locationtrackerplayer.ui.Location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpha.locationtrackerplayer.background.LocationScheduler
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.LocationHistory
import com.alpha.locationtrackerplayer.data.model.User
import com.alpha.locationtrackerplayer.navigation.LocalNavigator
import com.alpha.locationtrackerplayer.navigation.Screen
import com.alpha.locationtrackerplayer.ui.theme.AppColors
import com.alpha.locationtrackerplayer.viewModels.LocationViewModel
import io.github.xilinjia.krdb.ext.query
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocationListScreen() {
    val navigator = LocalNavigator.current
    val viewModel: LocationViewModel = viewModel()
    val realm     = RealmManager.realm
    val context   = LocalContext.current

    val locations by viewModel.locations.collectAsState()

    val loggedUser = remember { realm.query<User>("isLoggedIn == true").first().find() }
    val userId     = remember { loggedUser?._id?.toHexString() ?: "" }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadLocations(userId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.NavyDark, AppColors.NavyMid)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(AppColors.NavyCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    // Top bar
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Teal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp)) }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location History", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(loggedUser?.email ?: "", color = AppColors.TextMuted, fontSize = 12.sp)
                        }

                        // Logout
                        TextButton(
                            onClick = {
                                LocationScheduler.stop(context)
                                realm.writeBlocking {
                                    val user = query<User>("isLoggedIn == true").first().find()
                                    findLatest(user!!)?.isLoggedIn = false
                                }
                                navigator.clearAndNavigate(Screen.LoginScreen)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.ErrorRed)
                        ) {
                            Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (locations.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatChip(label = "Points", value = locations.size.toString(), modifier = Modifier.weight(1f))
                            StatChip(label = "Latest", value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(locations.last().timestamp)), modifier = Modifier.weight(1f))
                            StatChip(label = "First", value = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(locations.first().timestamp)), modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(14.dp))

                        // View map button
                        Button(
                            onClick = { navigator.navigate(Screen.MapScreen(userId)) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Brush.horizontalGradient(listOf(AppColors.Teal, AppColors.TealDim))),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppColors.NavyDark, modifier = Modifier.size(18.dp))
                                    Text("Play Route on Map", color = AppColors.NavyDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.TextMuted.copy(alpha = 0.08f)))

            // List / empty state
            if (locations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📡", fontSize = 48.sp)
                        Text("No locations yet", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Background tracking is active.\nLocations appear every 15 minutes.", color = AppColors.TextMuted, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(locations.reversed()) { index, location ->
                        LocationCard(location = location, index = locations.size - index, onClick = {
                            navigator.navigate(Screen.MapScreen(userId))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(AppColors.NavyDark.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = AppColors.Teal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppColors.TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
fun LocationCard(location: LocationHistory, index: Int, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
    val dotColors  = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFF8F00), Color(0xFF8E24AA), Color(0xFF00ACC1))
    val dotColor   = dotColors[(index - 1) % dotColors.size]

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(AppColors.NavyCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Index badge
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(dotColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("#$index", color = dotColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CoordChip("LAT", "%.6f".format(location.latitude))
                CoordChip("LNG", "%.6f".format(location.longitude))
            }
            Text(dateFormat.format(Date(location.timestamp)), color = AppColors.TextMuted, fontSize = 11.sp)
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = AppColors.TextMuted.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun CoordChip(label: String, value: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(AppColors.NavyDark.copy(alpha = 0.8f)).padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.Teal, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, color = AppColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
