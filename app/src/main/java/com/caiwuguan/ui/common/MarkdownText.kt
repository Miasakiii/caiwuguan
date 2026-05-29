package com.caiwuguan.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 增强版 Markdown 渲染组件
 * 支持：标题(##/###)、列表(-/1.)、粗体(**)、行内代码(`)、代码块(```)、链接[text](url)
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val lines = markdown.lines()
    var i = 0

    Column(modifier = modifier) {
        while (i < lines.size) {
            val line = lines[i]

            when {
                // 代码块
                line.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // 跳过结束的 ```

                    CodeBlock(codeLines.joinToString("\n"))
                }

                // 二级标题
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 三级标题
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 无序列表
                line.startsWith("- ") || line.startsWith("* ") -> {
                    val content = line.removePrefix("- ").removePrefix("* ")
                    Row {
                        Text(
                            text = "  • ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = parseInlineMarkdown(content),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 有序列表
                line.matches(Regex("^\\d+\\. .*")) -> {
                    val number = line.substringBefore(".")
                    val content = line.substringAfter(". ")
                    Row {
                        Text(
                            text = "  $number. ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = parseInlineMarkdown(content),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 空行
                line.isBlank() -> {
                    Spacer(Modifier.height(4.dp))
                }

                // 普通文本
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            i++
        }
    }
}

/**
 * 解析行内 Markdown：粗体、行内代码、链接
 */
@Composable
private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            // 行内代码 `code`
            val codeMatch = Regex("`([^`]+)`").find(remaining)
            // 粗体 **text**
            val boldMatch = Regex("\\*\\*([^*]+)\\*\\*").find(remaining)
            // 链接 [text](url)
            val linkMatch = Regex("\\[([^]]+)]\\(([^)]+)\\)").find(remaining)

            // 找到最早出现的匹配
            val earliest = listOfNotNull(codeMatch, boldMatch, linkMatch)
                .minByOrNull { it.range.first }

            if (earliest == null) {
                append(remaining)
                break
            }

            // 添加匹配前的文本
            if (earliest.range.first > 0) {
                append(remaining.substring(0, earliest.range.first))
            }

            when (earliest) {
                codeMatch -> {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        background = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f)
                    )) {
                        append(earliest.groupValues[1])
                    }
                }
                boldMatch -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(earliest.groupValues[1])
                    }
                }
                linkMatch -> {
                    withStyle(SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )) {
                        append(earliest.groupValues[1])
                    }
                }
            }

            remaining = remaining.substring(earliest.range.last + 1)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
