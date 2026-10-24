package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConsoleLogEntry
import com.example.data.model.LogLevel
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsoleDrawer(
    logs: List<ConsoleLogEntry>,
    onClearLogs: () -> Unit,
    onClose: () -> Unit,
    onFixWithAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }

    val filteredLogs = remember(logs, filterLevel) {
        if (filterLevel == null) logs else logs.filter { it.level == filterLevel }
    }

    val errorCount = remember(logs) { logs.count { it.level == LogLevel.ERROR } }
    val warnCount = remember(logs) { logs.count { it.level == LogLevel.WARN } }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp),
        color = Color(0xFF161B22),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONSOLE LOGS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Filter chips
                    FilterChip("ALL", selected = filterLevel == null) { filterLevel = null }
                    FilterChip(
                        "ERRORS ($errorCount)",
                        selected = filterLevel == LogLevel.ERROR,
                        color = NeonRed
                    ) { filterLevel = LogLevel.ERROR }
                    FilterChip(
                        "WARNS ($warnCount)",
                        selected = filterLevel == LogLevel.WARN,
                        color = NeonAmber
                    ) { filterLevel = LogLevel.WARN }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp).testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("close_console_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close console",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Logs stream
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No console output yet. Run your code or interact with the preview.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { entry ->
                        ConsoleLogItem(entry = entry, onFixWithAi = onFixWithAi)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleLogItem(
    entry: ConsoleLogEntry,
    onFixWithAi: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = remember(entry.timestamp) { timeFormat.format(Date(entry.timestamp)) }

    val (badgeColor, label) = when (entry.level) {
        LogLevel.ERROR -> NeonRed to "ERR"
        LogLevel.WARN -> NeonAmber to "WARN"
        LogLevel.INFO -> CyanBrand to "INFO"
        LogLevel.LOG -> Color(0xFF8B949E) to "LOG"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (entry.level == LogLevel.ERROR) NeonRed.copy(alpha = 0.08f) else Color(0xFF0D1117))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Text(
                text = timeStr,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )

            Text(
                text = entry.message,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (entry.level == LogLevel.ERROR) NeonRed else TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // If Error, show Fix with AI button
        if (entry.level == LogLevel.ERROR) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanBrand.copy(alpha = 0.15f))
                    .border(1.dp, CyanBrand.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .clickable { onFixWithAi(entry.message) }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = CyanBrand,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Fix with AI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBrand
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    color: Color = CyanBrand,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) color else BorderDark,
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else TextMuted
        )
    }
}
