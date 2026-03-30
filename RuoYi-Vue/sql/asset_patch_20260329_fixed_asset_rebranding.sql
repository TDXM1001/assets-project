-- ----------------------------
-- 资产管理系统品牌化更新 (资产 -> 固定资产)
-- Date: 2026-03-29
-- Description: 在不改变底层代码逻辑的情况下，将所有前端可见的“资产”标签更新为“固定资产”。
-- ----------------------------

-- 1. 更新菜单名称 (sys_menu)
UPDATE sys_menu SET menu_name = '固定资产管理' WHERE menu_name = '资产管理' AND parent_id = 0;
UPDATE sys_menu SET menu_name = '固定资产看板' WHERE menu_name = '资产看板';
UPDATE sys_menu SET menu_name = '固定资产台账' WHERE menu_name = '资产台账';
UPDATE sys_menu SET menu_name = '固定资产流水' WHERE menu_name = '资产流水';

-- 2. 更新表注释 (用于各数据库管理工具显示)
ALTER TABLE `asset_info` COMMENT = '固定资产台账表';
ALTER TABLE `asset_category` COMMENT = '固定资产分类表';
ALTER TABLE `asset_operate_order` COMMENT = '固定资产通用业务单据主表';
ALTER TABLE `asset_operate_order_item` COMMENT = '固定资产通用业务单据明细表';
ALTER TABLE `asset_inventory_task` COMMENT = '固定资产盘点任务表';
ALTER TABLE `asset_inventory_task_item` COMMENT = '固定资产盘点任务明细表';
ALTER TABLE `asset_event_log` COMMENT = '固定资产事件流水表';
ALTER TABLE `asset_attachment` COMMENT = '固定资产附件表';

-- 3. 更新字典项名称 (sys_dict_type)
UPDATE sys_dict_type SET dict_name = '固定资产分类定义' WHERE dict_type = 'asset_category_type' OR dict_name = '资产分类类型';
UPDATE sys_dict_type SET dict_name = '固定资产类型' WHERE dict_type = 'asset_type';
