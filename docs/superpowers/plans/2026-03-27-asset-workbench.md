# 资产单据工作台统一壳 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把资产单据创建、详情、审批和完成统一到同一套工作台壳与上下文流转中，让页面看起来仍属于资产系统，而不是一个割裂的独立页面。

**Architecture:** 采用“共享工作台壳 + 上下文适配层 + 类型展示配置”三层结构。工作台壳负责页面骨架和动作区；上下文适配层负责解析路由参数、草稿键和返回态；类型展示配置负责不同单据类型的标题、说明和默认标签。这样既保留当前动态路由体系，又把复杂流程从 `create/index.vue` 中拆出去。

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vue Router, existing asset module conventions, existing dynamic route/menu system.

---

## 任务 1：抽出单据工作台壳和上下文适配层

**Files:**
- Create: `art-design-pro/src/views/asset/order/modules/order-workbench-context.ts`
- Create: `art-design-pro/src/views/asset/order/modules/order-workbench-shell.vue`
- Modify: `art-design-pro/src/views/asset/order/create/index.vue`

建议先把上下文定义收敛成一个明确的类型，避免创建页里继续散落 route/query/sessionStorage 解析。

```ts
export interface OrderWorkbenchContext {
  orderType: string
  bridgeSource: string
  bridgeKey: string
  sourceBizType: string
  sourceBizId: string
  repairId: string
}

export interface OrderWorkbenchDraftKey {
  orderType: string
  bridgeSource: string
  sourceBizType: string
  sourceBizId: string
  repairId: string
}
```

- [ ] **Step 1: 把上下文解析和草稿 scope 计算抽到独立文件**

实现 `normalizeOrderWorkbenchContext()`、`buildOrderWorkbenchDraftScope()`、`readOrderWorkbenchContextFromStorage()`，只保留一个单一入口给创建页使用。

- [ ] **Step 2: 新增工作台壳组件**

工作台壳负责统一顶部状态区、主内容区和底部动作区，创建页只负责把 `OrderDialog` 和 shell 组装起来。

- [ ] **Step 3: 改造创建页接入壳组件**

把 `art-design-pro/src/views/asset/order/create/index.vue` 里的页面级布局改成壳组件 + 业务内容的组合，保留原有的保存草稿、保存并继续、提交并返回逻辑。

- [ ] **Step 4: 运行前端定点校验**

Run: `pnpm exec eslint src/views/asset/order/create/index.vue src/views/asset/order/modules/order-workbench-context.ts src/views/asset/order/modules/order-workbench-shell.vue`
Expected: PASS

- [ ] **Step 5: 浏览器冒烟**

Run the app and open `#/asset/order/create`
Expected:
- 页面仍然在资产系统布局内
- 顶部、主体、底部动作区结构一致
- 没有跳出系统壳的视觉断裂

- [ ] **Step 6: Commit**

```bash
git add art-design-pro/src/views/asset/order/create/index.vue
git add art-design-pro/src/views/asset/order/modules/order-workbench-context.ts
git add art-design-pro/src/views/asset/order/modules/order-workbench-shell.vue
git commit -m "feat(asset): 抽出单据工作台统一壳"
```

---

## 任务 2：打通上下文回流和草稿隔离

**Files:**
- Modify: `art-design-pro/src/views/asset/order/index.vue`
- Modify: `art-design-pro/src/views/asset/order/create/index.vue`
- Modify: `art-design-pro/src/views/asset/order/modules/order-workbench-context.ts`

- [ ] **Step 1: 统一列表页进入创建页的上下文构造**

把列表页的跳转参数收敛成 `OrderWorkbenchContext`，确保从维修、报废、通用新增进入时使用同一套字段。

- [ ] **Step 2: 统一草稿 key 计算**

草稿键必须包含 `orderType`、`bridgeSource`、`sourceBizType`、`sourceBizId`、`repairId`，避免不同来源互相覆盖。

- [ ] **Step 3: 统一返回态恢复**

返回列表时恢复来源筛选态，并保留当前单据类型，避免用户重新筛选。

- [ ] **Step 4: 运行前端定点校验**

Run: `pnpm exec eslint src/views/asset/order/index.vue src/views/asset/order/create/index.vue src/views/asset/order/modules/order-workbench-context.ts`
Expected: PASS

- [ ] **Step 5: 浏览器冒烟**

Run the app and verify:
- 从列表页进入创建页后能带回原筛选态
- 从维修入口进入后能保留维修上下文
- 草稿恢复不会串到别的来源

- [ ] **Step 6: Commit**

```bash
git add art-design-pro/src/views/asset/order/index.vue
git add art-design-pro/src/views/asset/order/create/index.vue
git add art-design-pro/src/views/asset/order/modules/order-workbench-context.ts
git commit -m "feat(asset): 打通单据工作台上下文回流"
```

---

## 任务 3：单据类型驱动的页面展示配置

**Files:**
- Create: `art-design-pro/src/views/asset/order/modules/order-workbench-config.ts`
- Modify: `art-design-pro/src/views/asset/order/create/index.vue`

建议把标题、说明、默认标签和首屏文案收敛到一个小配置对象里，避免 `create/index.vue` 继续堆 if/else。

```ts
export interface OrderWorkbenchTypeConfig {
  title: string
  description: string
  tags: Array<{ label: string; type: 'info' | 'warning' | 'danger' }>
}
```

- [ ] **Step 1: 建立类型配置映射**

至少覆盖 `INBOUND`、`DISPOSAL`、`TRANSFER`、`RETURN`、`REPAIR` 等当前使用中的单据类型。

- [ ] **Step 2: 替换创建页里的硬编码文案**

把标题、描述和标签逻辑改为读取配置，保持逻辑集中。

- [ ] **Step 3: 运行前端定点校验**

Run: `pnpm exec eslint src/views/asset/order/create/index.vue src/views/asset/order/modules/order-workbench-config.ts`
Expected: PASS

- [ ] **Step 4: 浏览器冒烟**

Run the app and verify:
- 不同单据类型的标题和说明会变化
- 报废单和通用单据的首屏文案不同
- 新类型补配置即可，不需要改壳层结构

- [ ] **Step 5: Commit**

```bash
git add art-design-pro/src/views/asset/order/create/index.vue
git add art-design-pro/src/views/asset/order/modules/order-workbench-config.ts
git commit -m "feat(asset): 单据工作台接入类型配置"
```

---

## 任务 4：同步文档和故事状态

**Files:**
- Modify: `docs/sprint-status.yaml`
- Modify: `docs/bmm-workflow-status.yaml`
- Create: `docs/sprint-plan-asset-workbench-2026-03-27.md`（已完成）
- Create: `docs/stories/story-E008-S001.md`（已完成）
- Create: `docs/stories/story-E008-S002.md`（已完成）
- Create: `docs/stories/story-E008-S003.md`（已完成）

- [ ] **Step 1: 确认 sprint-5 进入待执行状态**

Sprint-5 当前目标是“单据工作台与流程连续性”，三个 story 都是 `todo`。

- [ ] **Step 2: 确认 BMAD 工作流状态仍指向 sprint-status**

保留 `docs/sprint-status.yaml` 为当前实施入口，便于后续继续用 gstack 路线推进。

- [ ] **Step 3: Commit 文档变更**

```bash
git add docs/sprint-plan-asset-workbench-2026-03-27.md
git add docs/stories/story-E008-S001.md
git add docs/stories/story-E008-S002.md
git add docs/stories/story-E008-S003.md
git add docs/sprint-status.yaml
git add docs/bmm-workflow-status.yaml
git commit -m "docs(asset): 规划单据工作台新一轮故事"
```

---

## 计划自检

- 这份计划只覆盖一个产品线：资产单据工作台统一壳和上下文连续性。
- 每个任务都能单独产出可验证的软件片段，不需要等整轮完成才能看结果。
- 没有引入新的数据库脚本，因此当前轮不会触碰增量 SQL。
- 代码职责已经按文件拆开，避免创建页继续成为一个大而全的文件。
