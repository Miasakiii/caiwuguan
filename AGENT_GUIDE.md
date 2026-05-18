# 财务官 - Agent 实现指南

## 如何用 Agent 实现此项目

本文档定义了每个 Task 的输入/输出/验收标准，可以直接交给 Agent（如 Codex/Claude Code）逐个执行。

---

## 执行顺序

```text
Task 1.1 → 1.2 → 1.3 → 2.1 → 2.2 → 2.3 → 2.4 → 2.5
→ 3.1 → 3.2/3.3/3.4/3.5/3.6 (可并行) → 4.1 → 4.2 → 4.3 → 4.4
→ 5.1/5.2/5.3/5.4/5.5 (可并行) → 6.1 → 6.2 → 6.3 → 6.4 → 6.5
```

---

## Agent 执行模板

每个 Task 交给 Agent 时，使用以下 prompt 模板：

```text
## 任务：[Task 名称]

### 上下文
- 项目：财务官（Android 自动记账 App）
- 技术栈：Kotlin 2.0 + Jetpack Compose + Room + Hilt + KSP
- 架构：MVVM + Clean Architecture
- 参考文档：[ARCHITECTURE.md 相关章节]

### 具体要求
[从 ROADMAP.md 复制该 Task 的具体要求]

### 验收标准
- [ ] 代码可编译
- [ ] 单元测试通过
- [ ] 符合项目架构规范
- [ ] 关键路径有注释

### 输出
- 所有源码文件
- 单元测试文件
- 如有新增依赖，更新 build.gradle.kts
```

---

## Task 详细规格

### Task 1.1: 项目初始化

**Prompt for Agent:**

```text
创建一个 Android 项目，包名 com.financeofficer，要求：

1. 项目结构：
   app/src/main/java/com/financeofficer/
   ├── di/
   ├── ui/
   │   ├── home/
   │   ├── bill/
   │   ├── stats/
   │   ├── add/
   │   ├── settings/
   │   └── common/
   ├── domain/
   │   ├── model/
   │   ├── repository/
   │   └── usecase/
   ├── data/
   │   ├── db/
   │   │   ├── entity/
   │   │   ├── dao/
   │   │   └── database/
   │   ├── parser/
   │   ├── repository/
   │   └── prefs/
   ├── service/
   ├── ai/
   └── util/

2. build.gradle.kts 配置：
   - compileSdk 34, minSdk 26, targetSdk 34
   - Kotlin 2.0.0（使用 K2 编译器）
   - KSP 2.0.0-1.0.24（替代 kapt）
   - Compose BOM 2025.04.00
   - Room 2.6.1（使用 KSP）
   - Hilt 2.51.1（使用 KSP）
   - Navigation Compose 2.8.9
   - Vico 图表库 1.13.1
   - WorkManager 2.9.0
   - OkHttp 4.12.0
   - Gson 2.11.0
   - security-crypto-ktx 1.1.0（正式版）

3. AndroidManifest.xml 配置权限：
   - INTERNET
   - RECEIVE_BOOT_COMPLETED
   - FOREGROUND_SERVICE
   - FOREGROUND_SERVICE_SPECIAL_USE
   - POST_NOTIFICATIONS
   - REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

4. 前台 Service 声明：
   android:name=".service.KeepAliveService"
   android:foregroundServiceType="specialUse"

5. 主题配置：Material3 动态配色，支持 Dark Mode
6. Application 类配置 @HiltAndroidApp
7. MainActivity 配置 @AndroidEntryPoint
```

### Task 1.2: 数据库设计

**Prompt for Agent:**

```text
实现 Room 数据库层，包括：

1. Entity: BillEntity（参考 ARCHITECTURE.md 中的数据模型）
   - 字段：id, amount(Long, 单位分), type(INCOME/EXPENSE), category, merchant,
           description, source, notificationText, isAutoRecorded, timestamp, createdAt
   - 索引：timestamp, category, source, ledgerId
   - 注意：金额使用 Long（分），避免 Double 浮点精度问题

2. BillDao：
   - insert(bill: BillEntity): Long（使用 OnConflictStrategy.IGNORE）
   - insertAll(bills: List<BillEntity>): List<Long>（IGNORE）
   - update(bill: BillEntity)
   - delete(bill: BillEntity)
   - getById(id: Long): Flow<BillEntity?>
   - getByDateRange(start: Long, end: Long): Flow<List<BillEntity>>
   - getByMonth(year: Int, month: Int): Flow<List<BillEntity>>
   - getByCategory(category: String): Flow<List<BillEntity>>
   - getMonthlyStats(year: Int, month: Int): Flow<MonthlyStatsEntity>
   - getDailyAmounts(year: Int, month: Int): Flow<List<DailyAmount>>
   - getTotalExpense(start: Long, end: Long): Flow<Long?>（返回 Long 类型）
   - getTotalIncome(start: Long, end: Long): Flow<Long?>
   - search(keyword: String): Flow<List<BillEntity>>
   - 去重查询方法（精确/模糊/宽松匹配）

3. AppDatabase：
   - 版本 1
   - 自动迁移策略预留

4. 类型转换器 Converters：
   - Category enum ↔ String
   - BillType enum ↔ String
   - PaymentSource enum ↔ String
   - Map<Category, Long> ↔ JSON（Gson 复用单例实例，不要每次 new Gson()）

5. 单元测试：
   - 测试所有 DAO 方法
   - 测试索引是否生效
   - 测试边界情况（空数据、大量数据）
```

### Task 2.2: 微信支付解析器

**Prompt for Agent:**

```text
实现微信支付通知解析器 WechatParser：

1. 输入：通知文本 (String) + 包名 (String)
2. 输出：ParseResult
   - Success(amount: Long, merchant, type, category)  // 金额单位为分
   - Failure(reason)
   - Ignore(非支付通知)

3. 支持的通知格式：
   - "微信支付收款到账 ¥25.00" → 收入 2500 分
   - "你已成功向「星巴克」付款 ¥32.00" → 支出 3200 分，商户：星巴克
   - "微信支付凭证 支付金额 ¥15.50" → 支出 1550 分
   - "你收到了一个红包 ¥8.88" → 收入 888 分，分类：红包
   - "你已转账 ¥100.00 给 张三" → 支出 10000 分，分类：转账
   - "零钱提现 ¥500.00" → 忽略
   - "微信支付退款 ¥10.00" → 收入 1000 分，分类：退款

4. 解析逻辑：
   - 金额提取：支持 ¥ 和 ￥ 符号，支持千分位逗号
   - 金额转换：提取的元值需乘以 100 转为分存储
   - 商户提取：「」引号内内容，或"给 XXX"后的内容
   - 类型判断：收款/到账 → INCOME，付款/支付 → EXPENSE
   - 分类推断：基于关键词（餐饮/交通/购物等商户名映射）

5. 单元测试：至少 20 个测试用例，覆盖所有格式变体
```

### Task 2.3: 支付宝解析器

**Prompt for Agent:**

```text
实现支付宝支付通知解析器 AlipayParser：

1. 输入：通知文本 (String) + 包名 (String)
2. 输出：ParseResult
3. 支持的通知格式：
   - "支付宝成功付款 ¥18.00" → 支出 1800 分
   - "你已成功付款 ¥100.00 给 饿了么" → 支出 10000 分，商户：饿了么
   - "花呗自动还款 ¥3,500.00" → 支出 350000 分，分类：转账
   - "你收到一笔转账 ¥500.00" → 收入 50000 分
4. 解析逻辑同微信，金额/商户/类型提取（金额单位为分）
5. 单元测试：至少 15 个用例
```

### Task 2.4: 银行 App 通知解析器

**Prompt for Agent:**

```text
实现银行 App 通知解析器 BankAppParser：

1. BankAppParser：
   - 输入：通知内容 (String) + 发送包名 (String)
   - 输出：ParseResult
   - 支持银行：工商/建设/农业/中国/交通/招商
   - 解析格式：
     - "【工商银行】您尾号1234的卡片消费人民币158.00元" → 支出 15800 分
     - "【招商银行】您账户8888于05月12日消费支出人民币¥50.00" → 支出 5000 分
     - "【建设银行】...工资入账15000.00元" → 收入 1500000 分

2. 银行包名映射：
   - 工商银行 → PaymentSource.BANK_ICBC
   - 建设银行 → PaymentSource.BANK_CCB
   - 农业银行 → PaymentSource.BANK_ABC
   - 中国银行 → PaymentSource.BANK_BOC
   - 交通银行 → PaymentSource.BANK_COMM
   - 招商银行 → PaymentSource.BANK_CMB

> 注意：短信监听已降级为 P2 可选功能。此处仅实现银行 App 通知解析。

3. 单元测试：每个银行至少 5 个测试用例
```

### Task 2.5: 智能分类引擎

**Prompt for Agent:**

```text
实现智能分类引擎 CategoryClassifier：

1. 输入：商户名 (String) + 交易来源 (PaymentSource) + 原始分类字段（支付宝导入可选）
2. 输出：Category（分类结果）+ Float（置信度 0-1）

3. 分类策略（优先级从高到低）：
   - 支付宝导入自带分类字段 → 直接映射（置信度 1.0）
   - 用户历史修正记录 → 查 merchant_category 表（置信度 0.9）
   - 关键词匹配 → 规则引擎（置信度 0.7）
     - 餐饮：美团|饿了么|肯德基|麦当劳|星巴克|瑞幸|海底捞|外卖|餐厅|食堂
     - 交通：滴滴|高德|地铁|公交|出租|加油|停车|高速|铁路|航空
     - 购物：淘宝|京东|拼多多|天猫|超市|便利店|商场
     - 娱乐：电影|游戏|KTV|健身|旅游|酒店
     - 住房：房租|物业|水电|燃气|宽带
     - 医疗：医院|药店|诊所|体检
   - 兜底 → OTHER（置信度 0.0）

4. 学习机制：
   - 用户手动修正分类时，写入 merchant_category 表
   - 下次同一商户自动使用修正后的分类

5. 单元测试：至少 30 个用例，覆盖各分类和边界情况
```

### Task 5.1: 账单导入引擎

**Prompt for Agent:**

```text
实现账单文件导入功能：

1. Importer 接口：
   interface BillImporter {
       suspend fun detect(fileUri: Uri, context: Context): PaymentSource?
       suspend fun parse(fileUri: Uri, context: Context): ImportResult
   }

2. WechatCsvImporter：
   - 处理 UTF-8 BOM
   - 跳过前 N 行统计摘要（查找"交易时间"表头行）
   - 字段映射：交易时间→timestamp, 交易对方→merchant, 商品→description, 收/支→type, 金额(元)→amount
   - 金额清洗：去掉 ¥ 和逗号，元转分为 Long 存储
   - 交易类型：商户消费→EXPENSE, 微信红包→INCOME+RED_PACKET, 退款→INCOME
   - 来源标记：PaymentSource.WECHAT

3. AlipayCsvImporter：
   - 自动检测编码（GBK/UTF-8）
   - 字段映射：交易时间→timestamp, 交易对方→merchant, 商品说明→description, 收/支→type, 金额→amount
   - 交易分类字段→直接映射 Category（优先级最高）
   - 来源标记：PaymentSource.ALIPAY

4. BankCsvImporter：
   - 编码自动检测
   - 支持收入金额/支出金额双列格式
   - 按银行配置列映射（可扩展）
   - 来源标记：对应银行 PaymentSource

5. Deduplicator 去重引擎：
   - 精确匹配：source + transactionId（如果有的话）
   - 模糊匹配：source + amount + timestamp ±5min + merchant
   - 宽松匹配：amount + date + merchant（跨来源）
   - 返回：DuplicateCheckResult(isDuplicate, matchType, existingBillId)

6. 导入流程 UI（Compose）：
   - ImportScreen：文件选择（SAF Intent）
   - ImportPreviewScreen：显示解析结果 + 去重统计
   - ImportResultScreen：成功 N 条 / 跳过 M 条 / 失败 K 条
   - 进度条（大文件分批处理）

7. 单元测试：
   - 各格式 CSV 样本文件测试
   - 去重逻辑测试（精确/模糊/宽松）
   - 编码检测测试
   - 异常格式处理测试
```

### Task 3.2: 首页

**Prompt for Agent:**

```text
实现首页 UI（Jetpack Compose）：

1. 顶部卡片：
   - 今日日期（大字）
   - 今日支出 / 今日收入（Long 分转换为元显示）
   - 本月支出 / 本月收入

2. 预算进度条（P1 先放占位）

3. 最近账单列表：
   - 显示最近 5 笔
   - 每笔：分类图标 + 商户名 + 金额 + 时间
   - 点击跳转编辑
   - 空状态：引导用户开始记账

4. 快捷记账 FAB：
   - 右下角悬浮按钮
   - 点击进入手动记账页

5. ViewModel：
   - HomeViewModel
   - 暴露：todayStats, monthStats, recentBills
   - 使用 Hilt 注入 Repository

6. 预览：
   - 提供 @Preview 函数
   - 包含正常状态和空状态
```

### Task 4.1: NotificationListenerService

**Prompt for Agent:**

```text
实现通知监听服务：

1. NotificationListenerServiceImpl：
   - 继承 NotificationListenerService
   - 监听微信(com.tencent.mm)、支付宝(com.eg.android.AlipayGphone)通知
   - 收到通知 → 路由到对应 Parser → 解析 → 存入数据库
   - 去重：同一通知不重复处理（基于 key + 时间窗口）

2. ParserRegistry：
   - 维护 包名 → Parser 映射
   - 微信 → WechatParser
   - 支付宝 → AlipayParser
   - 银行包名列表 → BankAppParser

3. 处理流程：
   onNotificationPosted(sbn)
     → 提取包名和通知文本
     → 查找对应 Parser
     → 解析
     → 成功：写入数据库 + 发送本地广播更新 UI
     → 失败：记录到 debug 日志
     → 忽略：跳过

4. 单元测试：
   - Mock Notification 对象
   - 测试解析路由
   - 测试去重逻辑
   - 测试异常处理
```

### Task 4.2: 后台保活

**Prompt for Agent:**

```text
实现后台保活机制：

1. KeepAliveService（前台 Service）：
   - 常驻通知栏，显示"财务官运行中 - 今日消费 ¥XX.XX"
   - START_STICKY 模式
   - 使用 ServiceCompat.startForeground() 指定 FOREGROUND_SERVICE_TYPE_SPECIAL_USE 类型
   - 定期（每 15 分钟）检查 NotificationListener 状态

2. KeepAliveWorker（WorkManager）：
   - 每 30 分钟执行一次
   - 检查 NotificationListener 是否活跃
   - 如果不活跃，发送通知提醒用户

3. 厂商检测工具 ManufacturerHelper：
   fun isHuawei(): Boolean
   fun isXiaomi(): Boolean
   fun isOppo(): Boolean
   fun isVivo(): Boolean
   fun isSamsung(): Boolean

4. 保活引导页面 KeepAliveGuideActivity：
   - 显示当前保活状态（绿灯/红灯）
   - 检测厂商，显示对应的操作步骤
   - 一键跳转到系统设置页面
   - 支持华为/小米/OPPO/vivo/Samsung

5. 首次启动引导：
   - 检测到未开启通知权限 → 引导弹窗
   - 检测到未关闭电池优化 → 引导弹窗
   - 完成后标记，不再弹出
```

---

## 关键注意事项

### 1. 代码规范

- 所有公开 API 必须有 KDoc 注释
- ViewModel 使用 StateFlow 暴露状态
- Repository 使用 Flow 返回可观察数据
- 异常必须处理，不能静默吞掉

### 2. 测试要求

- 每个 Parser 必须有 ≥ 20 个单元测试
- DAO 层 100% 方法覆盖
- ViewModel 关键状态变化测试

### 3. 性能要求

- 通知解析 < 50ms
- 数据库查询 < 100ms（索引优化）
- UI 列表滚动 60fps

### 4. 安全要求

- 不申请网络权限（纯本地，AI 分析除外）
- 不收集任何用户数据
- 数据库文件不导出（除非用户主动导出）
- AI API Key 加密存储

### 5. 金额单位统一规则

- **内部存储**：所有金额使用 `Long` 类型，单位为**分**
- **UI 显示**：从分转换为元（`amount / 100.0`，格式化为 `%.2f`）
- **输入解析**：从通知/CSV 提取的元值需乘以 100 转为分
- **导出文件**：从分转换为元输出（保持用户阅读习惯）
- **禁止**：任何地方使用 `Double` 存储金额

### 6. Android 14 适配要点

- 前台服务必须声明 `android:foregroundServiceType="specialUse"`
- 使用 `ServiceCompat.startForeground()` 替代 `startForeground()`
- 在 Manifest 中同时声明 `<uses-permission>` 和 `<service>` 的 `foregroundServiceType`
