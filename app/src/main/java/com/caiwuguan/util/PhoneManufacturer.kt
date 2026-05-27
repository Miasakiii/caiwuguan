package com.caiwuguan.util

import android.os.Build

object PhoneManufacturer {

    enum class Manufacturer {
        HUAWEI,
        XIAOMI,
        OPPO,
        VIVO,
        SAMSUNG,
        ONEPLUS,
        MEIZU,
        OTHER
    }

    fun detect(): Manufacturer {
        return when (Build.MANUFACTURER.lowercase()) {
            "huawei", "honor" -> Manufacturer.HUAWEI
            "xiaomi", "redmi", "poco" -> Manufacturer.XIAOMI
            "oppo", "realme", "oneplus" -> Manufacturer.OPPO
            "vivo", "iqoo" -> Manufacturer.VIVO
            "samsung" -> Manufacturer.SAMSUNG
            "meizu" -> Manufacturer.MEIZU
            else -> Manufacturer.OTHER
        }
    }

    fun getAutoStartGuide(): String {
        return when (detect()) {
            Manufacturer.HUAWEI -> """
                华为/荣耀手机保活设置：
                1. 打开「手机管家」→「应用启动管理」
                2. 找到「财务官」，关闭「自动管理」
                3. 在弹出的对话框中，允许「自启动」「关联启动」「后台活动」
                4. 返回「设置」→「电池」→「更多电池设置」
                5. 关闭「休眠时始终保持网络连接」
            """.trimIndent()

            Manufacturer.XIAOMI -> """
                小米/红米手机保活设置：
                1. 打开「设置」→「应用设置」→「自启动管理」
                2. 找到「财务官」，开启自启动权限
                3. 返回「设置」→「电池」→「更多电池设置」
                4. 关闭「休眠时保持网络连接」和「待机时保持网络连接」
                5. 打开「设置」→「应用设置」→「应用管理」→「财务官」
                6. 开启「后台弹出界面」权限
            """.trimIndent()

            Manufacturer.OPPO -> """
                OPPO/Realme 手机保活设置：
                1. 打开「设置」→「电池」→「更多电池设置」
                2. 关闭「休眠时保持网络连接」
                3. 打开「设置」→「应用管理」→「自启动管理」
                4. 找到「财务官」，允许自启动
                5. 返回「设置」→「应用管理」→「财务官」
                6. 开启「后台运行」和「关联启动」
            """.trimIndent()

            Manufacturer.VIVO -> """
                vivo/iQOO 手机保活设置：
                1. 打开「设置」→「电池」→「更多电池设置」
                2. 关闭「睡眠模式」
                3. 打开「设置」→「应用与权限」→「权限管理」
                4. 选择「财务官」→「单项权限设置」
                5. 允许「后台弹出界面」和「自启动」
            """.trimIndent()

            Manufacturer.SAMSUNG -> """
                三星手机保活设置：
                1. 打开「设置」→「电池和设备维护」→「电池」
                2. 关闭「自适应电池」或添加「财务官」到未受限制的应用
                3. 打开「设置」→「应用程序」→「财务官」
                4. 点击「电池」→ 选择「不受限制」
            """.trimIndent()

            Manufacturer.ONEPLUS -> """
                一加手机保活设置：
                1. 打开「设置」→「电池」→「更多电池设置」
                2. 关闭「休眠时保持网络连接」
                3. 打开「设置」→「应用管理」→「自启动管理」
                4. 找到「财务官」，允许自启动
            """.trimIndent()

            Manufacturer.MEIZU -> """
                魅族手机保活设置：
                1. 打开「手机管家」→「权限管理」
                2. 选择「财务官」→「后台管理」
                3. 允许后台运行
                4. 关闭「低功耗模式」
            """.trimIndent()

            Manufacturer.OTHER -> """
                通用保活设置：
                1. 打开「设置」→「电池」→ 确保「财务官」不受电池优化限制
                2. 打开「设置」→「应用管理」→「财务官」→ 允许自启动
                3. 关闭「省电模式」或「低电量模式」
                4. 部分手机需要在「开发者选项」中关闭「不保留活动」
            """.trimIndent()
        }
    }
}
