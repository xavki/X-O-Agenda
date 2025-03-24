package com.institutmarianao.xo_agenda.utils

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.institutmarianao.xo_agenda.models.Task

object FirestoreHelper {
    private val db = Firebase.firestore

    fun addTask(task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getTasks(userId: String, onResult: (List<Task>) -> Unit) {
        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val tasks = snapshot.map { it.toObject(Task::class.java).copy(id = it.id) }
                onResult(tasks)
            }
    }
}
