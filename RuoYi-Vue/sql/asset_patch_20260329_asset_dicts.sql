-- ----------------------------
-- 固定资产字典项补齐脚本 (全覆盖增强版)
-- Date: 2026-03-29
-- Description: 涵盖 DRAFT, PENDING_DISPOSAL 等细颗粒度业务状态。
-- ----------------------------

-- 1. 资产状态 (asset_status)
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'asset_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'asset_status';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('固定资产状态', 'asset_status', '0', 'admin', SYSDATE(), '固定资产全生命周期状态');

-- 全业务状态大表
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (0, '草稿', 'DRAFT', 'asset_status', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '闲置', 'IDLE', 'asset_status', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '在用', 'USING', 'asset_status', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '在用', 'IN_USE', 'asset_status', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '维修中', 'REPAIR', 'asset_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '维修中', 'REPAIRING', 'asset_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '报废', 'SCRAP', 'asset_status', 'danger', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '报废', 'SCRAPPED', 'asset_status', 'danger', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (5, '已处置', 'DISPOSED', 'asset_status', 'danger', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (5, '待处置', 'PENDING_DISPOSAL', 'asset_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (6, '借用中', 'BORROWED', 'asset_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (7, '调拨中', 'IN_TRANSFER', 'asset_status', 'primary', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (8, '盘点中', 'IN_INVENTORY', 'asset_status', 'info', '0', 'admin', SYSDATE());

-- 2. 资产来源 (asset_source)
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'asset_source';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'asset_source';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('固定资产来源', 'asset_source', '0', 'admin', SYSDATE(), '固定资产获取渠道');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '采购', 'PURCHASE', 'asset_source', 'primary', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '自建', 'CONSTRUCT', 'asset_source', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '捐赠', 'DONATION', 'asset_source', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '租赁', 'LEASE', 'asset_source', 'warning', '0', 'admin', SYSDATE());
