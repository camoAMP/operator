package com.operator.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.operator.app.R
import com.operator.app.accessibility.OperatorService
import com.operator.app.storage.WorkflowRequest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.openSettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.runSample).setOnClickListener {
            val service = OperatorService.instance
            if (service == null) {
                Toast.makeText(this, "Enable the Operator accessibility service first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            service.runWorkflow(
                WorkflowRequest(
                    projectId = "default",
                    workflowId = "chatgpt_haiku"
                )
            )
            Toast.makeText(this, "Workflow queued.", Toast.LENGTH_SHORT).show()
        }
    }
}
