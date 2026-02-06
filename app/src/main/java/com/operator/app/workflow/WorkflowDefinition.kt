package com.operator.app.workflow

data class WorkflowDefinition(
    val workflowId: String,
    val name: String,
    val steps: List<WorkflowStep>
)
