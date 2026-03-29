-- ----------------------------
-- STORY-E009-S001 资产类型底座初始化脚本
-- Date: 2026-03-29
-- Description: 建立资产类型字典，补齐台账表类型字段，并确保旧数据兼容性。
-- ----------------------------

-- 1. 为资产台账表补齐“资产类型”字段
-- 默认设置为 FIXED_ASSET (固定资产)，确保现有 100% 的历史数据逻辑不中断。
ALTER TABLE `asset_info` 
ADD COLUMN `asset_type` varchar(20) NOT NULL DEFAULT 'FIXED_ASSET' COMMENT '资产类型' AFTER `asset_name`;

-- 2. 注册资产类型字典头 (sys_dict_type)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '资产类型', 'asset_type', '0', 'admin', SYSDATE(), '', NULL, '资产大类分区（固定资产、不动产、低值易耗品）'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'asset_type');

-- 3. 注册资产类型字典项 (sys_dict_data)
-- 固定资产 (默认开启)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 1, '固定资产', 'FIXED_ASSET', 'asset_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '标准实物固定资产'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'asset_type' AND `dict_value` = 'FIXED_ASSET');

-- 不动产 (默认开启)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2, '不动产', 'REAL_ESTATE', 'asset_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '土地、房屋等不可移动资产'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'asset_type' AND `dict_value` = 'REAL_ESTATE');

-- 低值易耗品 (默认开启)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 3, '低值易耗品', 'LOW_VALUE', 'asset_type', '', 'info', 'N', '0', 'admin', SYSDATE(), '', NULL, '单价较低、易于损耗的资产'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'asset_type' AND `dict_value` = 'LOW_VALUE');
