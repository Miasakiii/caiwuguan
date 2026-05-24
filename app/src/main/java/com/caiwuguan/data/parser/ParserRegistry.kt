package com.caiwuguan.data.parser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParserRegistry @Inject constructor(
    private val wechatParser: WechatParser,
    private val alipayParser: AlipayParser,
    private val bankAppParser: BankAppParser,
    private val categoryClassifier: CategoryClassifier
) {
    private val parsers: List<NotificationParser> = listOf(wechatParser, alipayParser, bankAppParser)

    fun findParser(packageName: String): NotificationParser? =
        parsers.firstOrNull { it.canParse(packageName) }

    fun parse(notificationText: String, packageName: String): ParseResult {
        val parser = findParser(packageName) ?: return ParseResult.Ignore
        return parser.parse(notificationText, packageName)
    }
}
