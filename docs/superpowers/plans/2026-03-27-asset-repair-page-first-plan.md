# 资产维修页面优先实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将资产维修模块从“列表 + 弹层/抽屉混合形态”收口为“页面优先”的标准业务工作流，新增、编辑、详情全部独立页面，并与 `asset/info/create` 统一视觉语言。

**Architecture:** 维修模块保留列表页作为入口与检索层，主流程改为 `create / edit / detail` 三个独立页面。页面共享同一套页面壳、草稿策略、上下文解析和局部浮层能力；主流程不再由弹层或抽屉承载，只保留资产选择器、确认弹层、附件抽屉等局部动作。路由仍保持动态路由体系，不回退到静态路由。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + Vite + `art-design-pro` 现有资产模块组件与 `RuoYi-Vue` 后端接口/SQL。

---

### Task 1: 冻结边界并确认当前页面入口

**Files:**
- Modify: `art-design-pro/src/views/asset/repair/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-dialog.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-detail-drawer.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-approve-dialog.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-finish-dialog.vue`

- [ ] **Step 1: 先写一个最小边界说明，明确 repair 只允许哪些主入口**

```ts
// repair 列表页只负责检索和跳转，主编辑流必须进入独立页面。
// 主流程弹层不再作为新增、编辑、详情的承载体，只保留局部动作弹层。
```

- [ ] **Step 2: 把列表页中的主入口映射梳理清楚**

```ts
// 新增 -> /asset/repair/create
// 编辑 -> /asset/repair/edit/:repairId
// 详情 -> /asset/repair/detail/:repairId
// 列表页只保留搜索、筛选、状态切换、导出和入口跳转。
```

- [ ] **Step 3: 运行 lint 先确认当前入口结构不会报错**

Run: `pnpm exec eslint src/views/asset/repair/index.vue src/views/asset/repair/modules/repair-dialog.vue src/views/asset/repair/modules/repair-detail-drawer.vue src/views/asset/repair/modules/repair-approve-dialog.vue src/views/asset/repair/modules/repair-finish-dialog.vue`
Expected: 通过，或者仅暴露出当前已知的模板/编码问题。

- [ ] **Step 4: 提交这一步的边界整理**

```bash
git add art-design-pro/src/views/asset/repair/index.vue art-design-pro/src/views/asset/repair/modules/repair-dialog.vue art-design-pro/src/views/asset/repair/modules/repair-detail-drawer.vue art-design-pro/src/views/asset/repair/modules/repair-approve-dialog.vue art-design-pro/src/views/asset/repair/modules/repair-finish-dialog.vue
git commit -m "docs(asset): 收紧维修模块页面边界"
```

### Task 2: 建立维修页面壳与路由承载

**Files:**
- Create: `art-design-pro/src/views/asset/repair/create/index.vue`
- Create: `art-design-pro/src/views/asset/repair/edit/index.vue`
- Create: `art-design-pro/src/views/asset/repair/detail/index.vue`
- Modify: `art-design-pro/src/router/modules/asset.ts`
- Modify: `art-design-pro/src/views/asset/repair/index.vue`

- [ ] **Step 1: 以资产新增页为模板写出维修页面壳**

```vue
<template>
  <div class="asset-repair-create-page art-full-height" v-loading="loading">
    <ElCard class="asset-repair-create-page__hero" shadow="never">
      <div class="asset-repair-create-page__hero-main">
        <div>
          <div class="asset-repair-create-page__eyebrow">资产维修单</div>
          <h1 class="asset-repair-create-page__title">{{ pageTitle }}</h1>
          <p class="asset-repair-create-page__desc">{{ pageDescription }}</p>
        </div>
        <ElSpace wrap>
          <ElTag type="warning" effect="light">草稿阶段</ElTag>
          <ElTag v-if="repairId" type="success" effect="light">已载入单据</ElTag>
        </ElSpace>
      </div>
    </ElCard>
  </div>
</template>
```

- [ ] **Step 2: 把 repair 路由改成页面路由**

```ts
// 新增、编辑、详情都用独立页面，不再把主流程挂在抽屉或弹层里。
{
  path: 'repair/create',
  component: () => import('@/views/asset/repair/create/index.vue'),
  name: 'AssetRepairCreate',
  meta: { title: '新增维修单', activeMenu: '/asset/repair' }
}
```

- [ ] **Step 3: 列表页入口改成跳页**

```vue
<ElButton v-auth="'asset:repair:add'" type="primary" @click="handleAdd">
  新增维修单
</ElButton>
```

```ts
const handleAdd = () => {
  router.push('/asset/repair/create')
}
```

- [ ] **Step 4: 运行 lint，确认新的页面壳和路由没有类型问题**

Run: `pnpm exec eslint src/router/modules/asset.ts src/views/asset/repair/index.vue src/views/asset/repair/create/index.vue src/views/asset/repair/edit/index.vue src/views/asset/repair/detail/index.vue`
Expected: 通过。

- [ ] **Step 5: 提交这一步**

```bash
git add art-design-pro/src/router/modules/asset.ts art-design-pro/src/views/asset/repair/index.vue art-design-pro/src/views/asset/repair/create/index.vue art-design-pro/src/views/asset/repair/edit/index.vue art-design-pro/src/views/asset/repair/detail/index.vue
git commit -m "feat(asset): 维修模块改为页面路由"
```

### Task 3: 抽出维修页面上下文与草稿隔离

**Files:**
- Create: `art-design-pro/src/views/asset/repair/modules/repair-page-context.ts`
- Create: `art-design-pro/src/views/asset/repair/modules/repair-draft-storage.ts`
- Modify: `art-design-pro/src/views/asset/repair/create/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/edit/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/detail/index.vue`

- [ ] **Step 1: 统一上下文结构**

```ts
export interface RepairPageContext extends Record<string, any> {
  sourcePage: string
  bridgeSource: string
  bridgeKey: string
  sourceBizType: string
  sourceBizId: string
  assetId: string
  repairId: string
}
```

- [ ] **Step 2: 统一草稿 scope**

```ts
export const buildRepairDraftScope = (context: Record<string, any> = {}) => {
  const sourcePage = String(context.sourcePage || 'manual')
  const bridgeSource = String(context.bridgeSource || 'manual')
  const sourceBizType = String(context.sourceBizType || '')
  const sourceBizId = String(context.sourceBizId || '')
  const assetId = String(context.assetId || '')
  const repairId = String(context.repairId || '')
  return [sourcePage, bridgeSource, sourceBizType, sourceBizId, assetId, repairId].join('|')
}
```

- [ ] **Step 3: 页面从路由和桥接存储读取统一上下文**

```ts
// 页面只认这一份标准化上下文，避免不同入口自己拼各自的参数。
const pageContext = computed<RepairPageContext>(() => normalizeRepairPageContext({
  sourcePage: rawSourcePage.value,
  bridgeSource: rawBridgeSource.value,
  bridgeKey: rawBridgeKey.value,
  sourceBizType: rawSourceBizType.value,
  sourceBizId: rawSourceBizId.value,
  assetId: rawAssetId.value,
  repairId: rawRepairId.value
}))
```

- [ ] **Step 4: 草稿恢复、清空、保存都按 scope 隔离**

```ts
const draftStorageKey = computed(() => buildRepairDraftStorageKey(pageContext.value))
const draftScope = computed(() => buildRepairDraftScope(pageContext.value))
```

- [ ] **Step 5: 运行定点 lint**

Run: `pnpm exec eslint src/views/asset/repair/modules/repair-page-context.ts src/views/asset/repair/modules/repair-draft-storage.ts src/views/asset/repair/create/index.vue src/views/asset/repair/edit/index.vue src/views/asset/repair/detail/index.vue`
Expected: 通过。

- [ ] **Step 6: 提交这一步**

```bash
git add art-design-pro/src/views/asset/repair/modules/repair-page-context.ts art-design-pro/src/views/asset/repair/modules/repair-draft-storage.ts art-design-pro/src/views/asset/repair/create/index.vue art-design-pro/src/views/asset/repair/edit/index.vue art-design-pro/src/views/asset/repair/detail/index.vue
git commit -m "feat(asset): 统一维修页面上下文和草稿隔离"
```

### Task 4: 迁移主单编辑能力到页面

**Files:**
- Create: `art-design-pro/src/views/asset/repair/modules/repair-form-sections.vue`
- Create: `art-design-pro/src/views/asset/repair/modules/repair-asset-selector.vue`
- Create: `art-design-pro/src/views/asset/repair/modules/repair-operations.vue`
- Modify: `art-design-pro/src/views/asset/repair/create/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/edit/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-dialog.vue`

- [ ] **Step 1: 把主表单拆成可复用分区**

```vue
<template>
  <ElCard shadow="never">
    <template #header>基础信息</template>
    <!-- 维修单号、报修时间、维修方式、供应商等 -->
  </ElCard>
</template>
```

- [ ] **Step 2: 把资产选择器抽成局部组件**

```vue
<template>
  <ElDialog v-model="visible" title="选择维修资产" width="1120px" append-to-body destroy-on-close>
    <!-- 资产筛选、资产表格、选择确认 -->
  </ElDialog>
</template>
```

- [ ] **Step 3: 创建页直接使用页面壳，不再依赖主弹层**

```vue
<template>
  <div class="asset-repair-create-page art-full-height">
    <RepairFormSections />
    <RepairAssetSelector />
    <RepairOperations />
  </div>
</template>
```

- [ ] **Step 4: 让 `repair-dialog.vue` 只保留局部复用能力**

```ts
// 这个组件不再承担主流程入口，只保留可以局部复用的表单片段和资产选择能力。
```

- [ ] **Step 5: 运行 lint**

Run: `pnpm exec eslint src/views/asset/repair/modules/repair-dialog.vue src/views/asset/repair/modules/repair-form-sections.vue src/views/asset/repair/modules/repair-asset-selector.vue src/views/asset/repair/modules/repair-operations.vue src/views/asset/repair/create/index.vue src/views/asset/repair/edit/index.vue`
Expected: 通过。

- [ ] **Step 6: 提交这一步**

```bash
git add art-design-pro/src/views/asset/repair/modules/repair-dialog.vue art-design-pro/src/views/asset/repair/modules/repair-form-sections.vue art-design-pro/src/views/asset/repair/modules/repair-asset-selector.vue art-design-pro/src/views/asset/repair/modules/repair-operations.vue art-design-pro/src/views/asset/repair/create/index.vue art-design-pro/src/views/asset/repair/edit/index.vue
git commit -m "feat(asset): 迁移维修主单编辑到页面"
```

### Task 5: 迁移详情页并保留局部动作

**Files:**
- Create: `art-design-pro/src/views/asset/repair/detail/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-detail-drawer.vue`
- Modify: `art-design-pro/src/views/asset/repair/index.vue`

- [ ] **Step 1: 把详情页做成页面本体**

```vue
<template>
  <div class="asset-repair-detail-page art-full-height">
    <ElCard class="asset-repair-detail-page__hero" shadow="never">
      <!-- 单号、状态、摘要 -->
    </ElCard>
    <!-- 详情分区：基础信息、资产明细、审批轨迹、完工结果、附件 -->
  </div>
</template>
```

- [ ] **Step 2: 让列表页的详情入口跳转详情页**

```ts
const handleDetail = (row: any) => {
  router.push(`/asset/repair/detail/${row.repairId}`)
}
```

- [ ] **Step 3: 保留局部动作入口**

```vue
<ElButton v-if="canApprove" type="success" @click="openApproveDialog">
  审批通过
</ElButton>
```

局部动作仍可短暂保留为小弹层，但不再作为主详情承载体。

- [ ] **Step 4: 运行 lint**

Run: `pnpm exec eslint src/views/asset/repair/detail/index.vue src/views/asset/repair/modules/repair-detail-drawer.vue src/views/asset/repair/index.vue`
Expected: 通过。

- [ ] **Step 5: 提交这一步**

```bash
git add art-design-pro/src/views/asset/repair/detail/index.vue art-design-pro/src/views/asset/repair/modules/repair-detail-drawer.vue art-design-pro/src/views/asset/repair/index.vue
git commit -m "feat(asset): 维修详情改为独立页面"
```

### Task 6: 页面统一与浏览器验收

**Files:**
- Modify: `art-design-pro/src/views/asset/repair/create/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/edit/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/detail/index.vue`
- Modify: `art-design-pro/src/views/asset/repair/index.vue`

- [ ] **Step 1: 统一页面头部、卡片间距、底部 sticky 操作栏**

```scss
.asset-repair-create-page,
.asset-repair-edit-page,
.asset-repair-detail-page {
  min-height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background:
    radial-gradient(circle at top right, rgb(17 24 39 / 5%), transparent 35%),
    linear-gradient(180deg, rgb(248 250 252), rgb(255 255 255));
}
```

- [ ] **Step 2: 补齐中文注释和 UTF-8 文案**

```ts
// repair 页面只承载主流程，局部确认和资产选择仍然可以复用小弹层。
```

- [ ] **Step 3: 浏览器冒烟**

Run: `gstack browse`
Expected:
- 新增页、编辑页、详情页都能直接访问
- 页面视觉与 `asset/info/create` 保持一致
- 列表页跳转后保留筛选态
- 草稿恢复正常，不互相串

- [ ] **Step 4: 回收不再使用的旧入口**

清理 `repair-detail-drawer.vue`、`repair-dialog.vue` 中不再被页面使用的主流程逻辑，确保它们只剩局部组件职责。

- [ ] **Step 5: 最终提交**

```bash
git add art-design-pro/src/views/asset/repair/create/index.vue art-design-pro/src/views/asset/repair/edit/index.vue art-design-pro/src/views/asset/repair/detail/index.vue art-design-pro/src/views/asset/repair/index.vue
git commit -m "feat(asset): 完成维修页面优先收口"
```

## Self-Review Notes

- Spec coverage: create / edit / detail 独立页面、列表页只做导航、视觉统一、上下文与草稿隔离、组件边界清晰、局部浮层保留、浏览器验收，均已被 Task 1-6 覆盖。
- Placeholder scan: 未使用 `TODO`、`TBD`、`待定` 之类占位内容。
- Type consistency: 上下文字段在 Task 3 中统一定义，后续任务持续复用同一套字段名。
- Scope check: 当前计划聚焦在维修模块页面优先，不扩展到资产其他模块。
