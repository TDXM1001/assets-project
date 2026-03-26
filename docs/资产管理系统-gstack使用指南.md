# 资产管理系统 gstack / BMAD 使用指南

> 这份文档是给“还不会用”的你看的。目标很简单：知道现在项目走到哪一步、下一步点什么、我会怎么配合你。

## 1. 这套流程是干什么的

gstack 在这个项目里，实际就是按 BMAD 流程把事情分成几层：

1. 先把需求想清楚。
2. 再把文档定下来。
3. 再把架构定下来。
4. 再把开发拆成故事。
5. 最后按故事一项一项实现。

你不用记所有术语，只要记住一句话：

> 先看状态，再选下一步工作。

---

## 2. 你现在处于什么阶段

当前这个资产项目已经走到实施阶段，状态大致是：

- `product-brief` 已完成
- `prd` 已完成
- `architecture` 已完成
- `solutioning-gate-check` 已完成
- `sprint-planning` 已完成
- `docs/sprint-status.yaml` 已生成
- `docs/stories/` 下已经有 10 个故事文件

也就是说，**你现在不是在做规划了，而是在准备按 story 开始开发**。

如果只说最短路径：

1. 先看 `docs/sprint-status.yaml`
2. 再选一个 story
3. 让我跑 `/dev-story`

---

## 3. 你应该先看哪些文件

### 3.1 总状态

- [BMAD 总状态](./bmm-workflow-status.yaml)

这个文件告诉你：

- 哪些流程已经完成
- 哪些流程还没做
- 下一步建议做什么

### 3.2 冲刺状态

- [Sprint 状态](./sprint-status.yaml)

这个文件告诉你：

- 当前有几个 sprint
- 每个 sprint 的目标是什么
- 每个 sprint 里有哪些 story
- 每个 story 的状态是什么

### 3.3 故事文件

- [story-E001-S001](./stories/story-E001-S001.md)
- [story-E001-S002](./stories/story-E001-S002.md)
- [story-E001-S003](./stories/story-E001-S003.md)
- [story-E002-S001](./stories/story-E002-S001.md)
- [story-E002-S002](./stories/story-E002-S002.md)
- [story-E003-S001](./stories/story-E003-S001.md)
- [story-E004-S001](./stories/story-E004-S001.md)
- [story-E004-S002](./stories/story-E004-S002.md)
- [story-E005-S001](./stories/story-E005-S001.md)
- [story-E005-S002](./stories/story-E005-S002.md)

每个 story 文件都可以直接拿来做开发输入。

---

## 4. 你会用到的命令

### 4.1 看状态

- `/workflow-status`

作用：

- 看整个 BMAD 现在到哪一步了
- 看下一步推荐做什么

### 4.2 开始开发前的故事规划

- `/sprint-planning`

作用：

- 把需求拆成 sprint 和 story
- 生成 `docs/sprint-status.yaml`
- 生成 `docs/stories/*.md`

你这个项目现在已经做完了这一步。

### 4.3 在现有冲刺里补一个新故事

- `/create-story`

作用：

- 当现有 story 不够用时，再补一个新 story

适合场景：

- 你发现还有遗漏需求
- 你想把某个大 story 再拆细一点

### 4.4 真正开始实现故事

- `/dev-story`

作用：

- 按 story 文件去实现代码
- 我会根据 story 的验收标准、技术说明和依赖来写代码

这是你现在最可能会用到的命令。

### 4.5 代码审查

- `/code-review`

作用：

- 在某个 story 做完以后，检查代码有没有问题
- 看有没有遗漏测试、权限、边界条件、命名问题

### 4.6 命令速查表

如果你不想看长说明，直接看这张表就够了：

| 场景 | 你该发的命令 | 对应 skill | 说明 |
| --- | --- | --- | --- |
| 看当前流程到哪一步 | `/workflow-status` | `BMad Master` / `bmad-orchestrator` | 先确认现在该做什么 |
| 第一次初始化项目 | `/workflow-init` | `BMad Master` / `bmad-orchestrator` | 生成 BMAD 配置和状态文件 |
| 生成产品简报 | `/product-brief` | `Product Manager` | 需求还比较散时用 |
| 做需求调研 | `/research` | `Product Manager` | 需要补市场或技术背景时用 |
| 生成 PRD | `/prd` | `Product Manager` | 需求已经明确后用 |
| 生成架构 | `/architecture` | `System Architect` | 准备进入开发前用 |
| 做方案门禁检查 | `/solutioning-gate-check` | `System Architect` | 架构和需求再对一次 |
| 拆 sprint 和 story | `/sprint-planning` | `Scrum Master` | 进入实施阶段时用 |
| 追加一个新 story | `/create-story` | `Scrum Master` | 发现漏项或要再拆故事时用 |
| 实现某个 story | `/dev-story STORY-ID` | `Developer` | 最常用，直接开始开发 |
| 代码审查 | `/code-review` | `Developer` | story 做完后收尾 |
| 修测试 | `/fix-tests` | `Developer` | 测试挂了时用 |
| 做重构 | `/refactor` | `Developer` | 功能对了但代码需要整理时用 |

### 4.7 BMAD 核心命令和 gstack 辅助技能

这部分最容易混淆，所以单独拆开：

- **BMAD 核心命令**：产品、架构、冲刺、开发主流程。
- **gstack 辅助技能**：评审、调试、QA、发布、文档收口。

#### BMAD 核心命令

| 命令 | 对应 skill | 说明 |
| --- | --- | --- |
| `/workflow-init` | `BMad Master` / `bmad-orchestrator` | 初始化 BMAD 项目 |
| `/workflow-status` | `BMad Master` / `bmad-orchestrator` | 查看总流程状态 |
| `/product-brief` | `Business Analyst` | 先把需求和问题讲清楚 |
| `/brainstorm` | `Business Analyst` | 发散思考和方案收敛 |
| `/research` | `Business Analyst` | 补充调研和背景信息 |
| `/prd` | `Product Manager` | 生成 PRD |
| `/tech-spec` | `Product Manager` | 生成技术规格 |
| `/create-ux-design` | `UX Designer` | 生成 UX 设计内容 |
| `/architecture` | `System Architect` | 生成架构文档 |
| `/solutioning-gate-check` | `System Architect` | 架构门禁检查 |
| `/sprint-planning` | `Scrum Master` | 拆 sprint 和 story |
| `/create-story` | `Scrum Master` | 追加一个 story |
| `/dev-story STORY-ID` | `Developer` | 按 story 实现代码 |
| `/code-review` | `Developer` | 做代码审查 |
| `/fix-tests` | `Developer` | 修测试 |
| `/refactor` | `Developer` | 做重构 |

#### gstack 辅助技能

| 命令 | 对应 skill | 说明 |
| --- | --- | --- |
| `/gstack-office-hours` | `office-hours` | 需求想法、方向、可行性讨论 |
| `/gstack-plan-eng-review` | `plan-eng-review` | 实现前做工程方案评审 |
| `/gstack-plan-design-review` | `plan-design-review` | 实现前做设计方案评审 |
| `/gstack-design-review` | `design-review` | 页面视觉和交互 QA |
| `/gstack-investigate` | `investigate` | 系统性排查 bug 根因 |
| `/gstack-qa` | `qa` | 需要边测边修时用 |
| `/gstack-qa-only` | `qa-only` | 只出 QA 报告，不修代码 |
| `/gstack-review` | `review` | 合并前做 PR 代码审查 |
| `/gstack-autoplan` | `autoplan` | 一次性跑完多轮 plan review |
| `/gstack-ship` | `ship` | 打包、发 PR、准备发布 |
| `/gstack-land-and-deploy` | `land-and-deploy` | 合并并验证部署 |
| `/gstack-setup-deploy` | `setup-deploy` | 先把部署配置接好 |
| `/gstack-document-release` | `document-release` | 发版后同步文档 |

> 记忆法：
> - BMAD 主线先看 `/workflow-status`，再进 `/prd`、`/architecture`、`/sprint-planning`、`/dev-story`
> - gstack 辅助主要用在评审、QA、发布、调试

---

## 5. 你现在最简单的用法

如果你现在还是完全不会用，就按这个顺序来：

1. 打开 [Sprint 状态](./sprint-status.yaml)
2. 选一个 `todo` 的 story
3. 把 story 文件发给我，或者直接说 story 编号
4. 我帮你跑 `/dev-story`
5. 做完后我再帮你做 `/code-review`

你也可以直接说：

- “先做 `STORY-E001-S001`”
- “继续下一个 story”
- “帮我检查这个 story 能不能开工”

---

## 6. 这个项目的推荐顺序

我已经按优先级给你排好了，建议按下面顺序走：

### Sprint 1

1. 资产分类管理
2. 存放位置管理
3. 资产台账管理
4. 入库与领用单据闭环
5. 调拨、归还与报废单据闭环

### Sprint 2

1. 盘点任务与差异处理
2. 资产事件流水
3. 资产运营看板
4. 附件能力
5. 导入导出与权限收口

这个顺序的好处是：

- 先打地基
- 再跑主流程
- 再补盘点和看板
- 最后做收口

---

## 7. 你需要记住的三个原则

### 原则 1：先看状态，不要自己猜

先看：

- `docs/bmm-workflow-status.yaml`
- `docs/sprint-status.yaml`

不要凭感觉直接开工。

### 原则 2：先做小 story，不要一口吃成胖子

一个 story 最好能在 1 到 3 天内完成。

如果一个 story 看起来太大，就要拆。

### 原则 3：story 是开发的唯一入口

后面你要我写代码，最好只给我两样东西：

- story 编号
- story 文件

我会按照 story 的：

- 用户故事
- 验收标准
- 技术说明
- 依赖

来做实现。

---

## 8. 这套流程里你最容易搞混的地方

### 8.1 `bmm-workflow-status.yaml` 和 `sprint-status.yaml` 的区别

- `bmm-workflow-status.yaml`：BMAD 总流程状态
- `sprint-status.yaml`：实施阶段的 sprint 和 story 状态

你可以把前者理解成“总导航”，后者理解成“施工清单”。

### 8.2 story 文件和代码的关系

story 文件不是代码，是开发说明书。

里面会告诉你：

- 要做什么
- 怎么算做完
- 有哪些依赖
- 先做哪一部分

### 8.3 我现在能帮你做什么

你现在可以直接让我做这几件事：

- 继续跑下一个 story
- 帮你把某个 story 再拆细
- 帮你检查当前 story 是否能开工
- 帮你做代码实现和审查

---

## 9. 如果你只记一句话

> 先看 `sprint-status.yaml`，再选一个 `todo` story，然后让我跑 `/dev-story`。

这就是你现在的最短上手方式。
