package com.example.todayvolleyball.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todayvolleyball.R
import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.ui.theme.MenLoseColor
import com.example.todayvolleyball.ui.theme.MenWinColor
import com.example.todayvolleyball.ui.theme.WomenLoseColor
import com.example.todayvolleyball.ui.theme.WomenWinColor
import com.example.todayvolleyball.util.KoreanHolidays
import io.github.boguszpawlowski.composecalendar.SelectableWeekCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableWeekCalendarState
import io.github.boguszpawlowski.composecalendar.week.Week
import java.time.LocalDate
import java.time.YearMonth

data class TeamFilterItem(
    val name: String,
    val colorHex: String,
    val isMen: Boolean
)

@Composable
fun CalendarScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val scheduledAlarms by viewModel.scheduledAlarms.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // DataStore에 저장된 필터 상태 연동 (앱 재시작 시에도 유지)
    val selectedLeague by viewModel.savedLeagueFilter.collectAsState()
    val selectedTeams by viewModel.selectedTeams.collectAsState()

    // 주간 달력 상태 관리
    val calendarState = rememberSelectableWeekCalendarState(
        initialSelection = listOf(LocalDate.now())
    )
    val selectedDate = calendarState.selectionState.selection.firstOrNull() ?: LocalDate.now()

    // 팀 목록 데이터 추출
    val allTeams = remember(schedules) {
        val list = mutableListOf<TeamFilterItem>()
        val seen = mutableSetOf<String>()
        schedules.forEach { s ->
            if (seen.add(s.homeTeamShortName)) {
                list.add(TeamFilterItem(s.homeTeamShortName, s.homeTeamColor, s.isMen))
            }
            if (seen.add(s.awayTeamShortName)) {
                list.add(TeamFilterItem(s.awayTeamShortName, s.awayTeamColor, s.isMen))
            }
        }
        list.sortedBy { it.name }
    }

    val womenTeams = remember(allTeams) { allTeams.filter { !it.isMen } }
    val menTeams = remember(allTeams) { allTeams.filter { it.isMen } }

    // 남여 리그 선택에 따른 가용한 팀 필터 목록
    val availableTeams = remember(selectedLeague, allTeams) {
        when (selectedLeague) {
            "MEN" -> menTeams
            "WOMEN" -> womenTeams
            else -> allTeams
        }
    }

    // 리그 선택 변경 시 선택된 팀 중 가용 목록에 없는 팀 제거
    LaunchedEffect(selectedLeague) {
        if (selectedTeams.isNotEmpty()) {
            val validTeams = selectedTeams.filter { name -> availableTeams.any { it.name == name } }.toSet()
            if (validTeams != selectedTeams) {
                viewModel.updateSelectedTeams(validTeams)
            }
        }
    }

    // 스케줄 필터링 헬퍼 함수
    fun isScheduleMatchingFilter(schedule: Schedule): Boolean {
        val leagueMatch = when (selectedLeague) {
            "MEN" -> schedule.isMen
            "WOMEN" -> !schedule.isMen
            else -> true
        }
        val teamMatch = if (selectedTeams.isEmpty()) {
            true
        } else {
            schedule.homeTeamShortName in selectedTeams || schedule.awayTeamShortName in selectedTeams
        }
        return leagueMatch && teamMatch
    }
    
    // 선택된 날짜와 필터에 맞게 스케줄 필터링 (여자부 경기를 우선 정렬)
    val filteredSchedules = schedules.filter { schedule ->
        val scheduleDate = LocalDate.parse(schedule.date) // yyyy-MM-dd
        scheduleDate == selectedDate && isScheduleMatchingFilter(schedule)
    }.sortedBy { it.isMen } // false(여자부)가 먼저, true(남자부)가 나중에 정렬됨

    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refreshData()
            Toast.makeText(context, "최신 리그 정보를 수동으로 동기화합니다.", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 최상단 "오늘의 배구" 로고 및 타이틀 (터치 시 오늘 날짜로 즉시 이동)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val today = LocalDate.now()
                                calendarState.weekState.currentWeek = Week.now()
                                calendarState.selectionState.onDateSelected(today)
                                Toast.makeText(context, "오늘 날짜로 이동했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volleyball),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "오늘의 배구", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            item {
                // 주간 달력 (기본 헤더를 커스텀 월 이동 컨트롤러로 오버라이드하여 단 하나만 표시)
                SelectableWeekCalendar(
                    calendarState = calendarState,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    weekHeader = { weekState ->
                        val currentYearMonth = weekState.currentWeek.yearMonth
                        val yearMonthText = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    weekState.currentWeek = weekState.currentWeek.minusWeeks(4)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "이전 달"
                                )
                            }

                            Text(
                                text = yearMonthText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    weekState.currentWeek = weekState.currentWeek.plusWeeks(4)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "다음 달"
                                )
                            }
                        }
                    },
                    dayContent = { dayState ->
                        val date = dayState.date
                        val isSelected = calendarState.selectionState.selection.contains(date)
                        val isToday = dayState.isCurrentDay || date == LocalDate.now()

                        // 달력 셀 내부 경기들 중 여자부(isMen = false) 경기를 위로 배치!
                        val matches = schedules.filter { 
                            LocalDate.parse(it.date) == date && isScheduleMatchingFilter(it) 
                        }.sortedBy { it.isMen }
                        
                        // 공휴일(일요일 포함) = 빨간색, 토요일 = 파란색
                        val dateTextColor = when {
                            KoreanHolidays.isHoliday(date) -> Color(0xFFE53935)
                            KoreanHolidays.isSaturday(date) -> Color(0xFF1E88E5)
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val cellShape = RoundedCornerShape(8.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                                .then(
                                    if (isToday) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cellShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = cellShape
                                )
                                .clip(cellShape)
                                .clickable { calendarState.selectionState.onDateSelected(date) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = dateTextColor
                            )
                            
                            matches.forEach { match ->
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                val homeColor = if (match.isFinished) {
                                    if (match.homePoint > match.awayPoint) (if (match.isMen) MenWinColor else WomenWinColor)
                                    else (if (match.isMen) MenLoseColor else WomenLoseColor)
                                } else MaterialTheme.colorScheme.onSurface
                                
                                val awayColor = if (match.isFinished) {
                                    if (match.awayPoint > match.homePoint) (if (match.isMen) MenWinColor else WomenWinColor)
                                    else (if (match.isMen) MenLoseColor else WomenLoseColor)
                                } else MaterialTheme.colorScheme.onSurface

                                // 팀명 (배경색 = 팀 컬러, 모든 팀 컴팩트 고정 크기 20.dp)
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = parseHexColor(match.homeTeamColor),
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.width(20.dp)
                                    ) {
                                        Text(
                                            text = match.homeTeamShortName,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }
                                    Text(text = ":", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 1.dp))
                                    Surface(
                                        color = parseHexColor(match.awayTeamColor),
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.width(20.dp)
                                    ) {
                                        Text(
                                            text = match.awayTeamShortName,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }
                                }
                                // 세트 점수
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${match.homePoint}", color = homeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = " : ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${match.awayPoint}", color = awayColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                // 1. 리그 필터 (전체 -> 여자부 -> 남자부 순서)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedLeague == "ALL",
                        onClick = { viewModel.updateLeagueFilter("ALL") },
                        label = { Text("전체") }
                    )
                    FilterChip(
                        selected = selectedLeague == "WOMEN",
                        onClick = { viewModel.updateLeagueFilter("WOMEN") },
                        label = { Text("여자부") }
                    )
                    FilterChip(
                        selected = selectedLeague == "MEN",
                        onClick = { viewModel.updateLeagueFilter("MEN") },
                        label = { Text("남자부") }
                    )
                }
            }

            item {
                // 2. 팀별 필터 (정렬된 2줄 8컬럼 동일 크기 격자)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1줄 (상단): [전체] + 여자부 7개 팀
                    if (selectedLeague == "ALL" || selectedLeague == "WOMEN") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 0: 전체 팀 선택 버튼
                            TeamChip(
                                name = "전체",
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                isSelected = selectedTeams.isEmpty(),
                                onClick = { viewModel.clearTeamFilter() },
                                modifier = Modifier.weight(1f)
                            )

                            // Column 1~7: 여자부 팀 7개
                            womenTeams.forEach { team ->
                                val isSelected = selectedTeams.contains(team.name)
                                TeamChip(
                                    name = team.name,
                                    backgroundColor = parseHexColor(team.colorHex),
                                    isSelected = isSelected,
                                    onClick = { viewModel.toggleTeamFilter(team.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 2줄 (하단): [정렬용 빈칸/전체] + 남자부 7개 팀
                    if (selectedLeague == "ALL" || selectedLeague == "MEN") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedLeague == "ALL") {
                                // Column 0: 빈 공간 (상단 [전체] 버튼과 세로 정렬 축 맞춤)
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                // 남자부 필터만 선택 시 Column 0에 [전체] 버튼 배치
                                TeamChip(
                                    name = "전체",
                                    backgroundColor = MaterialTheme.colorScheme.primary,
                                    isSelected = selectedTeams.isEmpty(),
                                    onClick = { viewModel.clearTeamFilter() },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Column 1~7: 남자부 팀 7개
                            menTeams.forEach { team ->
                                val isSelected = selectedTeams.contains(team.name)
                                TeamChip(
                                    name = team.name,
                                    backgroundColor = parseHexColor(team.colorHex),
                                    isSelected = isSelected,
                                    onClick = { viewModel.toggleTeamFilter(team.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 필터링된 스케줄 리스트
            if (filteredSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "이 날은 예정된 경기가 없습니다.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredSchedules) { schedule ->
                    val isScheduled = scheduledAlarms.contains(schedule.gnum)
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ScheduleItem(
                            schedule = schedule,
                            isScheduled = isScheduled,
                            onAlarmClick = {
                                viewModel.toggleAlarm(schedule, context, 1)
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "화면을 아래로 스와이프하여 데이터 업데이트가 가능합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamChip(
    name: String,
    backgroundColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightBg = backgroundColor.luminance() > 0.55f
    val contentColor = if (isLightBg) Color(0xFF1C1B1F) else Color.White
    val strokeColor = if (isLightBg) Color(0xFF1C1B1F) else Color.White

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected) BorderStroke(2.5.dp, strokeColor) else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = modifier
            .height(30.dp)
            .zIndex(if (isSelected) 1f else 0f)
            .graphicsLayer {
                if (isSelected) {
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text = name,
                    color = contentColor,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule, 
    isScheduled: Boolean,
    onAlarmClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 시간 및 장소
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.time, 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = schedule.place, 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 알림 버튼 (종료되지 않은 경기만)
                if (!schedule.isFinished) {
                    val canSchedule = schedule.canScheduleAlarm(1)
                    if (isScheduled) {
                        Button(
                            onClick = onAlarmClick,
                            enabled = canSchedule,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("예약 완료 ✓", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onAlarmClick,
                            enabled = canSchedule,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(if (canSchedule) "알림 예약" else "예약 불가", fontSize = 12.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 풀 네임 팀명 (2줄) 및 점수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 홈팀 (윗줄: 도시/기업명, 아랫줄: 팀명)
                val homeColor = if (schedule.isFinished) {
                    if (schedule.homePoint > schedule.awayPoint) {
                        if (schedule.isMen) MenWinColor else WomenWinColor
                    } else {
                        if (schedule.isMen) MenLoseColor else WomenLoseColor
                    }
                } else MaterialTheme.colorScheme.onSurface

                val (homeLine1, homeLine2) = formatTeamFullName(schedule.homeTeamName)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = homeLine1,
                        color = homeColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = homeLine2,
                        color = homeColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (schedule.isFinished && schedule.homePoint > schedule.awayPoint) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.End
                    )
                }

                // 점수 또는 'vs'
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(CircleShape)
                        .background(if (schedule.isFinished) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (schedule.isFinished) {
                        Text(
                            text = "${schedule.homePoint} : ${schedule.awayPoint}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Text(
                            text = "vs",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // 어웨이팀 (윗줄: 도시/기업명, 아랫줄: 팀명)
                val awayColor = if (schedule.isFinished) {
                    if (schedule.awayPoint > schedule.homePoint) {
                        if (schedule.isMen) MenWinColor else WomenWinColor
                    } else {
                        if (schedule.isMen) MenLoseColor else WomenLoseColor
                    }
                } else MaterialTheme.colorScheme.onSurface

                val (awayLine1, awayLine2) = formatTeamFullName(schedule.awayTeamName)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = awayLine1,
                        color = awayColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = awayLine2,
                        color = awayColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (schedule.isFinished && schedule.awayPoint > schedule.homePoint) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

private fun formatTeamFullName(fullName: String): Pair<String, String> {
    val cleanName = fullName
        .replace("프로배구단", "")
        .replace("배구단", "")
        .trim()

    return when {
        cleanName.contains("대한항공") -> Pair("인천 대한항공", "점보스")
        cleanName.contains("현대캐피탈") -> Pair("천안 현대캐피탈", "스카이워커스")
        cleanName.contains("우리카드") || cleanName.contains("우리WON") -> Pair("서울 우리카드", "우리WON")
        cleanName.contains("KB") -> Pair("의정부 KB손해보험", "스타즈")
        cleanName.contains("한국전력") -> Pair("수원 한국전력", "VIXTORM")
        cleanName.contains("OK") -> Pair("OK저축은행", "읏맨")
        cleanName.contains("삼성화재") -> Pair("대전 삼성화재", "블루팡스")
        cleanName.contains("흥국생명") -> Pair("인천 흥국생명", "핑크스파이더스")
        cleanName.contains("IBK") -> Pair("화성 IBK기업은행", "알토스")
        cleanName.contains("정관장") -> Pair("대전 정관장", "레드스파크스")
        cleanName.contains("SOOP") -> Pair("전남광주 SOOP", "SOOPers")
        cleanName.contains("도로공사") || cleanName.contains("하이패스") -> Pair("김천 한국도로공사", "하이패스")
        cleanName.contains("GS") -> Pair("GS칼텍스", "서울Kixx")
        cleanName.contains("현대건설") -> Pair("수원 현대건설", "힐스테이트")
        else -> {
            val parts = cleanName.split(" ")
            if (parts.size >= 2) {
                Pair(parts.dropLast(1).joinToString(" "), parts.last())
            } else {
                Pair(cleanName, "")
            }
        }
    }
}

private fun parseHexColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.DarkGray
    }
}
