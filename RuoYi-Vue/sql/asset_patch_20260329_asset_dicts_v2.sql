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
