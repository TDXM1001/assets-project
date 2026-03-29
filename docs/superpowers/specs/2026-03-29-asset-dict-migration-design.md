# 资产管理模块硬编码字典迁移设计文档

## 1. 背景
资产管理系统中多个 Vue 页面（盘点、维修、资产变更等）存在硬编码的 Label/Value 字典数组。为了提高系统的可维护性和一致性，现计划将其迁移至 RuoYi 后端字典管理系统（`sys_dict_type` 和 `sys_dict_data`），并统一使用 `useDict` 钩子和 `DictTag` 组件进行回显。

## 2. 迁移范围

### 2.1 盘点模块 (Asset Inventory)
- **硬编码位置**: `inventory/index.vue`
- **目标字典**: `asset_inventory_scope_type` (盘点范围)
- **选项内容**:
    - ALL: 全量盘点
    - DEPT: 按部门盘点
    - LOCATION: 按位置盘点
    - CATEGORY: 按分类盘点

### 2.2 维修模块 (Asset Repair)
- **硬编码位置**: `repair/index.vue`, `repair/modules/repair-dialog.vue`
- **目标字典 1**: `asset_repair_status` (维修状态)
    - DRAFT: 待提交 (info)
    - SUBMITTED: 待审批 (warning)
    - APPROVED: 维修中 (warning)
    - REJECTED: 已驳回 (danger)
    - FINISHED: 已完成 (success)
    - CANCELED: 已取消 (info)
- **目标字典 2**: `asset_repair_result_type` (维修结果类型)
    - RESUME_USE: 恢复在用
    - TO_IDLE: 转闲置
    - SUGGEST_DISPOSAL: 建议报废

### 2.3 盘点差异处理 (Inventory Diff)
- **硬编码位置**: `inventory/modules/inventory-diff-dialog.vue`
- **目标字典**: `asset_inventory_process_status` (差异处理状态)
- **选项内容**:
    - PENDING: 待处理
    - PROCESSED: 已处理

### 2.4 资产流水 (Asset Event)
- **目标字典扩充**: `asset_order_type`
- **扩充项**:
    - INVENTORY_TASK: 盘点任务

## 3. 实现计划

### 阶段 1: 数据库初始化 (SQL)
创建新的 SQL 补丁文件 `asset_patch_20260329_asset_dicts_v2.sql`，插入 `sys_dict_type` 和 `sys_dict_data` 数据。

### 阶段 2: 前端重构
1. **盘点模块**:
    - 修改 `inventory/index.vue`，引入 `asset_inventory_scope_type`。
    - 替换 `allScopeTypeOptions` 常量。
    - 在表格列中使用 `DictTag` 回显。
2. **维修模块**:
    - 修改 `repair/index.vue`，引入 `asset_repair_status` 和 `asset_repair_result_type`。
    - 替换 `statusOptions` 和 `resultTypeMap`。
    - 重构 `useTable` 的 `columns` 定义，使用 `h(DictTag, ...)`。
3. **弹窗组件**:
    - 同步更新 `inventory-diff-dialog.vue`、`repair-dialog.vue` 和 `repair-finish-dialog.vue` 中的选项加载逻辑。

## 4. 验证要点
1. 列表页状态颜色是否正确（使用 `DictTag` 的 `list_class`）。
2. 搜索表单的下拉选项是否能正常加载。
3. 详情弹窗和新建弹窗中的字典项逻辑是否保持一致。

## 5. 结论
通过本次迁移，资产模块将完全对齐 RuoYi 的字典驱动模式，消除前端代码中的硬编码字符，支持通过后台管理系统动态调整标签。
