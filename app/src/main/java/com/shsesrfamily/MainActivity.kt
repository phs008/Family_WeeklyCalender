package com.shsesrfamily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        enableEdgeToEdge()
        setContent {
            Family_WeeklyCalenderTheme {
                var showAddDialog by remember { mutableStateOf(false) }
                var selectedDay by remember { mutableStateOf("월") }
                var selectedTime by remember { mutableStateOf("9:00") }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("주간 일정표") },
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
                    WeeklyScheduleScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        onAddSchedule = { day, time ->
                            selectedDay = day
                            selectedTime = time
                            showAddDialog = true
                        }
                    )
                    
                    // 일정 추가 다이얼로그
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
fun WeeklyScheduleScreen(viewModel: ScheduleViewModel, modifier: Modifier = Modifier , onAddSchedule: (String, String) -> Unit) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val timeSlots = (9..23).flatMap { hour -> 
        listOf("$hour:00", "$hour:30") 
    } + "24:00"
    
    Column(modifier = modifier.fillMaxSize()) {
        // 요일 헤더
        Row(modifier = Modifier.fillMaxWidth()) {
            // 시간 열 헤더
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .border(0.5.dp, Color.LightGray)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("시간")
            }
            
            // 요일 헤더
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .border(0.5.dp, Color.LightGray)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 시간표 그리드
        LazyColumn {
            items(timeSlots.size) { index ->
                val time = timeSlots[index]
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 시간 표시
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .border(0.5.dp, Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontSize = 12.sp
                        )
                    }
                    
                    // 각 요일별 셀
                    daysOfWeek.forEach { day ->
                        val cellSchedules = schedules.filter { 
                            it.day == day && (it.startTime == time || 
                                             (isTimeBetween(time, it.startTime, it.endTime)))
                        }
                        
                        DayTimeCell(
                            day = day,
                            time = time,
                            schedules = cellSchedules,
                            onAddClick = { 
                                onAddSchedule(day, time)
                            },
                            onScheduleClick = { schedule ->
                                selectedSchedule = schedule
                                showEditDialog = true
                            },
                            onDragEnd = { schedule, newDay, newTime ->
                                // 드래그 앤 드롭으로 일정 이동 처리
                                val updatedSchedule = schedule.copy(day = newDay, startTime = newTime)
                                viewModel.updateSchedule(updatedSchedule)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 일정 수정 다이얼로그
    if (showEditDialog && selectedSchedule != null) {
        ScheduleDialog(
            schedule = selectedSchedule,
            initialDay = selectedSchedule?.day ?: "월",
            initialStartTime = selectedSchedule?.startTime ?: "9:00",
            onDismiss = { 
                showEditDialog = false
                selectedSchedule = null
            },
            onSave = { schedule ->
                viewModel.updateSchedule(schedule)
                showEditDialog = false
                selectedSchedule = null
            },
            onDelete = { schedule ->
                viewModel.deleteSchedule(schedule)
                showEditDialog = false
                selectedSchedule = null
            }
        )
    }
}


// 시간이 시작 시간과 종료 시간 사이에 있는지 확인하는 함수
fun isTimeBetween(time: String, startTime: String, endTime: String): Boolean {
    if (startTime.isBlank() || endTime.isBlank()) return false
    
    val currentMinutes = timeToMinutes(time)
    val startMinutes = timeToMinutes(startTime)
    val endMinutes = timeToMinutes(endTime)
    
    return currentMinutes in startMinutes until endMinutes
}


// 새로운 DayTimeCell 컴포넌트 (요일과 시간별 셀)
@Composable
fun RowScope.DayTimeCell(
    day: String,
    time: String,
    schedules: List<Schedule>,
    onAddClick: () -> Unit,
    onScheduleClick: (Schedule) -> Unit,
    onDragEnd: (Schedule, String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .border(0.5.dp, Color.LightGray)
            .clickable(enabled = schedules.isEmpty()) { onAddClick() }
    ) {
        if (schedules.isNotEmpty()) {
            // 여러 개의 TimeCell을 표시
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
            ) {
                items(schedules) { schedule ->
                    TimeCell(
                        schedule = schedule,
                        onClick = { onScheduleClick(schedule) },
                        onDragEnd = { newDay, newTime -> onDragEnd(schedule, newDay, newTime) }
                    )
                    
                    // 마지막 항목이 아니면 구분선 추가
                    if (schedule != schedules.last()) {
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        } else {
            // 빈 셀을 클릭 가능하게 함
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onAddClick() }
            )
        }
    }
}




// 개별 TimeCell 컴포넌트 (각 일정 항목)
@Composable
fun TimeCell(
    schedule: Schedule,
    onClick: () -> Unit,
    onDragEnd: (String, String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val color = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 2.dp)
            .background(color.copy(alpha = 0.7f))
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        onDragEnd(schedule.day, schedule.startTime)
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .padding(2.dp)
    ) {
        Column {
            Text(
                text = schedule.subject,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${schedule.startTime} - ${schedule.endTime}",
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
    
    // 30분 단위로 시간 슬롯 생성 (9:00 ~ 24:00)
    val timeSlots = (9..24).flatMap { hour -> 
        if (hour == 24) listOf("24:00") else listOf("$hour:00", "$hour:30") 
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // 스크롤 가능하도록 추가
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("제목") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                // 요일 선택
                Text("요일", modifier = Modifier.padding(top = 8.dp))
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(daysOfWeek.size) { index ->
                        val currentDay = daysOfWeek[index]
                        OutlinedButton(
                            onClick = { day = currentDay },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (day == currentDay) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(currentDay)
                        }
                    }
                }
                
                // 시작 시간 선택
                Text("시작 시간", modifier = Modifier.padding(top = 8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("시작 시간: $startTime")
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        timeSlots.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    startTime = time
                                    // 종료 시간이 시작 시간보다 이전이면 자동으로 30분 뒤로 설정
                                    if (!isValidTimeRange(startTime, endTime)) {
                                        endTime = calculateEndTime(startTime, 30)
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // 종료 시간 선택
                Text("종료 시간", modifier = Modifier.padding(top = 8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("종료 시간: $endTime")
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        // 시작 시간 이후의 시간만 표시
                        timeSlots.filter { time ->
                            isTimeAfter(time, startTime)
                        }.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    endTime = time
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // 색상 선택
                Text("색상", modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    val colors = listOf("#FF1976D2", "#FF4CAF50", "#FFF44336", "#FFFF9800", "#FF9C27B0")
                    colors.forEach { currentColor ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(currentColor)),
                                    shape = MaterialTheme.shapes.small
                                )
                                .border(
                                    width = if (color == currentColor) 2.dp else 0.dp,
                                    color = Color.Black,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { color = currentColor }
                        )
                    }
                }
                
                // 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    
                    if (!isNewSchedule && onDelete != null) {
                        TextButton(onClick = { schedule?.let { onDelete(it) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("삭제")
                        }
                    }
                    
                    Button(
                        onClick = {
                            val newSchedule = Schedule(
                                id = schedule?.id ?: UUID.randomUUID().toString(),
                                day = day,
                                startTime = startTime,
                                endTime = endTime,
                                subject = subject,
                                color = color
                            )
                            onSave(newSchedule)
                        },
                        enabled = subject.isNotBlank() && isValidTimeRange(startTime, endTime)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "저장")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("저장")
                    }
                }
            }
        }
    }
}

// 시작 시간에 분(minutes)을 더해서 종료 시간을 계산하는 함수
fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    val parts = startTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    val totalMinutes = hour * 60 + minute + durationMinutes
    val newHour = (totalMinutes / 60).coerceAtMost(24)
    val newMinute = if (totalMinutes % 60 == 0) 0 else 30 // 30분 단위로 조정
    
    return "${newHour.toString().padStart(2, '0')}:${newMinute.toString().padStart(2, '0')}"
}

// 시작 시간이 종료 시간보다 이전인지 확인하는 함수
fun isValidTimeRange(startTime: String, endTime: String): Boolean {
    if (startTime.isBlank() || endTime.isBlank()) return false
    return timeToMinutes(startTime) < timeToMinutes(endTime)
}

// 첫 번째 시간이 두 번째 시간보다 이후인지 확인하는 함수
fun isTimeAfter(time1: String, time2: String): Boolean {
    if (time1.isBlank() || time2.isBlank()) return false
    return timeToMinutes(time1) > timeToMinutes(time2)
}

// 시간 문자열을 분 단위로 변환하는 함수
fun timeToMinutes(timeStr: String): Int {
    if (timeStr.isBlank()) return 0
    val parts = timeStr.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}
