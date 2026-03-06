package com.shsesrfamily

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shsesrfamily.model.Schedule
import com.shsesrfamily.ui.theme.Family_WeeklyCalenderTheme
import com.shsesrfamily.viewmodel.ScheduleViewModel
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: ScheduleViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var isDarkMode by remember { 
                mutableStateOf(sharedPref.getBoolean("is_dark_mode", false)) 
            }
            var isAxisSwapped by remember { 
                mutableStateOf(sharedPref.getBoolean("is_axis_swapped", false)) 
            }

            Family_WeeklyCalenderTheme(darkTheme = isDarkMode) {
                var showAddDialog by remember { mutableStateOf(false) }
                var selectedDay by remember { mutableStateOf("월") }
                var selectedTime by remember { mutableStateOf("9:00") }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("주간 일정표") },
                            actions = {
                                IconButton(onClick = { 
                                    isAxisSwapped = !isAxisSwapped 
                                    sharedPref.edit().putBoolean("is_axis_swapped", isAxisSwapped).apply()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "축 전환"
                                    )
                                }
                                IconButton(onClick = { 
                                    isDarkMode = !isDarkMode 
                                    sharedPref.edit().putBoolean("is_dark_mode", isDarkMode).apply()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "테마 전환"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "일정 추가")
                        }
                    }
                ) { innerPadding ->
                    if (isAxisSwapped) {
                        HorizontalScheduleScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onAddSchedule = { day, time ->
                                selectedDay = day
                                selectedTime = time
                                showAddDialog = true
                            }
                        )
                    } else {
                        WeeklyScheduleScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onAddSchedule = { day, time ->
                                selectedDay = day
                                selectedTime = time
                                showAddDialog = true
                            }
                        )
                    }
                    
                    if (showAddDialog) {
                        ScheduleDialog(
                            schedule = null,
                            initialDay = selectedDay,
                            initialStartTime = selectedTime,
                            onDismiss = { showAddDialog = false },
                            onSave = { schedule ->
                                viewModel.addSchedule(schedule)
                                showAddDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyScheduleScreen(
    viewModel: ScheduleViewModel, 
    modifier: Modifier = Modifier, 
    onAddSchedule: (String, String) -> Unit
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val slotHeight = 60.dp
    val timeSlots = (9..23).flatMap { hour -> listOf("$hour:00", "$hour:30") }
    
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("시간", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                daysOfWeek.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.width(60.dp)) {
                timeSlots.forEach { time ->
                    Box(
                        modifier = Modifier.height(slotHeight).fillMaxWidth().border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(text = time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Box(modifier = Modifier.height(20.dp).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Text(text = "24:00", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            
            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                daysOfWeek.forEach { day ->
                    DayColumn(
                        day = day,
                        daySchedules = schedules.filter { it.day == day },
                        timeSlots = timeSlots,
                        slotHeight = slotHeight,
                        columnWidth = 100.dp,
                        onAddClick = onAddSchedule,
                        onScheduleClick = { 
                            selectedSchedule = it
                            showEditDialog = true
                        },
                        onDragEnd = { schedule, newDay, newTime ->
                            viewModel.updateSchedule(schedule.copy(day = newDay, startTime = newTime))
                        }
                    )
                }
            }
        }
    }
    
    if (showEditDialog && selectedSchedule != null) {
        ScheduleDialog(
            schedule = selectedSchedule,
            initialDay = selectedSchedule?.day ?: "월",
            initialStartTime = selectedSchedule?.startTime ?: "9:00",
            onDismiss = { showEditDialog = false; selectedSchedule = null },
            onSave = { viewModel.updateSchedule(it); showEditDialog = false; selectedSchedule = null },
            onDelete = { viewModel.deleteSchedule(it); showEditDialog = false; selectedSchedule = null }
        )
    }
}

@Composable
fun HorizontalScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
    onAddSchedule: (String, String) -> Unit
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val timeSlots = (9..23).flatMap { hour -> listOf("$hour:00", "$hour:30") }
    val slotWidth = 100.dp
    val rowHeight = 80.dp
    
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("요일", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                timeSlots.forEach { time ->
                    Box(
                        modifier = Modifier
                            .width(slotWidth)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        
        Column(modifier = Modifier.fillMaxSize().verticalScroll(verticalScrollState)) {
            daysOfWeek.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().horizontalScroll(horizontalScrollState)) {
                        Row {
                            timeSlots.forEach { time ->
                                Box(
                                    modifier = Modifier
                                        .width(slotWidth)
                                        .fillMaxHeight()
                                        .border(0.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        .clickable { onAddSchedule(day, time) }
                                )
                            }
                        }
                        
                        val daySchedules = schedules.filter { it.day == day }
                        val positions = calculateSchedulePositions(daySchedules)
                        
                        daySchedules.forEach { schedule ->
                            val pos = positions[schedule.id] ?: SchedulePos(0, 1)
                            val startMin = timeToMinutes(schedule.startTime)
                            val endMin = timeToMinutes(schedule.endTime)
                            val baseMin = timeToMinutes("9:00")
                            
                            val leftOffset = ((startMin - baseMin) / 30f * slotWidth.value).dp
                            val itemWidth = ((endMin - startMin) / 30f * slotWidth.value).dp
                            
                            val itemHeight = (rowHeight.value / pos.totalColumns).dp
                            val yOffset = itemHeight * pos.columnIndex
                            
                            Box(
                                modifier = Modifier
                                    .offset(x = leftOffset, y = yOffset)
                                    .width(itemWidth)
                                    .height(itemHeight)
                                    .padding(2.dp)
                            ) {
                                TimeCell(
                                    schedule = schedule,
                                    onClick = { 
                                        selectedSchedule = schedule
                                        showEditDialog = true
                                    },
                                    onDragEnd = { _, _ -> }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showEditDialog && selectedSchedule != null) {
        ScheduleDialog(
            schedule = selectedSchedule,
            initialDay = selectedSchedule?.day ?: "월",
            initialStartTime = selectedSchedule?.startTime ?: "9:00",
            onDismiss = { showEditDialog = false; selectedSchedule = null },
            onSave = { viewModel.updateSchedule(it); showEditDialog = false; selectedSchedule = null },
            onDelete = { viewModel.deleteSchedule(it); showEditDialog = false; selectedSchedule = null }
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DayColumn(
    day: String,
    daySchedules: List<Schedule>,
    timeSlots: List<String>,
    slotHeight: Dp,
    columnWidth: Dp,
    onAddClick: (String, String) -> Unit,
    onScheduleClick: (Schedule) -> Unit,
    onDragEnd: (Schedule, String, String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier
        .width(columnWidth)
        .height(slotHeight * timeSlots.size + 20.dp)
        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        val totalWidth = maxWidth
        Column {
            timeSlots.forEach { time ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slotHeight)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .clickable { onAddClick(day, time) }
                )
            }
        }
        
        val positions = calculateSchedulePositions(daySchedules)
        daySchedules.forEach { schedule ->
            val pos = positions[schedule.id] ?: SchedulePos(0, 1)
            val startMin = timeToMinutes(schedule.startTime)
            val endMin = timeToMinutes(schedule.endTime)
            val baseMin = timeToMinutes("9:00")
            
            val topOffset = ((startMin - baseMin) / 30f * slotHeight.value).dp
            val itemHeight = ((endMin - startMin) / 30f * slotHeight.value).dp
            val itemWidth = totalWidth / pos.totalColumns
            val xOffset = itemWidth * pos.columnIndex

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = topOffset)
                    .width(itemWidth)
                    .height(itemHeight)
                    .padding(0.5.dp)
            ) {
                TimeCell(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) },
                    onDragEnd = { newDay, newTime -> onDragEnd(schedule, newDay, newTime) }
                )
            }
        }
    }
}

data class SchedulePos(val columnIndex: Int, val totalColumns: Int)

fun calculateSchedulePositions(schedules: List<Schedule>): Map<String, SchedulePos> {
    if (schedules.isEmpty()) return emptyMap()
    val result = mutableMapOf<String, SchedulePos>()
    val groups = mutableListOf<MutableList<Schedule>>()
    for (schedule in schedules.sortedBy { timeToMinutes(it.startTime) }) {
        var added = false
        for (group in groups) {
            if (group.any { overlaps(it, schedule) }) {
                group.add(schedule)
                added = true
                break
            }
        }
        if (!added) groups.add(mutableListOf(schedule))
    }
    for (group in groups) {
        val columns = mutableListOf<MutableList<Schedule>>()
        val scheduleToCol = mutableMapOf<String, Int>()
        for (s in group.sortedBy { timeToMinutes(it.startTime) }) {
            var colIndex = -1
            for (i in columns.indices) {
                if (columns[i].none { overlaps(it, s) }) {
                    colIndex = i
                    break
                }
            }
            if (colIndex == -1) {
                columns.add(mutableListOf(s))
                colIndex = columns.size - 1
            } else {
                columns[colIndex].add(s)
            }
            scheduleToCol[s.id] = colIndex
        }
        for (s in group) {
            result[s.id] = SchedulePos(scheduleToCol[s.id]!!, columns.size)
        }
    }
    return result
}

fun overlaps(s1: Schedule, s2: Schedule): Boolean {
    val start1 = timeToMinutes(s1.startTime); val end1 = timeToMinutes(s1.endTime)
    val start2 = timeToMinutes(s2.startTime); val end2 = timeToMinutes(s2.endTime)
    return maxOf(start1, start2) < minOf(end1, end2)
}

@Composable
fun TimeCell(
    schedule: Schedule, 
    onClick: () -> Unit, 
    onDragEnd: (String, String) -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(schedule.color)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.8f), shape = MaterialTheme.shapes.extraSmall)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = schedule.subject, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 11.sp, color = Color.White)
    }
}

@Composable
fun ScheduleDialog(
    schedule: Schedule?,
    initialDay: String = "월",
    initialStartTime: String = "9:00",
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit,
    onDelete: ((Schedule) -> Unit)? = null
) {
    val isNewSchedule = schedule == null
    val title = if (isNewSchedule) "일정 추가" else "일정 수정"
    var subject by remember { mutableStateOf(schedule?.subject ?: "") }
    var day by remember { mutableStateOf(schedule?.day ?: initialDay) }
    var startTime by remember { mutableStateOf(schedule?.startTime ?: initialStartTime) }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: calculateEndTime(initialStartTime, 30)) }
    var color by remember { mutableStateOf(schedule?.color ?: "#FF1976D2") }
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val timeSlots = (9..24).flatMap { hour -> if (hour == 24) listOf("24:00") else listOf("$hour:00", "$hour:30") }
    val scheduleColors = listOf("#FF1976D2", "#FF4CAF50", "#FFF44336", "#FFFF9800", "#FF9C27B0", "#FF00BCD4", "#FF009688", "#FF8BC34A", "#FFCDDC39", "#FFFFEB3B", "#FFFFC107", "#FFFF5722", "#FF795548", "#FF9E9E9E", "#FF607D8B", "#FFF06292", "#FFBA68C8", "#FF9575CD", "#FF7986CB", "#FF64B5F6")

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                Text("요일", modifier = Modifier.padding(top = 8.dp))
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(daysOfWeek) { currentDay ->
                        OutlinedButton(onClick = { day = currentDay }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if (day == currentDay) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface), modifier = Modifier.padding(end = 4.dp)) { Text(currentDay) }
                    }
                }
                Text("시작 시간", modifier = Modifier.padding(top = 8.dp)); Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("시작 시간: $startTime") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        timeSlots.forEach { time -> DropdownMenuItem(text = { Text(time) }, onClick = { startTime = time; if (!isValidTimeRange(startTime, endTime)) { endTime = calculateEndTime(startTime, 30) }; expanded = false }) }
                    }
                }
                Text("종료 시간", modifier = Modifier.padding(top = 8.dp)); Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("종료 시간: $endTime") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        timeSlots.filter { time -> isTimeAfter(time, startTime) }.forEach { time -> DropdownMenuItem(text = { Text(time) }, onClick = { endTime = time; expanded = false }) }
                    }
                }
                Text("색상 선택", modifier = Modifier.padding(top = 8.dp)); Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 40.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
                        items(scheduleColors) { currentColor ->
                            Box(modifier = Modifier.size(40.dp).padding(4.dp).background(Color(android.graphics.Color.parseColor(currentColor)), shape = MaterialTheme.shapes.small).border(width = if (color == currentColor) 2.dp else 0.dp, color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small).clickable { color = currentColor })
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    if (!isNewSchedule && onDelete != null) {
                        TextButton(onClick = { schedule?.let { onDelete(it) } }) { Icon(Icons.Default.Delete, contentDescription = "삭제"); Spacer(modifier = Modifier.width(4.dp)); Text("삭제") }
                    }
                    Button(onClick = { val newSchedule = Schedule(id = schedule?.id ?: UUID.randomUUID().toString(), day = day, startTime = startTime, endTime = endTime, subject = subject, color = color); onSave(newSchedule) }, enabled = subject.isNotBlank() && isValidTimeRange(startTime, endTime)) { Icon(Icons.Default.Edit, contentDescription = "저장"); Spacer(modifier = Modifier.width(4.dp)); Text("저장") }
                }
            }
        }
    }
}

fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    val parts = startTime.split(":"); val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9; val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val totalMinutes = hour * 60 + minute + durationMinutes; val newHour = (totalMinutes / 60).coerceAtMost(24); val newMinute = if (totalMinutes % 60 == 0) 0 else 30
    return "${newHour.toString().padStart(2, '0')}:${newMinute.toString().padStart(2, '0')}"
}
fun isValidTimeRange(startTime: String, endTime: String): Boolean = timeToMinutes(startTime) < timeToMinutes(endTime)
fun isTimeAfter(time1: String, time2: String): Boolean = timeToMinutes(time1) > timeToMinutes(time2)
fun timeToMinutes(timeStr: String): Int {
    val parts = timeStr.split(":"); val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0; val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}
