package com.insan.syamsunk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DarkBg = Color(0xFF1A232A)
private val CardBg = Color(0xFF222D35)
private val TealHighlight = Color(0xFF0D7A75)
private val AmberYellow = Color(0xFFFFC107)
private val TextWhite = Color.White
private val TextMuted = Color(0xFFB0BEC5)

@Composable
fun PrayerScreen(viewModel: PrayerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            currentMethod = state.calculationMethod,
            currentMadhab = state.madhab,
            onDismiss = { showSettingsDialog = false },
            onSave = { method, madhab ->
                viewModel.updateSettings(method, madhab)
                showSettingsDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (state.isLoading && state.prayerTimes.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = TealHighlight
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                // Header
                HeaderSection(
                    address = state.address,
                    onSettingsClick = { showSettingsDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Countdown Card
                HeroCountdownSection(
                    prayerName = state.nextPrayerName,
                    prayerTime = state.nextPrayerTime,
                    countdown = state.countdownText
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 2.dp,
                    color = TealHighlight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Prayer List
                PrayerListSection(
                    prayers = state.prayerTimes,
                    activeIndex = state.activePrayerIndex
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    address: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Prayer Times",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (address.isNotEmpty()) {
                Text(
                    text = address,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
        Row {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroCountdownSection(
    prayerName: String,
    prayerTime: String,
    countdown: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = prayerName,
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = prayerTime,
            color = AmberYellow,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Remaining",
            color = TextWhite,
            fontSize = 16.sp
        )
        Text(
            text = countdown,
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PrayerListSection(
    prayers: List<PrayerTimeItem>,
    activeIndex: Int
) {
    Column {
        prayers.forEachIndexed { index, item ->
            val isActive = index == activeIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) TealHighlight else Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = item.time,
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
