package com.operator.app.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WorkflowQueue(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun enqueue(request: WorkflowRequest) {
        val current = load().toMutableList()
        current.add(request)
        save(current)
    }

    fun drain(): List<WorkflowRequest> {
        val current = load()
        prefs.edit().remove(KEY_QUEUE).apply()
        return current
    }

    private fun load(): List<WorkflowRequest> {
        val raw = prefs.getString(KEY_QUEUE, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            val items = mutableListOf<WorkflowRequest>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                items += WorkflowRequest(
                    projectId = obj.getString("projectId"),
                    workflowId = obj.getString("workflowId")
                )
            }
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun save(items: List<WorkflowRequest>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("projectId", item.projectId)
            obj.put("workflowId", item.workflowId)
            array.put(obj)
        }
        prefs.edit().putString(KEY_QUEUE, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "operator_workflow_queue"
        private const val KEY_QUEUE = "queue"
    }
}

data class WorkflowRequest(
    val projectId: String,
    val workflowId: String
)
