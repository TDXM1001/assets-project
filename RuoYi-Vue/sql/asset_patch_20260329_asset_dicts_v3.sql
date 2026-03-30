-- 维修方式字典
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('资产维修方式', 'asset_repair_mode', '0', 'admin', sysdate(), '资产维修申请方式');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
values (1, '内部维修', 'IN_HOUSE', 'asset_repair_mode', '', 'primary', 'Y', '0', 'admin', sysdate(), '内部维修');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
values (2, '外部送修', 'VENDOR', 'asset_repair_mode', '', 'warning', 'N', '0', 'admin', sysdate(), '外部送修');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
values (3, '上门维修', 'ONSITE', 'asset_repair_mode', '', 'success', 'N', '0', 'admin', sysdate(), '上门维修');
