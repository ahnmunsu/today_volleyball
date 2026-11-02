package com.example.todayvolleyball.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todayvolleyball.data.local.ScheduleChangeEntity

@Composable
fun ChangeLogDialog(
    changes: List<ScheduleChangeEntity>,
    onDismiss: () -> Unit
) {
    if (changes.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "일정 변경 알림") },
            text = {
                LazyColumn {
                    items(changes) { change ->
                        Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                            Text(text = change.message)
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("확인")
                }
            }
        )
    }
}
