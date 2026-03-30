# 不动产元数据补丁实施计划 (Real Estate Metadata Implementation Plan)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过 SQL 补丁初始化不动产专属字典与分类模板，激活前端“分类-模板联动”功能。

**Architecture:** 采用 RuoYi 标准的 SQL 补丁模式，在 `asset_category` 表中注入 `asset_type='REAL_ESTATE'` 的分类及配套 JSON 模板。

**Tech Stack:** SQL (MySQL/MariaDB)

---

### Task 1: 初始化系统字典 (SysDict)

**Files:**
- Create: `e:\my-project\assets-project\RuoYi-Vue\sql\asset_patch_20260330_real_estate_metadata_v1.sql`

- [ ] **Step 1: 编写字典初始化 SQL**

```sql
-- 1.1 建筑结构字典 (building_structure)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('建筑结构', 'building_structure', '0', 'admin', SYSDATE(), '不动产管理专用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
VALUES 
(1, '框架结构', 'FRAME', 'building_structure', 'info', 'Y', '0', 'admin', SYSDATE()),
(2, '钢混结构', 'STEEL_CONCRETE', 'building_structure', 'primary', 'N', '0', 'admin', SYSDATE()),
(3, '钢结构', 'STEEL', 'building_structure', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '砖混结构', 'BRICK_CONCRETE', 'building_structure', 'default', 'N', '0', 'admin', SYSDATE());

-- 1.2 土地用途字典 (land_purpose)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('土地用途', 'land_purpose', '0', 'admin', SYSDATE(), '不动产管理专用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
VALUES 
(1, '住宅用地', 'RESIDENTIAL', 'land_purpose', 'success', 'N', '0', 'admin', SYSDATE()),
(2, '商业用地', 'COMMERCIAL', 'land_purpose', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '工业用地', 'INDUSTRIAL', 'land_purpose', 'primary', 'Y', '0', 'admin', SYSDATE()),
(4, '仓储用地', 'WAREHOUSING', 'land_purpose', 'info', 'N', '0', 'admin', SYSDATE());
```

### Task 2: 注入不动产分类与动态模板

**Files:**
- Modify: `e:\my-project\assets-project\RuoYi-Vue\sql\asset_patch_20260330_real_estate_metadata_v1.sql`

- [ ] **Step 1: 编写分类与模板 SQL**

```sql
-- 2.1 办公用房分类及其动态模板
INSERT INTO `asset_category` (`category_code`, `category_name`, `asset_type`, `parent_id`, `field_template_json`, `status`, `create_by`, `create_time`)
VALUES ('RE01', '办公用房', 'REAL_ESTATE', 0, 
'{
  "fields": [
    {"fieldCode": "cert_no", "fieldName": "权属证证号", "componentType": "input", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 1},
    {"fieldCode": "building_area", "fieldName": "建筑面积(㎡)", "componentType": "number", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 2},
    {"fieldCode": "usable_area", "fieldName": "套内面积(㎡)", "componentType": "number", "groupName": "权证信息", "orderNum": 3},
    {"fieldCode": "structure", "fieldName": "建筑结构", "componentType": "select", "dictType": "building_structure", "groupName": "物理指标", "orderNum": 4},
    {"fieldCode": "floors_up", "fieldName": "地上层数", "componentType": "number", "groupName": "物理指标", "orderNum": 5},
    {"fieldCode": "build_year", "fieldName": "建成年代", "componentType": "date", "groupName": "物理指标", "orderNum": 6}
  ]
}', '0', 'admin', SYSDATE());

-- 2.2 工业土地分类及其动态模板
INSERT INTO `asset_category` (`category_code`, `category_name`, `asset_type`, `parent_id`, `field_template_json`, `status`, `create_by`, `create_time`)
VALUES ('RE02', '工业土地', 'REAL_ESTATE', 0, 
'{
  "fields": [
    {"fieldCode": "cert_no", "fieldName": "权属证证号", "componentType": "input", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 1},
    {"fieldCode": "land_expire_date", "fieldName": "土地证到期日", "componentType": "date", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 2},
    {"fieldCode": "land_area", "fieldName": "宗地面积(㎡)", "componentType": "number", "requiredFlag": "1", "groupName": "利用指标", "orderNum": 3},
    {"fieldCode": "land_purpose", "fieldName": "土地用途", "componentType": "select", "dictType": "land_purpose", "groupName": "利用指标", "orderNum": 4}
  ]
}', '0', 'admin', SYSDATE());
```

### Task 3: 后置验证

- [ ] **Step 1: 验证分类筛选逻辑**
刷新 `http://localhost:3006/#/asset/real-estate/create`，确认分类下拉框已出现“办公用房”和“工业土地”。

- [ ] **Step 2: 验证模板联动渲染**
分别选择“办公用房”和“工业土地”，确认下方“扩展字段”区域能够正确加载对应的专业指标字段及分组。
