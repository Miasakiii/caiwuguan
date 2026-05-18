# 财务官 - 技术架构设计

## 整体架构

```text
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│         Jetpack Compose + Navigation             │
├─────────────────────────────────────────────────┤
│                ViewModel Layer                   │
│      StateFlow + Side Effects (Channel)          │
├─────────────────────────────────────────────────┤
│                Domain Layer                      │
│     Use Cases (纯 Kotlin，不依赖 Android)         │
├─────────────────────────────────────────────────┤
│                  Data Layer                      │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────┐  │
│  │  Room DB  │ │ Prefs    │ │ Notification    │  │
│  │  (账单)   │ │ (设置)   │ │ Parser (解析器) │  │
│  └──────────┘ └──────────┘ └─────────────────┘  │
│  ┌──────────────────┐ ┌───────────────────────┐  │
│  │ Import Engine    │ │ Budget / Ledger       │  │
│  │ (CSV 导入+去重)  │ │ (预算/多账本)         │  │
│  └──────────────────┘ └───────────────────────┘  │
├─────────────────────────────────────────────────┤
│               Service Layer                      │
│  ┌─────────────────────┐ ┌────────────────────┐  │
│  │ NotificationListener│ │ KeepAliveService   │  │
│  │     Service         │ │ (前台服务保活)      │  │
│  └─────────────────────┘ └────────────────────┘  │
│  ┌─────────────────────┐ ┌────────────────────┐  │
│  │ SmsReceiver(P2可选) │ │ WorkManager        │  │
│  │ (短信监听)          │ │ (定时检查)          │  │
│  └─────────────────────┘ └────────────────────┘  │
├─────────────────────────────────────────────────┤
│              External (可选)                      │
│  ┌─────────────────────┐ ┌────────────────────┐  │
│  │  AI API (DeepSeek)  │ │ Widget (Glance)    │  │
│  └─────────────────────┘ └────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### 分层职责

| 层级 | 职责 | 关键技术 |
| ------ | ------ | ------ |
| UI Layer | 页面渲染、用户交互 | Jetpack Compose, Navigation Compose |
| ViewModel Layer | 状态管理、业务编排 | StateFlow, SavedStateHandle, Hilt |
| Domain Layer | 纯业务逻辑，不依赖 Android | UseCase, 数据模型定义 |
| Data Layer | 数据持久化、解析、导入、偏好 | Room, DataStore, Parser, Importer |
| Service Layer | 后台常驻、系统事件监听 | NotificationListenerService, BroadcastReceiver, WorkManager |
| External | 可选的外部能力 | DeepSeek API, Glance Widget |

---

## 模块划分

```text
app/
├── di/                    # Hilt 依赖注入
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── ParserModule.kt
├── ui/                    # UI 层
│   ├── home/             # 首页（今日账单 + 快捷入口）
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── bill/             # 账单列表
│   │   ├── BillListScreen.kt
│   │   ├── BillListViewModel.kt
│   │   ├── BillEditScreen.kt
│   │   └── BillEditViewModel.kt
│   ├── stats/            # 统计图表
│   │   ├── StatsScreen.kt
│   │   └── StatsViewModel.kt
│   ├── add/              # 手动记账
│   │   ├── AddBillScreen.kt
│   │   └── AddBillViewModel.kt
│   ├── settings/         # 设置
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── import/           # 账单导入
│   │   ├── ImportScreen.kt
│   │   ├── ImportPreviewScreen.kt
│   │   ├── ImportResultScreen.kt
│   │   └── ImportViewModel.kt
│   ├── budget/           # 预算管理
│   │   ├── BudgetScreen.kt
│   │   └── BudgetViewModel.kt
│   ├── onboarding/       # 首次启动引导
│   │   └── OnboardingScreen.kt
│   └── common/           # 通用组件
│       ├── BillCard.kt
│       ├── CategoryIcon.kt
│       ├── AmountText.kt
│       └── EmptyState.kt
├── domain/               # 业务逻辑层
│   ├── model/            # 数据模型
│   │   ├── Bill.kt
│   │   ├── Category.kt
│   │   ├── MonthlyStats.kt
│   │   └── Budget.kt
│   ├── repository/       # Repository 接口
│   │   ├── BillRepository.kt
│   │   ├── BudgetRepository.kt
│   │   └── LedgerRepository.kt
│   └── usecase/          # 用例
│       ├── GetMonthlyStatsUseCase.kt
│       ├── ImportBillsUseCase.kt
│       └── AnalyzeCategoryUseCase.kt
├── data/                 # 数据层
│   ├── db/               # Room 数据库
│   │   ├── entity/
│   │   │   ├── BillEntity.kt
│   │   │   ├── BudgetEntity.kt
│   │   │   ├── LedgerEntity.kt
│   │   │   ├── MerchantCategoryEntity.kt
│   │   │   └── MonthlyStatsEntity.kt
│   │   ├── dao/
│   │   │   ├── BillDao.kt
│   │   │   ├── BudgetDao.kt
│   │   │   ├── LedgerDao.kt
│   │   │   └── MerchantCategoryDao.kt
│   │   ├── database/
│   │   │   ├── AppDatabase.kt
│   │   │   └── Converters.kt
│   │   └── migration/
│   │       └── Migrations.kt
│   ├── parser/           # 通知解析器
│   │   ├── NotificationParser.kt      # 接口
│   │   ├── ParseResult.kt             # 解析结果密封类
│   │   ├── ParserRegistry.kt          # 包名 → Parser 路由
│   │   ├── WechatParser.kt
│   │   ├── AlipayParser.kt
│   │   ├── BankAppParser.kt
│   │   └── CategoryClassifier.kt      # 智能分类引擎
│   ├── importer/         # 账单导入引擎
│   │   ├── BillImporter.kt            # 接口
│   │   ├── WechatCsvImporter.kt
│   │   ├── AlipayCsvImporter.kt
│   │   ├── BankCsvImporter.kt
│   │   ├── GenericCsvImporter.kt
│   │   ├── Deduplicator.kt            # 去重引擎
│   │   └── EncodingDetector.kt        # 编码自动检测
│   ├── repository/       # Repository 实现
│   │   ├── BillRepositoryImpl.kt
│   │   ├── BudgetRepositoryImpl.kt
│   │   └── LedgerRepositoryImpl.kt
│   └── prefs/            # DataStore 偏好设置
│       └── AppPreferences.kt
├── service/              # 后台服务
│   ├── NotificationListenerServiceImpl.kt
│   ├── KeepAliveService.kt
│   ├── KeepAliveWorker.kt
│   └── SmsReceiver.kt
├── ai/                   # AI 分析模块
│   ├── AiAnalyzer.kt
│   ├── DeepSeekClient.kt
│   └── PromptTemplates.kt
├── widget/               # 桌面小组件
│   ├── FinanceWidget.kt
│   └── FinanceWidgetReceiver.kt
└── util/                 # 工具类
    ├── NotificationHelper.kt
    ├── DateUtils.kt
    ├── ManufacturerHelper.kt
    └── PermissionHelper.kt
```

---

## 核心数据模型

### Bill Entity

```kotlin
@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["source"]),
        Index(value = ["ledgerId"]),
        Index(value = ["source", "timestamp"]) // 复合索引，去重查询用
    ]
)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,                // 金额（单位：分，避免浮点精度问题）
    val type: BillType,              // INCOME / EXPENSE
    val category: Category,          // 分类
    val merchant: String,            // 商户名
    val description: String,         // 描述
    val source: PaymentSource,       // 微信/支付宝/银行/现金
    val ledgerId: Long = 0,          // 所属账本 ID（默认账本=0）
    val transactionId: String?,      // 交易单号（用于精确去重）
    val notificationText: String?,   // 原始通知文本（调试用）
    val isAutoRecorded: Boolean,     // 是否自动记录
    val timestamp: Long,             // 交易时间戳
    val createdAt: Long              // 创建时间
)
```

### 支付来源

```kotlin
enum class PaymentSource(val displayName: String) {
    WECHAT("微信"),
    ALIPAY("支付宝"),
    BANK_ICBC("工商银行"),
    BANK_CCB("建设银行"),
    BANK_ABC("农业银行"),
    BANK_BOC("中国银行"),
    BANK_COMM("交通银行"),
    BANK_CMB("招商银行"),
    CASH("现金"),
    OTHER("其他")
}
```

### 消费分类

```kotlin
enum class Category(val displayName: String, val icon: String) {
    FOOD("餐饮", "🍜"),
    TRANSPORT("交通", "🚗"),
    SHOPPING("购物", "🛒"),
    ENTERTAINMENT("娱乐", "🎮"),
    HOUSING("住房", "🏠"),
    MEDICAL("医疗", "💊"),
    EDUCATION("教育", "📚"),
    TRANSFER("转账", "💸"),
    RED_PACKET("红包", "🧧"),
    SALARY("工资", "💰"),
    INVESTMENT("投资", "📈"),
    OTHER("其他", "📦")
}
```

### 交易类型

```kotlin
enum class BillType {
    INCOME,   // 收入
    EXPENSE   // 支出
}
```

### 月度统计

```kotlin
data class MonthlyStats(
    val year: Int,
    val month: Int,
    val totalIncome: Long,     // 分
    val totalExpense: Long,    // 分
    val categoryBreakdown: Map<Category, Long>,  // 分
    val dailyTrend: List<Pair<Int, Long>>        // day -> amount(分)
)
```

### 账本 (Ledger)

```kotlin
@Entity(tableName = "ledgers")
data class Ledger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                // 账本名称
    val isDefault: Boolean = false,  // 是否默认账本
    val createdAt: Long
)
```

### 预算 (Budget)

```kotlin
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["year", "month"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int,
    val totalBudget: Long,                       // 月度总预算（分）
    val categoryBudgets: Map<Category, Long>,    // 分类预算（JSON 存储，分）
    val createdAt: Long
)
```

### 商户分类映射 (学习记录)

```kotlin
@Entity(
    tableName = "merchant_category",
    indices = [Index(value = ["merchant"], unique = true)]
)
data class MerchantCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,           // 商户名
    val category: Category,         // 用户修正后的分类
    val source: PaymentSource?,     // 来源（可选，null 表示全来源通用）
    val updatedAt: Long
)
```

### 类型转换器

```kotlin
class Converters {
    private val gson = Gson()  // 复用单例，避免每次 new Gson()

    @TypeConverter fun fromCategory(value: Category): String = value.name
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
    @TypeConverter fun fromBillType(value: BillType): String = value.name
    @TypeConverter fun toBillType(value: String): BillType = BillType.valueOf(value)
    @TypeConverter fun fromPaymentSource(value: PaymentSource): String = value.name
    @TypeConverter fun toPaymentSource(value: String): PaymentSource = PaymentSource.valueOf(value)
    @TypeConverter fun fromCategoryMap(map: Map<Category, Long>): String = gson.toJson(map)
    @TypeConverter fun toCategoryMap(json: String): Map<Category, Long> =
        gson.fromJson(json, object : TypeToken<Map<Category, Long>>() {}.type)
}
```

---

## DAO 设计

### BillDao

```kotlin
@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: Bill): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(bills: List<Bill>): List<Long>

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getById(id: Long): Flow<Bill?>

    @Query("SELECT * FROM bills WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<Bill>>

    @Query("""
        SELECT * FROM bills 
        WHERE strftime('%Y', timestamp/1000, 'unixepoch', 'localtime') = CAST(:year AS TEXT)
          AND strftime('%m', timestamp/1000, 'unixepoch', 'localtime') = printf('%02d', :month)
        ORDER BY timestamp DESC
    """)
    fun getByMonth(year: Int, month: Int): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<Bill>>

    @Query("""
        SELECT * FROM bills 
        WHERE (merchant LIKE '%' || :keyword || '%' 
               OR description LIKE '%' || :keyword || '%')
        ORDER BY timestamp DESC
    """)
    fun search(keyword: String): Flow<List<Bill>>

    @Query("SELECT SUM(amount) FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :start AND :end")
    fun getTotalExpense(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM bills WHERE type = 'INCOME' AND timestamp BETWEEN :start AND :end")
    fun getTotalIncome(start: Long, end: Long): Flow<Long?>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :start AND :end
        GROUP BY category ORDER BY total DESC
    """)
    fun getCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryAmount>>

    // 去重查询：精确匹配
    @Query("""
        SELECT * FROM bills 
        WHERE source = :source AND transactionId = :transactionId 
        LIMIT 1
    """)
    suspend fun findByTransactionId(source: String, transactionId: String): Bill?

    // 去重查询：模糊匹配（时间窗口 ±5min）
    @Query("""
        SELECT * FROM bills 
        WHERE source = :source 
          AND amount = :amount 
          AND merchant = :merchant
          AND timestamp BETWEEN :timeStart AND :timeEnd
        LIMIT 1
    """)
    suspend fun findDuplicate(source: String, amount: Long, merchant: String,
                              timeStart: Long, timeEnd: Long): Bill?

    // 去重查询：宽松匹配（跨来源）
    @Query("""
        SELECT * FROM bills 
        WHERE amount = :amount 
          AND merchant = :merchant
          AND timestamp BETWEEN :dayStart AND :dayEnd
        LIMIT 1
    """)
    suspend fun findLooseDuplicate(amount: Long, merchant: String,
                                   dayStart: Long, dayEnd: Long): Bill?
}

data class CategoryAmount(
    val category: String,
    val total: Long  // 分
)
```

### BudgetDao

```kotlin
@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    fun getByMonth(year: Int, month: Int): Flow<Budget?>

    @Delete
    suspend fun delete(budget: Budget)
}
```

### LedgerDao

```kotlin
@Dao
interface LedgerDao {
    @Insert
    suspend fun insert(ledger: Ledger): Long

    @Update
    suspend fun update(ledger: Ledger)

    @Delete
    suspend fun delete(ledger: Ledger)

    @Query("SELECT * FROM ledgers ORDER BY isDefault DESC, createdAt ASC")
    fun getAll(): Flow<List<Ledger>>

    @Query("SELECT * FROM ledgers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): Ledger?
}
```

### MerchantCategoryDao

```kotlin
@Dao
interface MerchantCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mapping: MerchantCategory)

    @Query("SELECT * FROM merchant_category WHERE merchant = :merchant LIMIT 1")
    suspend fun getByMerchant(merchant: String): MerchantCategory?
}
```

---

## 通知解析策略

### 解析框架

```kotlin
// 解析结果密封类
sealed class ParseResult {
    data class Success(
        val amount: Long,               // 金额（分）
        val type: BillType,
        val merchant: String = "",
        val category: Category = Category.OTHER,
        val transactionId: String? = null,
        val confidence: Float = 1.0f
    ) : ParseResult()

    data class Failure(val reason: String) : ParseResult()
    data object Ignore : ParseResult()
}

// Parser 接口
interface NotificationParser {
    fun canParse(packageName: String): Boolean
    fun parse(text: String, packageName: String): ParseResult
}

// ParserRegistry：包名 → Parser 路由
class ParserRegistry @Inject constructor(
    private val wechatParser: WechatParser,
    private val alipayParser: AlipayParser,
    private val bankAppParser: BankAppParser
) {
    private val packageMap = mapOf(
        "com.tencent.mm" to wechatParser,
        "com.eg.android.AlipayGphone" to alipayParser
    )

    fun getParser(packageName: String): NotificationParser? =
        packageMap[packageName]
}
```

### 微信支付通知格式

```text
微信支付收款到账 ¥25.00
微信支付凭证    支付金额 ¥15.50
你已成功向「星巴克」付款 ¥32.00
你收到了一个红包 ¥8.88
你已转账 ¥100.00 给 张三
微信支付退款 ¥10.00
```

**解析规则**：

- 金额正则：`(?:付款|支付|收款|到账|红包|转账|退款).*?[¥￥]?\s*([\d,.]+)`
- 商户提取：`「(.+?)」` 或 `向(.+?)付款` 或 `给\s*(.+?)$`
- 类型判断：收款/到账/红包/退款 → INCOME，付款/支付 → EXPENSE
- 忽略规则：零钱提现、充值类通知

### 支付宝通知格式

```text
支付宝成功付款 ¥18.00
你已成功付款 ¥100.00 给 饿了么
花呗自动还款 ¥3,500.00
你收到一笔转账 ¥500.00
```

**解析规则**：

- 金额正则：`(?:付款|支付|收款|还款|转账).*?[¥￥]?\s*([\d,.]+)`
- 商户提取：`给\s*(.+?)$` 或 `「(.+?)」`
- 花呗还款归类为"转账"

### 银行 App 通知格式

```text
【工商银行】您尾号1234的卡片消费人民币158.00元...
【招商银行】您账户8888于05月12日消费支出人民币¥50.00...
【建设银行】...工资入账15000.00元
```

**解析规则**：

- 金额正则：`消费.*?[人民币]*[¥￥]?\s*([\d,.]+)\s*元`
- 卡号提取：`尾号(\d{4})`
- 银行号码前缀过滤：95588(工商)/95533(建设)/95599(农业)/95566(中国)/95559(交通)/95555(招商)

---

## 智能分类引擎

```kotlin
data class ClassificationResult(
    val category: Category,
    val confidence: Float  // 0.0 ~ 1.0
)

class CategoryClassifier @Inject constructor(
    private val merchantCategoryDao: MerchantCategoryDao
) {
    // 分类策略（优先级从高到低）：
    // 1. 支付宝导入自带分类字段 → 直接映射 (confidence = 1.0)
    // 2. 用户历史修正记录 → merchant_category 表 (confidence = 0.9)
    // 3. 关键词匹配 → 规则引擎 (confidence = 0.7)
    // 4. 兜底 → OTHER (confidence = 0.0)

    suspend fun classify(
        merchant: String,
        source: PaymentSource,
        sourceCategory: String? = null  // 支付宝导入时可带分类
    ): ClassificationResult {
        // 优先级 1：来源自带分类
        if (sourceCategory != null) {
            val mapped = mapSourceCategory(sourceCategory)
            if (mapped != null) return ClassificationResult(mapped, 1.0f)
        }

        // 优先级 2：用户修正记录
        val learned = merchantCategoryDao.getByMerchant(merchant)
        if (learned != null) return ClassificationResult(learned.category, 0.9f)

        // 优先级 3：关键词匹配
        val keywordResult = matchByKeyword(merchant)
        if (keywordResult != null) return keywordResult

        // 优先级 4：兜底
        return ClassificationResult(Category.OTHER, 0.0f)
    }

    private fun matchByKeyword(merchant: String): ClassificationResult? {
        val rules = mapOf(
            Category.FOOD to listOf("美团", "饿了么", "肯德基", "麦当劳", "星巴克", "瑞幸",
                "海底捞", "外卖", "餐厅", "食堂", "奶茶", "咖啡", "烧烤", "火锅"),
            Category.TRANSPORT to listOf("滴滴", "高德", "地铁", "公交", "出租", "加油",
                "停车", "高速", "铁路", "航空", "12306", "携程"),
            Category.SHOPPING to listOf("淘宝", "京东", "拼多多", "天猫", "超市",
                "便利店", "商场", "唯品会", "苏宁"),
            Category.ENTERTAINMENT to listOf("电影", "游戏", "KTV", "健身", "旅游", "酒店"),
            Category.HOUSING to listOf("房租", "物业", "水电", "燃气", "宽带"),
            Category.MEDICAL to listOf("医院", "药店", "诊所", "体检", "药房")
        )
        for ((category, keywords) in rules) {
            if (keywords.any { merchant.contains(it, ignoreCase = true) }) {
                return ClassificationResult(category, 0.7f)
            }
        }
        return null
    }
}
```

---

## 账单导入引擎

### Importer 接口

```kotlin
interface BillImporter {
    /** 自动检测文件来源，返回 null 表示无法识别 */
    suspend fun detect(fileUri: Uri, context: Context): PaymentSource?

    /** 解析文件，返回导入结果 */
    suspend fun parse(fileUri: Uri, context: Context): ImportResult
}

data class ImportResult(
    val bills: List<Bill>,
    val totalParsed: Int,
    val totalDuplicate: Int,
    val totalFailed: Int,
    val errors: List<ImportError>
)

data class ImportError(val line: Int, val reason: String)
```

### 各导入器实现要点

| 导入器 | 编码 | 特殊处理 |
|--------|------|----------|
| WechatCsvImporter | UTF-8 BOM | 跳过统计摘要行，查找"交易时间"表头；金额去 ¥ 符号 |
| AlipayCsvImporter | GBK/UTF-8 自动检测 | 交易分类字段直接映射 Category（最高优先级） |
| BankCsvImporter | UTF-8/GBK/GB2312 自动检测 | 支持收入金额/支出金额双列格式；按银行配置列映射 |
| GenericCsvImporter | 自动检测 | 用户自定义列映射 UI；映射规则持久化 |

### 字段映射

| 来源 | 交易时间 | 商户 | 描述 | 类型 | 金额 | 分类 | 交易单号 |
|------|----------|------|------|------|------|------|----------|
| 微信 | 交易时间 | 交易对方 | 商品 | 收·支 | 金额(元) | — | 微信支付单号 |
| 支付宝 | 交易时间 | 交易对方 | 商品说明 | 收·支 | 金额 | 交易分类 | 商家订单号 |
| 银行 | 交易日期 | — | 摘要 | 收入/支出列 | 金额 | — | 流水号 |

### 去重引擎 (Deduplicator)

```kotlin
class Deduplicator @Inject constructor(
    private val billDao: BillDao
) {
    /**
     * 三级去重策略：
     * 1. 精确匹配：source + transactionId → 命中则一定是重复
     * 2. 模糊匹配：source + amount + timestamp(±5min) + merchant → 同源近似
     * 3. 宽松匹配：amount + date + merchant → 跨来源去重
     */
    suspend fun check(bill: Bill): DuplicateCheckResult {
        // 精确匹配
        if (bill.transactionId != null) {
            val exact = billDao.findByTransactionId(bill.source.name, bill.transactionId)
            if (exact != null) return DuplicateCheckResult(true, MatchType.EXACT, exact.id)
        }

        // 模糊匹配（±5 分钟）
        val windowMs = 5 * 60 * 1000L
        val fuzzy = billDao.findDuplicate(
            bill.source.name, bill.amount, bill.merchant,
            bill.timestamp - windowMs, bill.timestamp + windowMs
        )
        if (fuzzy != null) return DuplicateCheckResult(true, MatchType.FUZZY, fuzzy.id)

        // 宽松匹配（同一天，跨来源）
        val dayStart = bill.timestamp - (bill.timestamp % (24 * 60 * 60 * 1000L))
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L
        val loose = billDao.findLooseDuplicate(bill.amount, bill.merchant, dayStart, dayEnd)
        if (loose != null) return DuplicateCheckResult(true, MatchType.LOOSE, loose.id)

        return DuplicateCheckResult(false, null, null)
    }
}

enum class MatchType { EXACT, FUZZY, LOOSE }

data class DuplicateCheckResult(
    val isDuplicate: Boolean,
    val matchType: MatchType?,
    val existingBillId: Long?
)
```

### 导入流程 UI

```text
文件选择 (SAF)  →  自动识别来源  →  解析预览  →  确认导入  →  结果报告
                      │                   │
                  手动选择来源        显示：导入 N 条
                  (无法识别时)        跳过 M 条重复
                                      失败 K 条
```

---

## 后台保活方案

```kotlin
// 1. 前台 Service（核心）- Android 14 需指定 FOREGROUND_SERVICE_TYPE_SPECIAL_USE
class KeepAliveService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification()
        ServiceCompat.startForeground(
            this,
            ONGOING_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        return START_STICKY  // 被杀后自动重启
    }
}

// 2. NotificationListenerService（系统级，保活优先级最高）
class NotificationListenerServiceImpl : NotificationListenerService() {
    // 系统保证此服务存活，同时负责解析支付通知
}

// 3. WorkManager 兜底（定期检查服务状态）
class KeepAliveWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        // 检查 NotificationListener 是否活跃
        // 如果不活跃，发送通知提醒用户
        return Result.success()
    }
}

// 4. 厂商适配引导
fun checkAndGuideBatteryOptimization(context: Context) {
    // 检测厂商 → 引导用户到对应设置页面
    // 华为：设置 → 电池 → 应用启动管理
    // 小米：设置 → 应用设置 → 自启动管理
    // OPPO：设置 → 电池 → 自定义耗电保护
    // vivo：设置 → 电池 → 后台高耗电
}
```

### 厂商检测工具

```kotlin
object ManufacturerHelper {
    fun isHuawei(): Boolean = Build.MANUFACTURER.equals("HUAWEI", ignoreCase = true)
    fun isXiaomi(): Boolean = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
    fun isOppo(): Boolean = Build.MANUFACTURER.equals("OPPO", ignoreCase = true)
    fun isVivo(): Boolean = Build.MANUFACTURER.equals("vivo", ignoreCase = true)
    fun isSamsung(): Boolean = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
}
```

---

## Android 14 (API 34) 前台服务适配

Android 14 要求所有前台服务声明 `foregroundServiceType`，否则启动时抛出 `MissingForegroundServiceTypeException`。

```kotlin
// AndroidManifest.xml 中声明
<service
    android:name=".service.KeepAliveService"
    android:foregroundServiceType="specialUse"
    android:exported="false" />

// 服务启动时指定类型
val notification = createForegroundNotification()
ServiceCompat.startForeground(
    this,
    NOTIFICATION_ID,
    notification,
    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
)
```

**注意**：

- `specialUse` 是最适合的类型（通知监听不属于 dataSync/location/mediaPlayback）
- 需在 AndroidManifest 的 `<service>` 标签中添加 `android:foregroundServiceType="specialUse"`
- 同时需要在 `<uses-permission>` 中声明 `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`

---

## 短信监听限制说明

**重要：Android 4.4+ 已不允许第三方 App 静默接收 `SMS_RECEIVED` 广播。**

- `RECEIVE_SMS` 权限仍可申请，但只有默认短信 App 才能拦截并终止广播
- 第三方 App 只能接收到广播但无法阻止其他 App 接收，且 Android 10+ 进一步限制了 SMS 读取
- **结论**：短信监听作为 P0 功能不可行，降级为 P2 可选功能，仅在用户手动授权且系统允许时使用

**建议替代方案**：

- 依赖 NotificationListenerService 读取银行 App 推送通知（更可靠）
- 部分银行支持短信/通知双通道推送，通知内容已包含完整交易信息

---

## 权限管理架构

### 权限清单

| 权限 | 用途 | 必要性 |
|------|------|--------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 读取通知 | P0 核心 |
| `RECEIVE_SMS` | 读取银行短信（可选） | P2 补充 |
| `INTERNET` | AI API 调用 | P2 可选 |
| `FOREGROUND_SERVICE` | 前台保活 | P1 增强 |
| `POST_NOTIFICATIONS` | 通知栏提醒 | P1 增强 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 电池优化白名单 | P1 增强 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启 | P1 增强 |

### 权限引导流程

```text
首次启动
  │
  ├── 检查通知监听权限
  │     ├── 未开启 → 引导弹窗 → 跳转系统设置
  │     └── 已开启 → 继续
  │
  ├── 检查短信权限（可选）
  │     ├── 未授予 → 说明用途 → 系统权限弹窗
  │     └── 已授予 → 继续
  │
  ├── 检查电池优化
  │     ├── 未关闭 → 引导弹窗 → 跳转设置
  │     └── 已关闭 → 继续
  │
  └── 厂商自启动引导（按需）
        └── 检测厂商 → 显示对应操作步骤
```

---

## UI 架构

### Navigation 设计

```text
BottomNavigation
├── HomeScreen (首页)
│     └── → AddBillScreen (手动记账)
│     └── → BillEditScreen (编辑账单)
├── BillListScreen (账单)
│     └── → BillEditScreen
│     └── → SearchScreen
├── StatsScreen (统计)
│     └── → CategoryDetailScreen
└── SettingsScreen (设置)
      ├── → ImportScreen (导入)
      │     ├── → ImportPreviewScreen
      │     └── → ImportResultScreen
      ├── → BudgetScreen (预算)
      ├── → LedgerScreen (账本管理)
      ├── → KeepAliveGuideActivity (保活引导)
      └── → ExportScreen (导出)
```

### ViewModel 状态管理规范

```kotlin
// 统一使用 StateFlow + UiState 模式
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    data class UiState(
        val todayExpense: Long = 0L,    // 分
        val todayIncome: Long = 0L,     // 分
        val monthExpense: Long = 0L,    // 分
        val monthIncome: Long = 0L,     // 分
        val recentBills: List<Bill> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Side effects（导航、Toast、SnackBar）
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    sealed class Event {
        data class NavigateTo(val route: String) : Event()
        data class ShowSnackbar(val message: String) : Event()
    }
}
```

---

## 数据导出

### CSV 导出

```kotlin
class CsvExporter {
    fun export(bills: List<Bill>, outputStream: OutputStream) {
        val writer = outputStream.bufferedWriter()
        writer.write("交易时间,类型,金额(元),分类,商户,描述,来源,备注")
        writer.newLine()
        bills.forEach { bill ->
            val yuan = "%.2f".format(bill.amount / 100.0)  // 分→元
            writer.write("${formatDate(bill.timestamp)},${bill.type.displayName}," +
                "$yuan,${bill.category.displayName},${bill.merchant}," +
                "${bill.description},${bill.source.displayName},${bill.notificationText ?: ""}")
            writer.newLine()
        }
        writer.flush()
    }
}
```

### Excel 导出（已移除）

> Apache POI 依赖体积过大（10-15MB），与 APK < 15MB 目标矛盾，已从依赖清单中移除。
> 如需 Excel 支持，建议通过在线转换工具或使用 Google Sheets 打开 CSV 后导出。

---

## 桌面小组件

```kotlin
// Glance Compose 实现
class FinanceWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val todayStats = // 从数据库读取
        Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
            Text("今日消费", style = TextStyle(fontSize = 14.sp))
            Text("¥${todayStats.expense}", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text("本月 ¥${todayStats.monthExpense}", style = TextStyle(fontSize = 12.sp))
        }
    }
}

class FinanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FinanceWidget()
}
```

---

## AI 分析模块（P2）

```kotlin
// 免费 API 方案
object AiAnalyzer {
    private const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    
    suspend fun analyzeMonthly(
        bills: List<Bill>,
        stats: MonthlyStats
    ): String {
        val prompt = """
        你是一个个人财务分析师。请根据以下账单数据给出分析和建议：
        
        本月收入：${stats.totalIncome}元
        本月支出：${stats.totalExpense}元
        分类支出：
        ${stats.categoryBreakdown.entries.joinToString("\n") { 
            "  ${it.key.displayName}: ${it.value}元" 
        }}
        
        请给出：
        1. 消费结构分析（哪些支出占比过高）
        2. 与上月对比趋势
        3. 节省建议（具体可执行）
        4. 风险提醒（如有异常消费模式）
        
        回复简洁，用中文，不超过300字。
        """.trimIndent()
        
        return callDeepSeek(prompt)
    }
}
```

### AI 安全策略

- API Key 加密存储于 EncryptedSharedPreferences
- 请求频率限制：每日最多 N 次（可配置）
- 离线降级：本地统计替代 AI 分析
- 传输数据仅包含聚合统计，不传原始账单明细

---

## 依赖清单

```kotlin
// build.gradle.kts (project-level)
plugins {
    id("com.android.application") version "8.5.0"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("com.google.dagger.hilt.android") version "2.51.1"
    id("com.google.devtools.ksp") version "2.0.0-1.0.24"  // KSP 替代 kapt
}

// build.gradle.kts (app)
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.04.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Room (使用 KSP 替代 kapt)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt (使用 KSP 替代 kapt)
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Charts (Vico - Compose 原生图表库)
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore (替代 SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // EncryptedSharedPreferences (使用 KTX 正式版替代 alpha)
    implementation("androidx.security:security-crypto-ktx:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // HTTP (AI API 调用)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Glance (桌面小组件)
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")

    // CSV 解析
    implementation("com.opencsv:opencsv:5.9")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

---

## 性能指标

| 指标 | 目标 |
|------|------|
| 通知解析耗时 | < 50ms |
| 数据库查询耗时 | < 100ms（索引优化） |
| UI 列表滚动帧率 | 60fps |
| 冷启动时间 | < 1.5s |
| APK 体积 | < 15MB |
| 后台常驻内存 | < 30MB |

---

## 安全设计

| 维度 | 措施 |
|------|------|
| 数据存储 | 纯本地 Room 数据库，不上传云端 |
| 网络请求 | 仅 AI 模块有网络请求，其他模块不申请网络权限 |
| API Key | EncryptedSharedPreferences 加密存储 |
| 数据导出 | 仅用户主动触发，导出文件不自动分享 |
| 隐私 | 通知原文仅存本地用于调试，不外传 |
