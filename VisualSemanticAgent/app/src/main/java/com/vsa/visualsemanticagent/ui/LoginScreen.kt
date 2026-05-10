package com.vsa.visualsemanticagent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val LoginPrimary = Color(0xFFF0C24A)
private val LoginOnBackground = Color(0xFF4A3810)
private val LoginMuted = Color(0xFF8B7337)
private val LoginOutline = Color(0xFFEEDDA1)
private val LoginCard = Color(0xFFFFFDF8)
private val LoginGlowTop = Color(0xFFFFE8A6)
private val LoginGlowBottom = Color(0xFFFFD98A)

@Composable
fun LoginScreen(onEnterApp: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFBEF),
                        Color(0xFFFFFCF8),
                        Color(0xFFFFF2CC)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 32.dp)
                .fillMaxWidth(0.72f)
                .height(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(LoginGlowTop.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 120.dp)
                .fillMaxWidth(0.62f)
                .height(220.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(LoginGlowBottom.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Surface(
                    modifier = Modifier.height(72.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = LoginPrimary
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "织时",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "织时",
                    color = LoginOnBackground,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "将校园信息碎片整理成清晰时间线",
                    color = LoginMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = LoginCard,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "欢迎回来",
                        color = LoginOnBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("学号或邮箱") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = LoginPrimary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions.Default,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoginPrimary,
                            unfocusedBorderColor = LoginOutline,
                            focusedLabelColor = LoginPrimary,
                            unfocusedLabelColor = LoginMuted,
                            cursorColor = LoginPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("密码") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = LoginPrimary)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions.Default,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoginPrimary,
                            unfocusedBorderColor = LoginOutline,
                            focusedLabelColor = LoginPrimary,
                            unfocusedLabelColor = LoginMuted,
                            cursorColor = LoginPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = onEnterApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoginPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "进入织时",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        }
    }
}
