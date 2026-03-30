# 不动产独立管理模块实施计划 (Real Estate Independent Module)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个完全独立于固定资产的不动产（房产、土地）管理模块，支持专业字段、独立字典及地图集成。

**Architecture:** 
- **视图层**: 采用 Vue 3 + Element Plus，新建专属视图目录。
- **逻辑层**: 复用资产主表 `asset_info`，但通过 `asset_type` 过滤，并使用 `attr_json` 承载各分类的动态专业字段。
- **组件化**: 将地图选择与分类动态渲染封装为独立组件。

**Tech Stack:** Vue 3, Element Plus, RuoYi-Vue-Plus (useDict), AMap (高德地图).

---

### Task 1: 数据库与字典初始化

**Files:**
- Create: `RuoYi-Vue/sql/asset_patch_20260330_real_estate_dicts.sql`

- [ ] **Step 1: 编写 SQL 脚本定义 `real_estate_status` 字典**
- [ ] **Step 2: 确认 `asset_info` 表包含地理位置字段 (Longitude, Latitude)**
- [ ] **Step 3: 提交 SQL 补丁**

---

### Task 2: 不动产基础组件开发 (地图选择器)

**Files:**
- Create: `art-design-pro/src/views/asset/real-estate/components/RealEstateMap.vue`

- [ ] **Step 1: 实现基于高德地图的静态展示与选点组件**
- [ ] **Step 2: 提交代码**

---

### Task 3: 不动产台账列表页 (`index.vue`)

**Files:**
- Create: `art-design-pro/src/views/asset/real-estate/index.vue`

- [ ] **Step 1: 编写列表页，硬过滤 `asset_type='REAL_ESTATE'`**
- [ ] **Step 2: 配置 `useDict('real_estate_status')` 实现状态回显**
- [ ] **Step 3: 优化表格列展示专业指标 (面积、地址、产证)**
- [ ] **Step 4: 提交代码**

---

### Task 4: 不动产专业编辑器 (`create.vue`)

**Files:**
- Create: `art-design-pro/src/views/asset/real-estate/create/index.vue`

- [ ] **Step 1: 独立的不动产新增/编辑表单布局**
- [ ] **Step 2: 集成地理信息区块**
- [ ] **Step 3: 实现基于分类的动态扩展字段加载 (attr_json)**
- [ ] **Step 4: 提交代码**

---

### Task 5: 存量数据迁移与菜单配置

**Files:**
- Create: `RuoYi-Vue/sql/asset_patch_20260330_real_estate_migration.sql`

- [ ] **Step 1: 编写 SQL 将存量不动产记录迁移至新状态机**
- [ ] **Step 2: 编写 SQL 新增“不动产管理”菜单入口**
- [ ] **Step 3: 提交代码**

---

### Task 6: 清理固资台账中的“房产”逻辑

**Files:**
- Modify: `art-design-pro/src/views/asset/info/create/index.vue`

- [ ] **Step 1: 移除原固资台账中对房产分类的特殊兼容逻辑**
- [ ] **Step 2: 提交代码**
