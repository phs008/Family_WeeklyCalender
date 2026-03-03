package com.shsesrfamily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shsesrfamily.model.Schedule
import com.shsesrfamily.ui.theme.Family_WeeklyCalenderTheme
import com.shsesrfamily.viewmodel.ScheduleViewModel
import java.util.*

class __MainActivity : ComponentActivity() {
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
    val timeSlots = (9..23).flatMap { hour ->
        listOf("$hour:00", "$hour:30")
    } + "24:00"

    val slotHeight = 60.dp // 30분당 높이
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // 요일 헤더
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .border(0.5.dp, Color.LightGray)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("시간", fontSize = 12.sp)
            }
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .border(0.5.dp, Color.LightGray)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 스크롤 가능한 시간표 본체
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // 시간 라벨 열
            Column(modifier = Modifier.width(60.dp)) {
                timeSlots.forEach { time ->
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(slotHeight)
                            .border(0.5.dp, Color.LightGray),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(text = time, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // 요일별 컨텐츠 영역
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(slotHeight * timeSlots.size)
                        .border(0.5.dp, Color.LightGray)
                ) {
                    // 배경 그리드 및 클릭 이벤트 레이어
                    Column {
                        timeSlots.forEach { time ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(slotHeight)
                                    .border(0.2.dp, Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { onAddSchedule(day, time) }
                            )
                        }
                    }

                    // 스케줄 아이템들을 시간에 따른 위치에 배치
                    val daySchedules = schedules.filter { it.day == day }
                    daySchedules.forEach { schedule ->
                        val startMin = timeToMinutes(schedule.startTime)
                        val endMin = timeToMinutes(schedule.endTime)
                        val baseMin = 9 * 60 // 9:00 시작 기준

                        if (startMin >= baseMin) {
                            val topOffset = (startMin - baseMin) / 30f * slotHeight.value
                            val durationHeight = (endMin - startMin) / 30f * slotHeight.value

                            if (durationHeight > 0) {
                                TimeCell(
                                    schedule = schedule,
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .offset(y = topOffset.dp)
                                        .height(durationHeight.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        selectedSchedule = schedule
                                        showEditDialog = true
                                    },
                                    onDragEnd = { newDay, newTime ->
                                        val updated =
                                            schedule.copy(day = newDay, startTime = newTime)
                                        viewModel.updateSchedule(updated)
                                    }
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
            onSave = { schedule ->
                viewModel.updateSchedule(schedule); showEditDialog = false; selectedSchedule = null
            },
            onDelete = { schedule ->
                viewModel.deleteSchedule(schedule); showEditDialog = false; selectedSchedule = null
            }
        )
    }
}

@Composable
fun TimeCell(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDragEnd: (String, String) -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.8f), shape = MaterialTheme.shapes.small)
            .border(0.5.dp, color, shape = MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = schedule.subject,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
            Text(
                text = "${schedule.startTime}-${schedule.endTime}",
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ... ScheduleDialog 및 유틸리티 함수 (calculateEndTime, timeToMinutes 등)는 기존과 동일하게 유지됩니다.