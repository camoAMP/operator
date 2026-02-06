package com.operator.app.workflow

import android.content.Context
import java.io.BufferedReader

class WorkflowRepository(private val context: Context) {
    fun loadFromAssets(workflowId: String): WorkflowDefinition {
        val path = "workflows/$workflowId.json"
        val json = context.assets.open(path).bufferedReader().use(BufferedReader::readText)
        return WorkflowJsonParser.parse(json)
    }
}
