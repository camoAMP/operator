package com.operator.app.storage

import android.content.Context
import java.io.File

class ArtifactStore(private val context: Context) {
    fun writeText(relativePath: String, content: String): File {
        val file = File(context.filesDir, relativePath)
        file.parentFile?.mkdirs()
        file.writeText(content)
        return file
    }
}
