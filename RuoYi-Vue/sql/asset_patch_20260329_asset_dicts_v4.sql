-- 6. 单据审批状态 (asset_order_status)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('单据审批状态', 'asset_order_status', '0', 'admin', SYSDATE(), '资产各类申请单的通用审批状态');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (1, '草稿', 'DRAFT', 'asset_order_status', 'info', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (2, '待审批', 'SUBMITTED', 'asset_order_status', 'warning', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (3, '已完成', 'APPROVED', 'asset_order_status', 'success', '0', 'admin', SYSDATE());
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `status`, `create_by`, `create_time`) VALUES (4, '已驳回', 'REJECTED', 'asset_order_status', 'danger', '0', 'admin', SYSDATE());
