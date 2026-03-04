package com.shsesrfamily.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val day: String = "", // 요일
    val startTime: String = "", // 시작시간
    val endTime: String = "", // 종료시간
    val subject: String = "", // 제목
    val color: String = "#FF1976D2" // 화면에 보여질 색상 (기본값: 파란색)
)