# 不动产模块前端样式对齐实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将不动产模块（列表页与编辑页）的视觉风格、组件布局和交互模式，与系统现有的固定资产模块完全对齐，实现 1:1 的样式克隆与专业化提升。

**Architecture:** 
- **布局层**：复用 `art-full-height` 和 `asset-info-layout` 样式类。
- **组件层**：通过自定义插槽（Slot）强化 `ElCard` 头部，整合 `InfoFilterTree` 侧边栏。
- **配置层**：在 `useTable` 勾子中强制注入展示逻辑。

**Tech Stack:** Vue 3, Element Plus, TypeScript, Tailwind CSS.

---

### Task 1: 列表页布局重构 (引入侧边过滤树)

**Files:**
- Modify: `src/views/asset/real-estate/index.vue`

- [ ] **Step 1: 调整 Template 结构**
  引入 `aside` 区域和 `InfoFilterTree` 组件。
  ```vue
  <div class="real-estate-layout">
    <aside class="asset-info-aside">
      <InfoFilterTree
        ref="treeRef"
        :mode="filterMode"
        :data="locationTreeOptions"
        @node-click="handleFilterNodeClick"
      />
    </aside>
    <div class="real-estate-main">
      <!-- 搜索栏与表格 -->
    </div>
  </div>
  ```

- [ ] **Step 2: 注入样式类**
  确保 `.real-estate-layout` 和 `.real-estate-main` 的 CSS 属性与 `asset/info` 一致（特别是 flex 占比和 overflow 处理）。

- [ ] **Step 3: 实现树节点过滤逻辑**
  在 `index.vue` 中添加 `handleFilterNodeClick` 方法，点击树节点时自动更新 `searchParams` 并触发搜索。

- [ ] **Step 4: 提交代码**
  ```bash
  git add src/views/asset/real-estate/index.vue
  git commit -m "style: implement list page sidebar and layout alignment"
  ```

---

### Task 2: 编辑页 Hero 区域与样式类对标

**Files:**
- Modify: `src/views/asset/real-estate/create/index.vue`

- [ ] **Step 1: 升级 Hero 头部文案与结构**
  ```vue
  <ElCard class="asset-info-create-page__hero" shadow="never">
    <div class="asset-info-create-page__hero-main">
      <div>
        <div class="asset-info-create-page__eyebrow">不动产登记</div>
        <h1 class="asset-info-create-page__title">{{ pageTitle }}</h1>
        <p class="asset-info-create-page__desc">先把不动产主档录稳，后续单据、事件和附件都沿用这套基础字段继续流转。</p>
      </div>
    </div>
  </ElCard>
  ```

- [ ] **Step 2: 统一外层容器类名**
  将根节点 class 改为 `asset-info-create-page`，以复用全局 SCSS 变量。

- [ ] **Step 3: 提交代码**
  ```bash
  git add src/views/asset/real-estate/create/index.vue
  git commit -m "style: upgrade hero header and align container classes"
  ```

---

### Task 3: 表单卡片 (Section Cards) 深度改版

**Files:**
- Modify: `src/views/asset/real-estate/create/index.vue`

- [ ] **Step 1: 重构卡片 Header 插槽**
  为每一个 `ElCard` 添加以下结构，替换现有的简单标题。
  ```vue
  <template #header>
    <div class="asset-info-create-page__section-header">
      <div>
        <div class="asset-info-create-page__section-title">基础信息</div>
        <div class="asset-info-create-page__section-subtitle">
          描述不动产的核心地理及分类属性，确权的基础。
        </div>
      </div>
    </div>
  </template>
  ```

- [ ] **Step 2: 规范各板块描述**
  - **归属信息**：填充“确权到人、责任到位的关键字段”描述。
  - **财务信息**：填充“用于价值追踪与财务折旧的核算依据”描述。

- [ ] **Step 3: 提交代码**
  ```bash
  git add src/views/asset/real-estate/create/index.vue
  git commit -m "style: implement immersive section card headers for form"
  ```

---

### Task 4: 底部操作栏标准化与地图整合

**Files:**
- Modify: `src/views/asset/real-estate/create/index.vue`
- Modify: `src/views/asset/real-estate/components/RealEstateMap.vue`

- [ ] **Step 1: 统一底部操作栏布局**
  确保按钮顺序为：[返回, 保存, 保存并继续]，且容器带有 `fixed-bottom` 类。

- [ ] **Step 2: 将地图嵌入到专门的地理信息卡片中**
  创建一个名为“地理与座落”的 `ElCard`，将经纬度录入与 `RealEstateMap` 组件水平/垂直排布，对标固定资产的“资产照片”位置感。

- [ ] **Step 3: 最终自检**
  运行 `npm run lint` 确保格式无误，并在浏览器中核对样式。

- [ ] **Step 4: 提交代码**
  ```bash
  git add src/views/asset/real-estate/create/index.vue
  git commit -m "style: standardize action bar and integrate spatial card"
  ```
