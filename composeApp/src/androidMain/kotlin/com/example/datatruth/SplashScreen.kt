package com.example.datatruth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.alpha


@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val alpha by animateFloatAsState(
                targetValue = 1f,
                label = "splashFade"
            )

            Image(
                painter = painterResource(id = R.drawable.ic_datatruth_logo),
                contentDescription = "DataTruth Logo",
                modifier = Modifier
                    .size(180.dp)
                    .alpha(alpha)
            )

        }
    }
}
