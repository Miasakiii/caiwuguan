package com.caiwuguan.data.parser

interface NotificationParser {
    fun canParse(packageName: String): Boolean
    fun parse(text: String, packageName: String): ParseResult
}
