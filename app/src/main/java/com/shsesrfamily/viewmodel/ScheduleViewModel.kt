package com.shsesrfamily.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.shsesrfamily.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    init {
        fetchSchedules()
    }

    private fun fetchSchedules() {
        db.collection("schedules")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val scheduleList = snapshot?.map { document ->
                    document.toObject(Schedule::class.java).copy(id = document.id)
                } ?: emptyList()

                _schedules.value = scheduleList
            }
    }

    fun addSchedule(schedule: Schedule) {
        db.collection("schedules")
            .add(schedule)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun updateSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id)
            .set(schedule)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun deleteSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id)
            .delete()
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}