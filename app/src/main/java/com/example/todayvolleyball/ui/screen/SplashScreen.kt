package com.example.todayvolleyball.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todayvolleyball.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "volleyball_pulse")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1600)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1), // Deep Volleyball Blue
                        Color(0xFF0A192F)  // Dark Navy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Volleyball Graphic Icon
            Image(
                painter = painterResource(id = R.drawable.ic_volleyball),
                contentDescription = "Today Volleyball Logo",
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "오늘의 배구",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "2026-2027 프로배구",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFD54F) // Gold Accent
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "경기 일정 & 알림",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
