# 财务官 - 项目路线图

## 总览

- **总工期**：约 6-8 周（单人开发）
- **MVP 交付**：第 4 周
- **当前进度**：Phase 1-3 已完成，Phase 4 部分完成
- **技术栈**：Kotlin 2.0 + Jetpack Compose + Room + Hilt + KSP

---

## Phase 1: 项目基础 (Week 1) ✅ 已完成

### Task 1.1: 项目初始化 ✅

- [x] 创建 Android 项目，配置 Gradle
- [x] 配置 Compose + Hilt + Room 依赖
- [x] 设置项目结构（ui/domain/data/service 分层）
- [x] 配置 KSP（替代 kapt），确保编译通过
- [x] 声明 `FOREGROUND_SERVICE_SPECIAL_USE` 权限
- **产出**：可编译的空项目骨架

### Task 1.2: 数据库设计与实现 ✅

- [x] 定义 `BillEntity`（金额使用 `Long` 存储，单位：分）
- [x] 定义 `BillDao`（CRUD + 统计查询，insert 使用 `OnConflictStrategy.IGNORE`）
- [x] 创建 `AppDatabase`（v2, 5 entities, 5 DAOs）
- [x] 数据库索引配置（timestamp, category, source, 复合索引）
- [ ] ~~编写 Migration 策略~~（当前使用 `fallbackToDestructiveMigration()`，发布前需替换）
- [ ] 单元测试 DAO 查询
- [x] `Converters` 中 Gson 复用（当前为实例属性，建议提取 companion object 单例）
- **产出**：完整的 Room 数据库层 ✅

### Task 1.3: 数据模型与 Repository ✅

- [x] 定义 Domain 层模型（Bill, Category, MonthlyStats，金额均为 Long/分）
- [x] 定义 BillRepository 接口 + 4 个辅助 Repository 接口（Budget/Ledger/MerchantCategory/MonthlyStats）
- [x] 实现 5 个 RepositoryImpl
- [x] Hilt Module 配置
- **产出**：Repository 层可注入使用 ✅

### Phase 1 附加产出

- [x] 额外实体：BudgetEntity, LedgerEntity, MerchantCategoryEntity, MonthlyStatsEntity
- [x] UserPrefs（DataStore 偏好设置）
- [x] PermissionManager（运行时权限管理）

---

## Phase 2: 通知解析引擎 (Week 2) ✅ 已完成

### Task 2.1: 通知解析框架 ✅

- [x] 创建 `NotificationParser` 接口
- [x] 实现 `ParseResult` 密封类（Success/Failure/Ignore，金额使用 Long/分）
- [x] 创建 `ParserRegistry`（按包名路由到对应解析器）
- **产出**：可扩展的解析框架 ✅

### Task 2.2: 微信支付解析器 ✅

- [ ] ~~收集微信支付通知样本（至少 20 种格式）~~
- [x] 实现 `WechatParser`
  - 付款通知解析（「付款」「支付」关键词）
  - 收款通知解析（「收款到账」「到账」）
  - 红包通知解析
  - 转账通知解析
  - 退款识别
- [x] 提取：金额（BigDecimal 元→分）、商户名、交易类型
- [x] 过滤逻辑：忽略零钱通/理财通/零钱提现
- [x] 商户提取：「」引用内容 + "给"后面文本
- [ ] 单元测试（覆盖所有格式变体）
- **产出**：微信通知解析框架就绪，样本覆盖待验证 ⚠️

### Task 2.3: 支付宝解析器 ✅

- [ ] ~~收集支付宝通知样本~~
- [x] 实现 `AlipayParser`
  - 付款通知
  - 转账通知（收入/支出双向）
  - 还款通知
  - 退款通知
- [x] 过滤逻辑：忽略余额宝/余利宝通知
- [ ] 单元测试
- **产出**：支付宝通知解析框架就绪，样本覆盖待验证 ⚠️

### Task 2.4: 银行 App 通知解析器 ✅

- [ ] ~~收集银行 App 推送通知格式~~
- [x] 实现 `BankAppParser`（6 大银行）
  - 工商银行 (com.icbc)
  - 建设银行 (com.chinamworld.main)
  - 农业银行 (com.example.bankabc)
  - 中国银行 (com.boc)
  - 交通银行 (com.bankcomm)
  - 招商银行 (cmb.pb)
- [x] 交易类型：工资/消费/退款/转账/取款/收入
- [ ] 单元测试
- **产出**：6 大银行 App 通知解析框架覆盖 ⚠️

> **注意**：Android 4.4+ 已不允许第三方 App 静默接收 SMS 广播。
> 短信监听已降级为 P2 可选功能，此处仅实现银行 App 通知解析。

### Task 2.5: 智能分类引擎 ✅

- [x] 基于商户名的关键词分类映射表（7 大类别，60+ 关键词）
- [x] `CategoryClassifier` 实现
  - 规则引擎（关键词匹配，置信度 0.7）
  - 支付宝分类字段直接映射（置信度 1.0）
  - ~~历史学习（用户修正过的商户记住分类）~~（MerchantCategory 数据库已就绪，但分类器未注入 DAO）
- [ ] ~~分类置信度评分~~（仅返回固定置信度，未实现动态评分）
- [ ] 单元测试
- **产出**：规则引擎就绪，历史学习未接入 ⚠️

---

## Phase 3: 核心 UI (Week 3) ✅ 已完成

### Task 3.1: 导航框架 ✅

- [x] 设计底部导航（首页/账单/统计/设置）
- [x] Navigation Graph 配置（6 个 destination）
- [x] FAB 快速记账按钮
- [ ] ~~页面切换动画~~（默认动画）
- **产出**：4 Tab 底部导航框架 ✅

### Task 3.2: 首页 ✅

- [x] 今日收支卡片
- [x] 本月收支汇总
- [x] 最近 5 笔账单列表
- [x] 快捷记账 FAB 按钮
- [x] 空状态设计
- [ ] ~~预算进度条~~（未实现）
- **产出**：首页完整可用 ✅

### Task 3.3: 账单列表页 ✅

- [x] 按日期分组的账单列表
- [x] 滑动删除（SwipeToDismissBox）
- [x] 点击进入编辑页
- [x] 顶部月份切换器
- [ ] ~~搜索入口~~（BillDao.search() 已就绪，无搜索 UI）
- [ ] ~~筛选（按分类/金额范围/来源）~~
- **产出**：账单 CRUD 基本完整 ⚠️

### Task 3.4: 手动记账页 ✅

- [x] 金额输入（UI 显示元，内部存储分）
- [x] 收入/支出切换（Switch）
- [x] 分类选择（FlowRow FilterChip）
- [x] 商户名输入
- [x] 日期时间选择（Material3 DatePicker）
- [x] 备注输入
- **产出**：手动记账完整流程 ✅

### Task 3.5: 统计页 ✅

- [x] 月度收支总览（含结余）
- [x] 分类饼图（Canvas 手绘，非 Vico）
- [ ] ~~每日趋势折线图~~（实现了柱状图，非折线图）
- [x] 分类排行列表（金额降序 + 进度条）
- [x] 月份切换
- **产出**：统计分析可视化 ✅

### Task 3.6: 设置页 ✅

- [x] 通知监听权限引导
- [x] 电池优化白名单引导
- [x] 前台服务说明
- [ ] ~~默认分类设置~~（未实现）
- [ ] ~~数据导出入口~~（未实现）
- [x] 关于页面
- **产出**：设置页基本完整 ⚠️

---

## Phase 4: 后台服务与集成 (Week 4) 🔶 部分完成

### Task 4.1: NotificationListenerService ✅

- [x] 实现 `NotificationListenerServiceImpl`（@AndroidEntryPoint + Hilt 注入）
- [x] 解析路由：packageName → ParserRegistry → 对应 Parser
- [x] 解析成功后调用 AddBillUseCase（去重 + 分类 + 插入）
- [x] 去重逻辑（Deduplicator 精确+模糊匹配）
- [ ] ~~异常通知存入待审核列表~~
- **产出**：通知自动记账核心功能 ✅

### Task 4.2: 后台保活 🔶

- [x] 前台 Service KeepAliveService（常驻通知栏，使用 `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`）
- [x] BootReceiver（开机自启动）
- [ ] ~~通知栏显示今日消费~~（当前仅显示固定文本）
- [ ] WorkManager 定时检查（依赖已声明但未使用）
- [ ] 厂商检测工具类
- [ ] 首次启动引导用户完成保活设置
- **产出**：基本保活实现，厂商适配缺失 ⚠️

### Task 4.3: 权限引导流程 🔶

- [x] 通知监听权限检查 + 引导跳转
- [x] 电池优化关闭引导
- [x] PermissionManager 完整实现
- [ ] 各厂商自启动引导（华为/小米/OPPO/vivo）
- [ ] 权限状态检查仪表盘
- [ ] 首次启动引导流程
- **产出**：基本权限引导，厂商适配缺失 ⚠️

### Task 4.4: 集成测试 ❌

- [ ] 端到端测试：通知 → 解析 → 存储 → 显示
- [ ] 微信支付模拟通知测试
- [ ] 支付宝模拟通知测试
- [ ] 边界情况测试（重复通知、格式异常等）
- **产出**：未开始 ❌

---

## Phase 5: 增强功能 (Week 5-6) ❌ 未开始

### Task 5.1: 账单导入引擎 ❌

- [ ] `Importer` 接口定义
- [ ] `WechatCsvImporter`：解析微信账单 CSV
- [ ] `AlipayCsvImporter`：解析支付宝账单 CSV
- [ ] `Deduplicator` 完善（宽松匹配已注释掉）
- [ ] 导入流程 UI
- [ ] 单元测试
- **产出**：未开始 ❌

### Task 5.2: 预算管理 ❌

- [ ] UI 页面（Entity/DAO/Repository 已就绪）
- [ ] 超支通知提醒
- [ ] 首页预算进度条
- **产出**：仅数据层就绪，无 UI ❌

### Task 5.3: 数据导出 ❌

- [ ] CSV 导出
- [ ] 分享到微信/邮件
- **产出**：未开始 ❌

### Task 5.4: 多账本 ❌

- [ ] UI 页面（Entity/DAO/Repository 已就绪）
- [ ] 账本间切换
- **产出**：仅数据层就绪，无 UI ❌

### Task 5.5: 搜索增强 ❌

- [ ] 搜索 UI（BillDao.search() 已就绪）
- [ ] 高级筛选组合
- **产出**：仅 DAO 就绪，无 UI ❌

---

## Phase 6: 财务官AI (Week 7-10) ❌ 未开始

> 详见 `F:\biji\Obbiji\财务官\20-Code-Review\财务官AI-设计方案-2026-05-24.md`

### Phase 6A: 自然语言记账 (Week 7-8) ❌

- [ ] DeepSeek API 客户端封装 (OkHttp + kotlinx.serialization)
- [ ] EncryptedSharedPreferences 存储 API Key
- [ ] AiContextProvider + Prompt 模板系统
- [ ] NlBillParser: 自然语言 → 结构化账单 → AddBillUseCase
- [ ] AddBillScreen 顶部自然语言输入栏
- [ ] 确认卡片 UI (NlBillConfirmDialog)
- [ ] 语音输入集成 (Android SpeechRecognizer)
- [ ] 离线降级: API 不可用时回退到规则引擎
- **产出**: 打字/语音一句话记账 ✅

### Phase 6B: 智能问答 (Week 8-9) ❌

- [ ] ChatScreen + 聊天气泡 UI (支持 Markdown 渲染)
- [ ] AiConversationEntity + DAO (对话历史持久化)
- [ ] AiChatViewModel (管理多轮对话状态)
- [ ] FinancialContext 数据组装 (脱敏财务数据注入 Prompt)
- [ ] 流式响应 SSE (逐字显示 AI 回复)
- [ ] 首页 AI 洞察卡片 (最新洞察摘要 + 入口)
- **产出**: 对话式财务查询 + 首页 AI 入口

### Phase 6C: 分析与洞察 (Week 9-10) ❌

- [ ] AiInsightEntity + DAO
- [ ] 月度报告生成器 (DeepSeek-R1 深度推理)
- [ ] 异常检测引擎 (Z-Score + 同环比 + 重复扣款)
- [ ] WorkManager 周度洞察定时生成
- [ ] 统计页 [AI 分析] 按钮 + 报告展示
- [ ] 预算建议生成 (基于 3 个月历史趋势)
- **产出**: 完整 AI 分析能力

### Phase 6D: 记忆与个性化 (Week 10+) ❌

- [ ] AiMemoryManager (用户偏好学习 + Prompt 注入)
- [ ] 分类别名学习 ("猪脚饭" → 餐饮)
- [ ] 消费习惯对比洞察 ("外卖比上月上升 40%")
- [ ] 个性化省钱建议
- [ ] 多轮对话上下文优化
- **产出**: AI 越用越懂你

### Task 6.5: 桌面小组件 ❌

- [ ] App Widget
- [ ] Glance Compose 实现
- **产出**：未开始 ❌

---

## 里程碑进度

| 里程碑 | 时间 | 状态 | 交付物 |
|--------|------|------|--------|
| M1 - 骨架完成 | Week 1 末 | ✅ 已完成 | 可编译项目 + 数据库 + 5 Entity + 5 DAO + 5 Repository |
| M2 - 解析引擎 | Week 2 末 | ✅ 已完成 | 微信/支付宝/6银行通知解析框架 + 分类器 |
| M3 - MVP 可用 | Week 4 末 | ✅ 基本可用 | 完整记账流程 + 通知自动记录 + 严重问题已修复 |
| M4 - 功能增强 | Week 6 末 | ❌ 未开始 | 预算/导出/多账本 |
| M5 - AI 加持 | Week 8 末 | ❌ 未开始 | AI 分析 + 桌面组件 |

---

## 当前已知问题清单

> 最后更新：2026-05-27 修复 C1-C7 严重问题 + Hilt 双重绑定清理

### 🔴 严重问题（上线前必须修复）

| # | 问题 | 文件 | 状态 | 解决方式 |
|---|------|------|------|----------|
| C1 | 农行假包名 `com.example.bankabc` | BankAppParser.kt | ✅ 已修复 | 改为 `com.android.bankabc` |
| C2 | AiHelper 绕过 DI 直接 new | AiHelper.kt | ✅ 已修复 | 构造注入 CategoryClassifier |
| C3 | UserPrefs/PermissionManager Hilt 双重绑定 | AppModule.kt | ✅ 已修复 | 删除 @Provides，保留 @Inject |
| C4 | ParserRegistry 注入 categoryClassifier 未使用 | ParserRegistry.kt | ✅ 已修复 | 移除未使用的注入 |
| C5 | SettingsViewModel 未接入 UI | SettingsScreen.kt | ✅ 已修复 | 接入 ViewModel，显示权限状态图标 |
| C6 | 通知原文泄漏到 Logcat | NotificationListenerServiceImpl.kt | ✅ 已修复 | 用 BuildConfig.DEBUG 包裹 |
| C7 | Service CoroutineScope 内存泄漏 | NotificationListenerServiceImpl.kt | ✅ 已修复 | SupervisorJob + onDestroy cancel() |
| C8 | Hilt 多类双重绑定 | AppModule.kt | ✅ 已修复 | 删除 7 个冗余 @Provides（ParserRegistry/AddBillUseCase/WechatParser/AlipayParser/BankAppParser/CategoryClassifier/Deduplicator） |
| C9 | PermissionManager.startActivity 崩溃 | PermissionManager.kt | ✅ 已修复 | 添加 FLAG_ACTIVITY_NEW_TASK |
| C10 | 电池优化按钮无效（activity 永远 null） | PermissionManager.kt | ✅ 已修复 | 改用 context.startActivity()，删除 activity 字段 |

### 🟡 功能缺失

| # | 问题 | 文件 | 解决方向 |
|---|------|------|----------|
| M1 | 商户分类历史不落地 | AddBillUseCase.kt:50-51 | 实现 merchantCategoryRepository.saveOrUpdate() |
| M2 | 第三级去重被注释 | Deduplicator.kt:36-41 | 取消注释 + 添加 BillDao.findLooseDuplicate |
| M3 | BankAppParser 优先级 Bug | BankAppParser.kt:32 | `(A \|\| B) && !C` 加括号 |
| M4 | AlipayParser 单字"付"误伤 | AlipayParser.kt | ✅ 已修复：移除单字"付"匹配 |
| M5 | CategoryAmount 暴露在 DAO 文件 | BillDao.kt:12-15 | 移到独立 model |
| M6 | Converters Map 序列化器未使用 | Converters.kt:33-37 | 删除或修复 |
| M7 | AiHelper 未参与分类决策 | AddBillUseCase.kt | 集成 suggestCategory() |
| M8 | billRepository 注入到 Service 未使用 | NotificationListenerServiceImpl.kt | 移除 |
| M9 | KeepAlive 通知不显示实时消费 | KeepAliveService.kt:51 | 更新通知文案为今日消费 |
| M10 | 无厂商保活适配 | — | 华为/小米/OPPO/vivo 自启动引导 |
| M11 | 预算 UI 未实现 | — | Entity/DAO/Repository 已就绪，缺 UI |

### 🔵 技术债务

| # | 问题 | 状态 | 解决方式 |
|---|------|------|----------|
| m1 | 零测试覆盖 | ✅ 已修复 | 添加 45 个单元测试（Parser/Classifier/AmountExtractor） |
| m2 | destructive migration | ✅ 已修复 | 添加 Migration(2,3) |
| m3 | 删除按钮无确认对话框 | ✅ 已修复 | 添加 AlertDialog 确认 |
| m4 | 硬编码中文字符串 | ✅ 已修复 | 提取 40+ 字符串到 strings.xml |
| m5 | BootReceiver 缺导出声明 | ✅ 已修复 | Manifest exported=true |
| m6 | transactionId 查询缺联合索引 | ✅ 已修复 | 添加 Index(["source","transactionId"]) |
| m7 | Gson 实例未复用 | ✅ 已修复 | Converters companion object |
| m8 | 金额提取正则 3 处重复 | ✅ 已修复 | 提取 AmountExtractor 工具类 |
| m9 | foregroundServiceType 未声明 | ✅ 已修复 | Manifest 已有 specialUse |
| m10 | PermissionManager context as Activity | ✅ 已修复 | 移除 Activity 字段，改用 context.startActivity() |
| m11 | AiHelper 返回 Markdown 无渲染 | ✅ 已修复 | 添加 MarkdownText Composable |
| m12 | DAO Provider 未标记 @Singleton | ✅ 已修复 | AppModule 添加注解 |

---

### 🟠 UI/UX 改进（2026-05-24 评审新增）

> 详见 `F:\biji\Obbiji\财务官\20-Code-Review\UI设计改进方案-2026-05-24.md`

| # | 问题 | 解决方向 | 优先级 |
|---|------|----------|--------|
| U1 | 分类图标全用同一 primaryContainer 色 | 提取 CategoryColors 共享映射，每分类独立色彩 | P0 |
| U2 | AddBillScreen 分类用 FilterChip 太简陋 | 改为彩色图标网格 (3-4列) | P0 |
| U3 | 首页卡片布局单调、无层次感 | Hero 卡 + 并排迷你卡 + 预算环 | P1 |
| U4 | 金额字体不够大 | AddBillScreen 金额用 headlineLarge | P1 |
| U5 | 饼图应改为环形图 | drawArc Stroke style + 中心总额 | P1 |
| U6 | 数字无动画 | animateFloatAsState spring 动画 | P1 |
| U7 | 图表无入场动画 | staggered sweepAngle 绘制 | P2 |
| U8 | 账单列表日期不吸顶 | stickyHeader 替换 item | P2 |
| U9 | 滑动删除无撤销 | Snackbar + 撤销按钮 | P2 |
| U10 | 无触觉反馈 | 删除/保存/切换时 HapticFeedback | P2 |
| U11 | 首页预算环未实现 | Entity/DAO 已就绪，补 UI | P1 |
| U12 | 删除按钮无确认对话框 | AlertDialog 确认 | P2 |

---

## 建议下一步工作

1. ✅ ~~修复 C1-C7 严重问题~~（已完成 2026-05-27）
2. **本周**：
   - **UI P0**: 分类色彩系统 + 记账页图标网格（视觉冲击最大）
   - 修复 M1-M4 功能缺失（商户分类历史、三级去重、解析器 Bug）
3. **短期（1-2 周）**：
   - 修复 M5-M8 → KeepAlive 通知显示实时消费
   - **UI P1**: 首页卡片重构 + 环形图 + 数字动画 + 预算环
4. **中期（3-4 周）**：
   - 账单 CSV 导入 → 数据导出 → 厂商保活适配
   - **UI P2**: 吸顶日期 + 撤销删除 + 触觉反馈 + 图表动画
5. **长期（5-6 周）**：AI 接入（DeepSeek）→ 桌面小组件 → 多账本
