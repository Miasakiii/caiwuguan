package com.caiwuguan.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 简单的 Markdown 渲染组件
 * 支持：标题(##)、列表(-)、粗体(**)
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        markdown.lines().forEach { line ->
            when {
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                line.startsWith("- ") -> {
                    Text(
                        text = "  • ${line.removePrefix("- ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                line.isBlank() -> {
                    Spacer(Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
