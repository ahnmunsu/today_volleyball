package com.example.todayvolleyball.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.todayvolleyball.util.verticalScrollbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정 및 알림 관리") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val matchAlarmEnabled by viewModel.matchAlarmEnabled.collectAsState()
        val matchResultAlarmEnabled by viewModel.matchResultAlarmEnabled.collectAsState()
        val dailyAlarmEnabled by viewModel.dailyAlarmEnabled.collectAsState()
        val dailyAlarmHour by viewModel.dailyAlarmHour.collectAsState()
        val dailyAlarmMinute by viewModel.dailyAlarmMinute.collectAsState()
        val reservedSchedules by viewModel.reservedSchedules.collectAsState()
        
        var showSyncToast by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        val context = LocalContext.current

        val formattedTime = remember(dailyAlarmHour, dailyAlarmMinute) {
            val amPm = if (dailyAlarmHour < 12) "오전" else "오후"
            val displayHour = if (dailyAlarmHour % 12 == 0) 12 else dailyAlarmHour % 12
            String.format(Locale.KOREA, "%s %02d시 %02d분", amPm, displayHour, dailyAlarmMinute)
        }

        if (showTimePicker) {
            var selectedHour by remember { mutableStateOf(dailyAlarmHour) }
            var selectedMinute by remember { mutableStateOf(dailyAlarmMinute) }

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("알림 시간 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateDailyAlarmTime(selectedHour, selectedMinute, context)
                        showTimePicker = false
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("취소")
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        WheelTimePicker(
                            initialHour = dailyAlarmHour,
                            initialMinute = dailyAlarmMinute,
                            onTimeChanged = { hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            }
                        )
                    }
                }
            )
        }

        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            // 1. 경기 개별 알림 설정
            Text("경기 개별 알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("달력에서 예약한 알림 활성화")
                Switch(
                    checked = matchAlarmEnabled,
                    onCheckedChange = { viewModel.toggleMatchAlarm(it) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("당일 경기 종료 결과 알림 수신")
                Switch(
                    checked = matchResultAlarmEnabled,
                    onCheckedChange = { viewModel.toggleMatchResultAlarm(it) }
                )
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 2. 예약된 경기 알림 목록 (1회성 알림)
            Text("예약된 경기 알림 목록 (${reservedSchedules.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (reservedSchedules.isEmpty()) {
                Text(
                    text = "달력에서 알림 예약된 경기가 없습니다.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScrollbar(
                            state = listState,
                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            width = 6.dp,
                            paddingEnd = 2.dp
                        ),
                    contentPadding = PaddingValues(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reservedSchedules, key = { it.gnum }) { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${schedule.date} ${schedule.time} (${schedule.place})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${schedule.homeTeamName} vs ${schedule.awayTeamName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.cancelReservedAlarm(schedule, context) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "삭제",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 3. 매일 알림 설정
            Text("매일 알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("경기가 있는 날 매일 알림 수신")
                Switch(
                    checked = dailyAlarmEnabled,
                    onCheckedChange = { viewModel.toggleDailyAlarm(it, context) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("알림 시간")
                TextButton(onClick = { showTimePicker = true }) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 4. 데이터 수동 동기화
            Text("데이터 관리", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { 
                    viewModel.syncData()
                    showSyncToast = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("리그 정보 수동 업데이트")
            }
            
            if (showSyncToast) {
                Text("최신 리그 정보를 수동으로 동기화합니다.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    var isAm by remember(initialHour) { mutableStateOf(initialHour < 12) }
    var hour12 by remember(initialHour) { mutableStateOf(if (initialHour % 12 == 0) 12 else initialHour % 12) }
    var minute by remember(initialMinute) { mutableStateOf(initialMinute) }

    fun notifyTimeChange(am: Boolean, h: Int, m: Int) {
        val h24 = when {
            am && h == 12 -> 0
            am -> h
            !am && h == 12 -> 12
            else -> h + 12
        }
        onTimeChanged(h24, m)
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        // 1. 오전 / 오후 휠
        AndroidView(
            factory = { ctx ->
                android.widget.NumberPicker(ctx).apply {
                    minValue = 0
                    maxValue = 1
                    displayedValues = arrayOf("오전", "오후")
                    value = if (isAm) 0 else 1
                    setOnValueChangedListener { _, _, newVal ->
                        isAm = (newVal == 0)
                        notifyTimeChange(isAm, hour12, minute)
                    }
                }
            },
            update = { view ->
                val targetVal = if (isAm) 0 else 1
                if (view.value != targetVal) {
                    view.value = targetVal
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 시 (1 ~ 12) 휠
        AndroidView(
            factory = { ctx ->
                android.widget.NumberPicker(ctx).apply {
                    minValue = 1
                    maxValue = 12
                    value = hour12
                    setOnValueChangedListener { _, _, newVal ->
                        hour12 = newVal
                        notifyTimeChange(isAm, hour12, minute)
                    }
                }
            },
            update = { view ->
                if (view.value != hour12) {
                    view.value = hour12
                }
            }
        )

        Text("시", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(8.dp))

        // 3. 분 (00 ~ 59) 휠
        AndroidView(
            factory = { ctx ->
                android.widget.NumberPicker(ctx).apply {
                    minValue = 0
                    maxValue = 59
                    setFormatter { String.format(Locale.KOREA, "%02d", it) }
                    value = minute
                    setOnValueChangedListener { _, _, newVal ->
                        minute = newVal
                        notifyTimeChange(isAm, hour12, minute)
                    }
                }
            },
            update = { view ->
                if (view.value != minute) {
                    view.value = minute
                }
            }
        )

        Text("분", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
    }
}
