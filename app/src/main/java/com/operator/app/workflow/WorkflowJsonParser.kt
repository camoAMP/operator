package com.operator.app.workflow

import org.json.JSONArray
import org.json.JSONObject

object WorkflowJsonParser {
    fun parse(json: String): WorkflowDefinition {
        val root = JSONObject(json)
        val workflowId = root.getString("workflowId")
        val name = root.optString("name", workflowId)
        val steps = parseSteps(root.getJSONArray("steps"))
        return WorkflowDefinition(workflowId = workflowId, name = name, steps = steps)
    }

    private fun parseSteps(array: JSONArray): List<WorkflowStep> {
        val steps = mutableListOf<WorkflowStep>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            steps += WorkflowStep(
                action = obj.getString("action"),
                resourceId = obj.optNullableString("resourceId"),
                value = obj.optNullableString("value"),
                artifactType = obj.optNullableString("artifactType"),
                extension = obj.optNullableString("extension"),
                packageName = obj.optNullableString("package")
            )
        }
        return steps
    }

    private fun JSONObject.optNullableString(key: String): String? {
        return if (has(key) && !isNull(key)) getString(key) else null
    }
}
