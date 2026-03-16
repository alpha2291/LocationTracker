package com.alpha.locationtrackerplayer.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpha.locationtrackerplayer.background.LocationScheduler
import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.User
import com.alpha.locationtrackerplayer.navigation.LocalNavigator
import com.alpha.locationtrackerplayer.navigation.Screen
import com.alpha.locationtrackerplayer.ui.theme.AppColors
import io.github.xilinjia.krdb.ext.query

@Composable
fun LoginScreen() {
    val navigator = LocalNavigator.current
    val context   = LocalContext.current
    val realm     = RealmManager.realm

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.NavyDark, AppColors.NavyMid, AppColors.NavyDark)))
    ) {
        // Top accent bar
        Box(
            modifier = Modifier.fillMaxWidth().height(3.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, AppColors.Teal, AppColors.TealDim, Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(AppColors.Teal.copy(alpha = 0.2f), AppColors.TealDim.copy(alpha = 0.1f)))),
                contentAlignment = Alignment.Center
            ) { Text("📍", fontSize = 32.sp) }

            Spacer(Modifier.height(20.dp))

            Text("Welcome Back", color = AppColors.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            Text("Sign in to continue tracking", color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(36.dp))

            // Form card
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(AppColors.NavyCard).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(label = "EMAIL", value = email, onChange = { email = it; errorMessage = "" },
                    placeholder = "you@email.com", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)

                AppTextField(label = "PASSWORD", value = password, onChange = { password = it; errorMessage = "" },
                    placeholder = "••••••••", icon = Icons.Default.Lock, keyboardType = KeyboardType.Password,
                    isPassword = true, showPassword = showPassword, onTogglePassword = { showPassword = !showPassword })

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

                GradientButton(
                    text = "Sign In",
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        val user = realm.query<User>("email == $0 AND password == $1", email, password).first().find()
                        if (user != null) {
                            realm.writeBlocking { findLatest(user)?.isLoggedIn = true }
                            LocationScheduler.start(context, user._id.toHexString())
                            navigator.clearAndNavigate(Screen.LocationListScreen)
                        } else {
                            errorMessage = "Invalid email or password"
                            isLoading = false
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New here? ", color = AppColors.TextMuted, fontSize = 14.sp)
                TextButton(onClick = { navigator.navigate(Screen.SignupScreen) }, contentPadding = PaddingValues(4.dp)) {
                    Text("Create account", color = AppColors.Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AppTextField(
    label: String, value: String, onChange: (String) -> Unit,
    placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false, showPassword: Boolean = false, onTogglePassword: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = AppColors.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        OutlinedTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = AppColors.TextMuted.copy(alpha = 0.4f), fontSize = 14.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (isPassword && onTogglePassword != null) {{
                TextButton(onClick = onTogglePassword, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text(if (showPassword) "Hide" else "Show", color = AppColors.Teal, fontSize = 12.sp)
                }
            }} else null,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Teal,
                unfocusedBorderColor = AppColors.TextMuted.copy(alpha = 0.15f),
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.Teal,
                focusedContainerColor = AppColors.NavyDark.copy(alpha = 0.6f),
                unfocusedContainerColor = AppColors.NavyDark.copy(alpha = 0.6f),
            )
        )
    }
}

@Composable
fun GradientButton(text: String, isLoading: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .then(
                    if (isLoading)
                        Modifier.background(AppColors.TealDim.copy(alpha = 0.5f))
                    else Modifier.background( Brush.horizontalGradient(listOf(AppColors.Teal, AppColors.TealDim)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) CircularProgressIndicator(color = AppColors.NavyDark, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text(text, color = AppColors.NavyDark, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
        }
    }
}
