package com.example.data.models

import androidx.compose.ui.graphics.Color

data class WorkflowItem(
    val id: String,
    val title: String,
    val description: String,
    val agent: AgentType,
    val promptTemplate: String,
    val badgeColor: Color,
    val category: String
)

object WorkflowLibrary {
    val workflows = listOf(
        WorkflowItem(
            id = "wf_refactor",
            title = "Full-Stack Code Refactor",
            description = "Analyzes source code for architectural flaws, performance leaks, and Compose optimization.",
            agent = AgentType.AI_CODER,
            promptTemplate = "Perform a complete full-stack code audit and refactor for Jetpack Compose performance and clean architecture.",
            badgeColor = Color(0xFF10B981),
            category = "Coding"
        ),
        WorkflowItem(
            id = "wf_security",
            title = "API & Secret Security Audit",
            description = "Scans API endpoints, headers, and secret keys for potential vulnerabilities.",
            agent = AgentType.API_MANAGER,
            promptTemplate = "Audit API security for Gemini, Groq, GLM-4.6 and Supabase REST endpoints. Check authorization header security.",
            badgeColor = Color(0xFFEC4899),
            category = "Security"
        ),
        WorkflowItem(
            id = "wf_ocr",
            title = "Vision Document & OCR Scanner",
            description = "Extracts structured text, code, or tabular data from attached document images.",
            agent = AgentType.ACTION_MANAGER,
            promptTemplate = "Extract and parse all readable code, text, and data structures from the attached document/image.",
            badgeColor = Color(0xFF22D3EE),
            category = "Vision"
        ),
        WorkflowItem(
            id = "wf_sys_opt",
            title = "Automated System Optimization",
            description = "Executes memory cleanup, storage cache wipe, and network latency diagnostics.",
            agent = AgentType.ACTION_MANAGER,
            promptTemplate = "Run complete system memory optimization, storage cache analysis, and ping test across all LLM backends.",
            badgeColor = Color(0xFFF59E0B),
            category = "System"
        ),
        WorkflowItem(
            id = "wf_edge_deploy",
            title = "Supabase Edge Function Deployer",
            description = "Generates and validates Node.js Edge Handler code for Supabase deployment.",
            agent = AgentType.AI_CODER,
            promptTemplate = "Write a complete Supabase Edge Function in TypeScript for multi-agent prompt delegation with CORS headers.",
            badgeColor = Color(0xFF8B5CF6),
            category = "Deployment"
        ),
        WorkflowItem(
            id = "wf_python_engine",
            title = "Python Subprocess Task Engine",
            description = "Creates Python subprocess scripts for background telemetry and file processing.",
            agent = AgentType.AI_CODER,
            promptTemplate = "Generate a Python script using subprocess and asyncio to run background system telemetry checks.",
            badgeColor = Color(0xFF3B82F6),
            category = "Automation"
        )
    )
}
