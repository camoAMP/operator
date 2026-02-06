package com.operator.app.workflow

data class WorkflowStep(
    val action: String,
    val resourceId: String? = null,
    val value: String? = null,
    val artifactType: String? = null,
    val extension: String? = null,
    val packageName: String? = null
)
