# 资产类型轻量化前端隔离 (UI Hybrid) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 通过前置增加 Vue 前端 `v-if` 的动态隔离算子机制，隐藏非硬件类（如不动产）资产完全用不到的物理硬件依赖框（品牌/信号/规格等），以极低的代价跨越后端改表风险重构，0 级联崩溃实现闭环。

**Architecture:** 纯前端修改。对涉及主观物理特征的 `ElCol` 或 `ElFormItem` 提取一个核心的 `computed` 计算值 `isHardwareAsset` 并执行判定。

**Tech Stack:** Vue 3, Element Plus.

---

### Task 1: 新增资产页面的硬件字段隔离

**Files:**
- Modify: `art-design-pro/src/views/asset/info/create/index.vue`

**Step 1: 植入计算属性和逻辑控制阀**
```typescript
import { computed } from 'vue'

// 在 `<script setup>` 内声明：只有当非 REAL_ESTATE 时才认定为硬件型实体
const isHardwareAsset = computed(() => {
  return formData.assetType && formData.assetType !== 'REAL_ESTATE'
})
// 衍生：低值易耗品通常也没有维保过期概念
const showWarranty = computed(() => {
  return formData.assetType === 'FIXED_ASSET'
})
```

**Step 2: 包裹硬件属性区块**
定位到 `品牌` (brand), `型号` (model), `规格` (specification), `序列号` (serialNo) 的表单域，对它们或它们的 `ElCol` 外层增加 `v-if="isHardwareAsset"`。
再定位到 `保修截止日期` (warrantyExpireDate)，增加 `v-if="showWarranty"`。

**Step 3: 测试响应式状态**
Run: 当前由于 `vite` 持续运行中（热更新），保存文件后通过界面观察选定非硬件分类如直接切“不动产”时，观察被包裹表单项是否隐藏跳变即可。

### Task 2: 资产单条记录编辑页面的防守重构

**Files:**
- Modify: `art-design-pro/src/views/asset/info/modules/info-dialog.vue`

**Step 1: 镜像逻辑注入**
像 `Task 1` 一样把 `isHardwareAsset` 与 `showWarranty` 拷贝到包含着编辑能力的 `info-dialog.vue` 的 setup 段内。

**Step 2: 对编辑视图里的列再次施加屏蔽保护**
找到和编辑台帐相同的 `品牌`、`型号`、`规格`、`序列号` 和 `保修截止日` 添加对应计算 `v-if`。使得历史保存如果是包含无关垃圾键值，由于 `v-if` 会销毁自身及模型联通，提交时会被 Element Plus 表单忽略，变相实现脏数据清空或冻结。

**Step 3: 整体代码规整与 Commit 提交**
```bash
git add art-design-pro/src/views/asset/info/
git commit -m "feat(ui): 针对资产主表物理字段，增设不同资产类型的强 UI 屏蔽墙"
```
