package com.bibin.opnsense.domain.model

data class BlockSchedule(
    val id: Long = 0,
    val deviceMac: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int>, // Calendar.MONDAY..Calendar.SUNDAY
)
