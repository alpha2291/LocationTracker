package com.alpha.locationtrackerplayer.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.User
import com.alpha.locationtrackerplayer.navigation.LocalNavigator
import com.alpha.locationtrackerplayer.navigation.Screen
import com.alpha.locationtrackerplayer.ui.theme.AppColors
import io.github.xilinjia.krdb.ext.query

@Composable
fun SignupScreen() {
    val navigator = LocalNavigator.current
    val realm     = RealmManager.realm

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.NavyDark, AppColors.NavyMid, AppColors.NavyDark)))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(3.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, AppColors.Teal, AppColors.TealDim, Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(AppColors.Teal.copy(alpha = 0.2f), AppColors.TealDim.copy(alpha = 0.1f)))),
                contentAlignment = Alignment.Center
            ) { Text("🗺️", fontSize = 30.sp) }

            Spacer(Modifier.height(20.dp))

            Text("Create Account", color = AppColors.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            Text("Start tracking your journey", color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(AppColors.NavyCard).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(label = "EMAIL", value = email, onChange = { email = it; errorMessage = "" },
                    placeholder = "you@email.com", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)

                AppTextField(label = "PASSWORD", value = password, onChange = { password = it; errorMessage = "" },
                    placeholder = "Min. 6 characters", icon = Icons.Default.Lock, keyboardType = KeyboardType.Password,
                    isPassword = true, showPassword = showPassword, onTogglePassword = { showPassword = !showPassword })

                // Password strength indicator
                if (password.isNotEmpty()) {
                    val strength = when {
                        password.length < 4 -> 0
                        password.length < 6 -> 1
                        password.length < 10 -> 2
                        else -> 3
                    }
                    val strengthColor = listOf(AppColors.ErrorRed, Color(0xFFFF8A00), Color(0xFFFFD600), AppColors.Teal)[strength]
                    val strengthText  = listOf("Too short", "Weak", "Good", "Strong")[strength]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                                    .background(if (i <= strength) strengthColor else AppColors.TextMuted.copy(alpha = 0.15f))
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(strengthText, color = strengthColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(AppColors.ErrorRed.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", fontSize = 14.sp)
                        Text(errorMessage, color = AppColors.ErrorRed, fontSize = 13.sp)
                    }
                }

                GradientButton(text = "Create Account", isLoading = isLoading, onClick = {
                    when {
                        email.isBlank() || password.isBlank() -> errorMessage = "Please fill in all fields"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        realm.query<User>("email == $0", email).first().find() != null -> errorMessage = "Email already registered"
                        else -> {
                            isLoading = true
                            realm.writeBlocking {
                                copyToRealm(User().apply {
                                    this.email = email
                                    this.password = password
                                    this.isLoggedIn = false
                                })
                            }
                            navigator.navigate(Screen.LoginScreen)
                        }
                    }
                })
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = AppColors.TextMuted, fontSize = 14.sp)
                TextButton(onClick = { navigator.pop() }, contentPadding = PaddingValues(4.dp)) {
                    Text("Sign In", color = AppColors.Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
