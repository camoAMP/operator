package com.operator.app.storage

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ArtifactNamer {
    fun generateRelativePath(
        projectId: String,
        workflowId: String,
        artifactType: String,
        extension: String
    ): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = formatter.format(Date())
        return "Operator/Projects/$projectId/$workflowId/${artifactType}_$timestamp.$extension"
    }
}
