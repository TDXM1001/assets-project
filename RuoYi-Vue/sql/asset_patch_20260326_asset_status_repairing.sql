-- 为资产状态字典补充“维修中”，支撑归还单把资产回写到维修相关状态

insert into sys_dict_data
  (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 9, '维修中', 'REPAIRING', 'asset_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '资产状态列表-维修中'
where not exists (
  select 1
  from sys_dict_data
  where dict_type = 'asset_status'
    and dict_value = 'REPAIRING'
);
