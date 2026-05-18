# 财务官 - 项目总览

## 项目文档

| 文档 | 路径 | 说明 |
|------|------|------|
| PRD | PRD.md | 产品需求文档 |
| 架构设计 | ARCHITECTURE.md | 技术架构 + 数据模型 + 通知解析策略 |
| 项目路线图 | ROADMAP.md | 6 阶段 30+ 任务的详细规划 |
| Agent 实现指南 | AGENT_GUIDE.md | 每个 Task 的 Agent prompt 模板 |

## 快速开始

### 方式 1：逐个 Task 交给 Agent

按照 AGENT_GUIDE.md 中的 prompt 模板，将每个 Task 依次交给 Agent 执行。

推荐执行顺序：

```text
1.1 → 1.2 → 1.3 (基础层，串行)
→ 2.1 → 2.2 → 2.3 → 2.4 → 2.5 (解析引擎，串行)
→ 3.1 → 3.2-3.6 (UI 层，3.2-3.6 可并行)
→ 4.1 → 4.2 → 4.3 → 4.4 (服务层，串行)
→ 5.1-5.5 (增强功能，可并行)
→ 6.1 → 6.2-6.4 (AI 层) → 6.5 (短信可选)
```

### 方式 2：分 Phase 整体交付

将整个 Phase 作为一次 Agent 任务，提供完整上下文：

```text
Phase 1: 项目基础 (Week 1)
Phase 2: 通知解析引擎 (Week 2)
Phase 3: 核心 UI (Week 3)
Phase 4: 后台服务与集成 (Week 4)
Phase 5: 增强功能 (Week 5-6)
Phase 6: AI 分析 (Week 7-8)
```

## 关键技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 平台 | 仅 Android | iOS 无法读取通知 |
| 语言 | Kotlin 2.0 | K2 编译器性能大幅提升 |
| 注解处理 | KSP | 替代 kapt，编译速度更快，Google 推荐 |
| UI | Jetpack Compose | 现代声明式 UI，开发效率高 |
| 存储 | Room (SQLite) | 纯本地，无需服务端 |
| DI | Hilt | Google 官方推荐，Compose 集成好 |
| 图表 | Vico | Compose 原生，轻量级 |
| AI | DeepSeek API | 免费额度，中文能力强 |
| 保活 | 前台 Service(specialUse) + WorkManager | 兼顾稳定性和电池友好 |
| 金额存储 | Long（分） | 避免 Double 浮点精度问题 |
| 数据导出 | CSV（移除 Apache POI） | 保持 APK < 15MB，POI 体积过大 |

## 已修复的设计缺陷

| 问题 | 修复 |
|------|------|
| Double 存储金额 | 改为 Long（分） |
| DAO OnConflictStrategy.REPLACE | 改为 IGNORE，避免 createdAt 被重置 |
| Gson 每次 new 实例 | 改为单例复用 |
| 短信监听作为 P0 功能 | 降级为 P2 可选（Android 4.4+ 受限） |
| 缺少 Android 14 前台服务类型 | 补充 specialUse 声明 |
| kapt 注解处理 | 改为 KSP |
| security-crypto alpha | 改为 security-crypto-ktx 正式版 |
| Apache POI 体积过大 | 移除，仅保留 CSV 导出 |
| Kotlin/Compose 版本过时 | 升级至 Kotlin 2.0 + Compose BOM 2025.04 |

## 风险与应对

| 风险 | 概率 | 影响 | 应对 |
|------|------|------|------|
| 通知格式变更 | 高 | 中 | 解析器可热更新，正则可配置 |
| 厂商后台限制 | 高 | 高 | 逐厂商适配 + 引导用户完成保活设置 |
| 通知内容截断 | 中 | 中 | 降级为仅记录金额 |
| AI API 限流 | 低 | 低 | 本地统计兜底 |
| 银行 App 通知不推送 | 中 | 高 | 依赖 CSV 导入补录历史数据 |
