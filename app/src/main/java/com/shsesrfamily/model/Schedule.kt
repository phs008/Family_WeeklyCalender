package com.shsesrfamily.model

import androidx.compose.ui.graphics.Color

data class Schedule(
    val id: String = "",
    val day: String = "",
    val time: String = "",
    val person: String = "", // 이름
    val subject: String = "", // 제목
    val duration: Int = 30, // 시간(분 단위)
    val color: String = "#FF1976D2" // 화면에 보여질 색상 (기본값: 파란색)
)