# 资产模块硬编码字典迁移实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将资产管理模块中的硬编码字典替换为 RuoYi 后端字典系统，实现动态回显。

**Architecture:** 采用 RuoYi 标准的 `useDict` 组合式 API 获取数据，并配合 `DictTag` 组件在表格和详情页中进行动态标签渲染。前端常量数组将被删除，改为从后端字典服务获取。

**Tech Stack:** Vue 3, Vite, RuoYi-Vue-Plus (Element Plus), MySQL

---

### Task 1: 数据库字典初始化

**Files:**
- Create: `RuoYi-Vue/sql/asset_patch_20260329_asset_dicts_v2.sql`

- [x] **Step 1: 编写 SQL 插入脚本**

```sql
-- 1. 盘点范围 (asset_inventory_scope_type)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('盘点范围', 'asset_inventory_scope_type', '0', 'admin', SYSDATE(), '资产盘点任务覆盖范围');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '全量盘点', 'ALL', 'asset_inventory_scope_type', 'primary', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '按部门盘点', 'DEPT', 'asset_inventory_scope_type', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '按位置盘点', 'LOCATION', 'asset_inventory_scope_type', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '按分类盘点', 'CATEGORY', 'asset_inventory_scope_type', 'info', '0', 'admin', SYSDATE());

-- 2. 维修工单状态 (asset_repair_status)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('维修工单状态', 'asset_repair_status', '0', 'admin', SYSDATE(), '资产维修申请单状态流程');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '待提交', 'DRAFT', 'asset_repair_status', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '待审批', 'SUBMITTED', 'asset_repair_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '维修中', 'APPROVED', 'asset_repair_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '已驳回', 'REJECTED', 'asset_repair_status', 'danger', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (5, '已完成', 'FINISHED', 'asset_repair_status', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (6, '已取消', 'CANCELED', 'asset_repair_status', 'info', '0', 'admin', SYSDATE());

-- 3. 维修结果类型 (asset_repair_result_type)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('维修结果类型', 'asset_repair_result_type', '0', 'admin', SYSDATE(), '维修完成后的资产去向');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '恢复在用', 'RESUME_USE', 'asset_repair_result_type', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '转闲置', 'TO_IDLE', 'asset_repair_result_type', 'primary', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '建议报废', 'SUGGEST_DISPOSAL', 'asset_repair_result_type', 'danger', '0', 'admin', SYSDATE());

-- 4. 盘点差异处理状态 (asset_inventory_process_status)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('盘差处理状态', 'asset_inventory_process_status', '0', 'admin', SYSDATE(), '盘点结果差异的处理进度');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '待处理', 'PENDING', 'asset_inventory_process_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '已处理', 'PROCESSED', 'asset_inventory_process_status', 'success', '0', 'admin', SYSDATE());

-- 5. 扩充单据类型 (asset_order_type)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`)
VALUES (20, '盘点任务', 'INVENTORY_TASK', 'asset_order_type', 'primary', '0', 'admin', SYSDATE());
```

- [x] **Step 2: 提交 SQL 补丁**

```bash
git add RuoYi-Vue/sql/asset_patch_20260329_asset_dicts_v2.sql
git commit -m "sql: 新增资产盘点、维修及盘差处理字典项"
```

---

### Task 2: 盘点任务视图重构 (Inventory index.vue)

**Files:**
- Modify: `art-design-pro/src/views/asset/inventory/index.vue`

- [x] **Step 1: 引入字典加载逻辑**

在 `<script setup>` 中添加 `useDict` 调用，并删除硬编码的 `allScopeTypeOptions`。

```javascript
// 修改前
const allScopeTypeOptions = [
  { label: '全量盘点', value: 'ALL' },
  // ...
]

// 修改后
const { asset_inventory_scope_type, asset_inventory_task_status, asset_inventory_result } = useDict(
  'asset_inventory_scope_type',
  'asset_inventory_task_status',
  'asset_inventory_result'
);
```

- [x] **Step 2: 更新表格列定义**

在 `columns` 中使用 `h(DictTag, ...)` 渲染 `taskScopeType`。

```javascript
// 修改 useTable 的 columns 定义
{
  title: '盘点范围',
  dataIndex: 'taskScopeType',
  width: 120,
  customRender: ({ text }) => h(DictTag, { options: asset_inventory_scope_type.value, value: text })
}
```

- [x] **Step 3: 更新搜索表单选项**

```html
<!-- 搜索表单部分 -->
<el-option
  v-for="dict in asset_inventory_scope_type"
  :key="dict.value"
  :label="dict.label"
  :value="dict.value"
/>
```

- [x] **Step 4: 提交**

```bash
git add art-design-pro/src/views/asset/inventory/index.vue
git commit -m "feat(inventory): 盘点范围切换为字典加载"
```

---

### Task 3: 盘差处理弹窗重构 (Inventory Diff Dialog)

**Files:**
- Modify: `art-design-pro/src/views/asset/inventory/modules/inventory-diff-dialog.vue`

- [x] **Step 1: 引入字典逻辑**

```javascript
const { asset_inventory_process_status } = useDict('asset_inventory_process_status');

// 删除硬编码的 processStatusOptions
```

- [x] **Step 2: 更新 UI 回显逻辑**

在差异列表或详情中使用 `DictTag` 展示 `processStatus`。

- [ ] **Step 3: 提交**

```bash
git add art-design-pro/src/views/asset/inventory/modules/inventory-diff-dialog.vue
git commit -m "feat(inventory): 盘差处理状态切换为字典加载"
```

---

### Task 4: 资产维修视图重构 (Repair index.vue)

**Files:**
- Modify: `art-design-pro/src/views/asset/repair/index.vue`

- [x] **Step 1: 引入维修相关字典**

```javascript
const { asset_repair_status, asset_repair_result_type } = useDict(
  'asset_repair_status',
  'asset_repair_result_type'
);

// 删除 statusOptions 和 resultTypeMap
```

- [x] **Step 2: 重构表格列渲染器**

根据 `asset_repair_status` 设置颜色和标签。

```javascript
// useTable 里的 columns
{
  title: '任务状态',
  dataIndex: 'status',
  customRender: ({ text }) => h(DictTag, { options: asset_repair_status.value, value: text })
}
```

- [ ] **Step 3: 提交**

```bash
git add art-design-pro/src/views/asset/repair/index.vue
git commit -m "feat(repair): 维修状态及结果切换为字典加载"
```

---

### Task 5: 维修操作弹窗重构 (Repair Dialogs)

**Files:**
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-dialog.vue`
- Modify: `art-design-pro/src/views/asset/repair/modules/repair-finish-dialog.vue`

- [x] **Step 1: 更新提交弹窗中的状态加载**

- [x] **Step 2: 更新完成弹窗中的结果类型加载**

- [ ] **Step 3: 提交**

```bash
git add art-design-pro/src/views/asset/repair/modules/repair-dialog.vue art-design-pro/src/views/asset/repair/modules/repair-finish-dialog.vue
git commit -m "feat(repair): 维修弹窗选项同步切换为字典"
```

---

### Task 6: 资产流水单据类型扩充 (Event index.vue)

**Files:**
- Modify: `art-design-pro/src/views/asset/event/index.vue`

- [x] **Step 1: 验证字典加载情况**

由于 `Event` 页面已使用 `asset_order_type`，只需确保扩充后的字典项能正常渲染。

- [x] **Step 2: 提交（如有修改）**

---

### 验证步骤
1. 执行 SQL 脚本。
2. 登录后台，在“字典管理”中检查新录入的项。
3. 进入“资产盘点”页面，确认“盘点范围”列显示带颜色的标签。
4. 进入“资产维修”页面，确认“维修状态”显示带颜色的标签。
5. 操作维修完成，确认“维修结果类型”下拉菜单数据非硬编码。
