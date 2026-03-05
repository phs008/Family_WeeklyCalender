package com.shsesrfamily.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shsesrfamily.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    init {
        signInAnonymously()
    }

    private fun signInAnonymously(){
        if(auth.currentUser == null){
            auth.signInAnonymously()
                .addOnSuccessListener {
                    fetchSchedules()
                }
                .addOnFailureListener {
                    android.util.Log.e("AuthError", "익명 로그인 실패")
                }
        }else{
            fetchSchedules()
        }
    }

    private fun fetchSchedules() {
        db.collection("schedules")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("FirestoreError", "데이터 가져오기 실패", e)
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
                android.util.Log.e("FirestoreError", "추가 실패", it)
            }
    }

    fun updateSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id)
            .set(schedule)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                android.util.Log.e("FirestoreError", "수정 실패", it)
            }
    }

    fun deleteSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id)
            .delete()
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                android.util.Log.e("FirestoreError", "삭제 실패", it)
            }
    }
}