package com.example.todayvolleyball.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todayvolleyball.R
import com.example.todayvolleyball.domain.model.TeamRank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val selectedStage by viewModel.selectedStage.collectAsState()
    val teamRanks by viewModel.teamRanks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    val womenRanks = remember(teamRanks) { teamRanks.filter { !it.isMen } }
    val menRanks = remember(teamRanks) { teamRanks.filter { it.isMen } }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refreshData()
            Toast.makeText(context, "최신 순위 정보를 수동으로 동기화합니다.", Toast.LENGTH_SHORT).show()
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
                    // 최상단 "오늘의 배구" 로고 및 타이틀
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
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

                    // 우측 아이콘: [일정 아이콘] [순위 아이콘] [설정 아이콘]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "일정",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { /* Currently on standings */ }) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "순위",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "설정"
                            )
                        }
                    }
                }
            }

            item {
                // Stage selection tabs (정규, 준플옵, 플옵, 챔결)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val stages = listOf(
                        "201" to "정규",
                        "204" to "준플옵",
                        "202" to "플옵",
                        "203" to "챔결"
                    )
                    stages.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedStage == code,
                            onClick = { viewModel.selectStage(code) },
                            label = {
                                Text(
                                    text = label,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (selectedStage == code) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 1. 여자부 순위 테이블
            item {
                Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    StandingsTable(
                        title = "여자부",
                        ranks = womenRanks
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 2. 남자부 순위 테이블
            item {
                Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    StandingsTable(
                        title = "남자부",
                        ranks = menRanks
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp),
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
fun StandingsTable(
    title: String,
    ranks: List<TeamRank>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 15.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ranks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "등록된 순위 정보가 없습니다.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("순위", weight = 0.75f, isHeader = true)
                        TableCell("팀", weight = 1.05f, isHeader = true)
                        TableCell("승점", weight = 0.8f, isHeader = true)
                        TableCell("승", weight = 0.65f, isHeader = true)
                        TableCell("패", weight = 0.65f, isHeader = true)
                        TableCell("득세", weight = 0.8f, isHeader = true)
                        TableCell("실세", weight = 0.8f, isHeader = true)
                        TableCell("세트률", weight = 1.05f, isHeader = true)
                        TableCell("득점", weight = 0.85f, isHeader = true)
                        TableCell("실점", weight = 0.85f, isHeader = true)
                        TableCell("점수률", weight = 1.05f, isHeader = true)
                        TableCell("연속", weight = 0.85f, isHeader = true)
                    }

                    // Content Rows
                    ranks.forEachIndexed { index, item ->
                        val rowBg = if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        } else {
                            Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("${item.rank}", weight = 0.75f, isBold = true)
                            // Team Cell with team color badge
                            Row(
                                modifier = Modifier.weight(1.05f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = parseHexColor(item.teamColor),
                                    shape = RoundedCornerShape(2.dp),
                                    modifier = Modifier
                                        .size(6.dp)
                                        .padding(end = 1.dp)
                                ) {}
                                Text(
                                    text = item.teamShortName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val pointsStr = if (item.points % 1.0 == 0.0) item.points.toInt().toString() else item.points.toString()
                            TableCell(pointsStr, weight = 0.8f, isBold = true, color = MaterialTheme.colorScheme.primary)
                            TableCell("${item.wins}", weight = 0.65f)
                            TableCell("${item.losses}", weight = 0.65f)
                            TableCell("${item.setsWon}", weight = 0.8f)
                            TableCell("${item.setsLost}", weight = 0.8f)
                            TableCell(item.setRatio, weight = 1.05f)
                            TableCell("${item.scorePoints}", weight = 0.85f)
                            TableCell("${item.scoreConceded}", weight = 0.85f)
                            TableCell(item.scoreRatio, weight = 1.05f)
                            TableCell(item.streak, weight = 0.85f)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        textAlign = TextAlign.Center,
        fontSize = if (isHeader) 11.5.sp else 12.5.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Medium,
        color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else color,
        maxLines = 1
    )
}

private fun parseHexColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.DarkGray
    }
}

