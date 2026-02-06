package com.operator.app.workflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.json.JSONException

class WorkflowJsonParserTest {
    @Test
    fun parseUsesWorkflowIdWhenNameMissingAndOptionalFieldsOmitted() {
        val json = """
            {
              "workflowId": "wf_missing_name",
              "steps": [
                { "action": "CLICK" }
              ]
            }
        """.trimIndent()

        val definition = WorkflowJsonParser.parse(json)

        assertEquals("wf_missing_name", definition.workflowId)
        assertEquals("wf_missing_name", definition.name)
        assertEquals(1, definition.steps.size)

        val step = definition.steps[0]
        assertEquals("CLICK", step.action)
        assertNull(step.resourceId)
        assertNull(step.value)
        assertNull(step.artifactType)
        assertNull(step.extension)
        assertNull(step.packageName)
    }

    @Test
    fun parseTreatsExplicitNullsAsNullAndMapsPackageName() {
        val json = """
            {
              "workflowId": "wf_nulls",
              "name": "Null Handling",
              "steps": [
                {
                  "action": "OPEN_APP",
                  "package": "com.example.app",
                  "resourceId": null,
                  "value": null,
                  "artifactType": null,
                  "extension": null
                }
              ]
            }
        """.trimIndent()

        val definition = WorkflowJsonParser.parse(json)

        assertEquals("wf_nulls", definition.workflowId)
        assertEquals("Null Handling", definition.name)
        assertEquals(1, definition.steps.size)

        val step = definition.steps[0]
        assertEquals("OPEN_APP", step.action)
        assertEquals("com.example.app", step.packageName)
        assertNull(step.resourceId)
        assertNull(step.value)
        assertNull(step.artifactType)
        assertNull(step.extension)
    }

    @Test(expected = JSONException::class)
    fun parseThrowsOnMalformedJson() {
        val json = """
            {
              "workflowId": "wf_bad",
              "steps": [
                { "action": "CLICK" }
              ]
            
        """.trimIndent()

        WorkflowJsonParser.parse(json)
    }

    @Test(expected = JSONException::class)
    fun parseThrowsWhenStepsMissing() {
        val json = """
            {
              "workflowId": "wf_no_steps"
            }
        """.trimIndent()

        WorkflowJsonParser.parse(json)
    }

    @Test(expected = JSONException::class)
    fun parseThrowsWhenStepsNotArray() {
        val json = """
            {
              "workflowId": "wf_steps_not_array",
              "steps": { "action": "CLICK" }
            }
        """.trimIndent()

        WorkflowJsonParser.parse(json)
    }

    @Test
    fun parseAllowsEmptyStepsArray() {
        val json = """
            {
              "workflowId": "wf_empty_steps",
              "steps": []
            }
        """.trimIndent()

        val definition = WorkflowJsonParser.parse(json)

        assertEquals("wf_empty_steps", definition.workflowId)
        assertEquals("wf_empty_steps", definition.name)
        assertEquals(0, definition.steps.size)
    }
}
