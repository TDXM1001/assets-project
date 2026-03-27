-- 修复资产状态字典中“维修中”文案乱码
-- 说明：
-- 1. 该字典项在数据库里已经存在，但 dict_label / remark 被写成了乱码。
-- 2. 这里采用“先更新、再补插”的方式，确保老环境和新环境都能落到正确值。

update sys_dict_data
set dict_sort = 9,
    dict_label = '维修中',
    css_class = '',
    list_class = 'warning',
    is_default = 'N',
    status = '0',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '资产状态列表-维修中'
where dict_type = 'asset_status'
  and dict_value = 'REPAIRING';

insert into sys_dict_data
  (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 9, '维修中', 'REPAIRING', 'asset_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '资产状态列表-维修中'
where not exists (
  select 1
  from sys_dict_data
  where dict_type = 'asset_status'
    and dict_value = 'REPAIRING'
);
