-- ----------------------------
-- STORY-RE001 不动产独立模块初始化脚本
-- Date: 2026-03-30
-- Description: 补齐不动产特有物理字段，并初始化专业状态字典。
-- ----------------------------

-- 1. 为资产台账表补齐不动产物理字段
ALTER TABLE `asset_info` 
ADD COLUMN `longitude` decimal(10, 7) DEFAULT NULL COMMENT '经度' AFTER `current_location_id`,
ADD COLUMN `latitude` decimal(10, 7) DEFAULT NULL COMMENT '纬度' AFTER `longitude`,
ADD COLUMN `addr_detail` varchar(500) DEFAULT NULL COMMENT '详细地址/地理位置说明' AFTER `latitude`;

-- 2. 注册“不动产状态”字典头 (sys_dict_type)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '不动产状态', 'real_estate_status', '0', 'admin', SYSDATE(), '', NULL, '不动产管理专用状态（自用、出租、抵押等）'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'real_estate_status');

-- 3. 注册“不动产状态”字典项 (sys_dict_data)
-- 闲置/交付待用 (VACANT)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '闲置/待用', 'VACANT', 'real_estate_status', '', 'info', 'Y', '0', 'admin', SYSDATE(), '交付待用'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'VACANT');

-- 自用中 (IN_USE)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '自用中', 'IN_USE', 'real_estate_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '部门占用'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'IN_USE');

-- 已出租 (LEASED)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '已出租', 'LEASED', 'real_estate_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), '外租获益'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'LEASED');

-- 维修中 (UNDER_REPAIR)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '维修中', 'UNDER_REPAIR', 'real_estate_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), '结构加固/大修'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'UNDER_REPAIR');

-- 待处置 (PENDING_DISPOSAL)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '待处置', 'PENDING_DISPOSAL', 'real_estate_status', '', 'danger', 'N', '0', 'admin', SYSDATE(), '拟拆迁/拟出售'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'PENDING_DISPOSAL');

-- 已处置 (DISPOSED)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '已处置', 'DISPOSED', 'real_estate_status', '', 'info', 'N', '0', 'admin', SYSDATE(), '已退出资产库'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'real_estate_status' AND `dict_value` = 'DISPOSED');
