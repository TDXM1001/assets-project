-- -------------------------------------------------------------
-- [2026-03-30] 不动产模块独立化 - 终极安装脚本 (Official Final)
-- -------------------------------------------------------------

-- 1. [物理字段补齐] 如果字段已存在，Navicat 可能提示错误，可忽略并运行后续部分
ALTER TABLE `asset_info` ADD COLUMN `longitude` decimal(10, 7) NULL COMMENT '经度';
ALTER TABLE `asset_info` ADD COLUMN `latitude` decimal(10, 7) NULL COMMENT '纬度';
ALTER TABLE `asset_info` ADD COLUMN `addr_detail` varchar(500) NULL COMMENT '详细地址';

-- 2. [字典脚本] 状态字典初始化
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '不动产状态', 'real_estate_status', '0', 'admin', sysdate(), '不动产管理专用状态机' from dual 
where not exists (select 1 from sys_dict_type where dict_type = 'real_estate_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
values 
(1, '空置', 'VACANT', 'real_estate_status', 'info', 'Y', '0', 'admin', sysdate()),
(2, '正在使用', 'IN_USE', 'real_estate_status', 'success', 'N', '0', 'admin', sysdate()),
(3, '对外出租', 'LEASED', 'real_estate_status', 'warning', 'N', '0', 'admin', sysdate()),
(4, '维修改造', 'UNDER_REPAIR', 'real_estate_status', 'danger', 'N', '0', 'admin', sysdate()),
(5, '待处置', 'PENDING_DISPOSAL', 'real_estate_status', 'info', 'N', '0', 'admin', sysdate()),
(6, '已下账', 'DISPOSED', 'real_estate_status', 'default', 'N', '0', 'admin', sysdate());

-- 3. [菜单与路由] 强制重建最新菜单结构
-- 清理之前可能的半成品数据
delete from sys_menu where menu_name in ('不动产管理', '不动产台账', '不动产登记');

-- 安装一级目录 (parentId=0, 类型 M)
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values ('不动产管理', 0, 5, 'real_estate_root', '', '1', '0', 'M', '0', '0', null, 'office', 'admin', sysdate(), '顶级不动产独立入口');

-- 安装页面路由 (类型 C)
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values ('不动产台账', (select menu_id from (select menu_id from sys_menu where menu_name = '不动产管理' and parent_id = 0 limit 1) t), 1, 'info', 'asset/real-estate/index', '1', '0', 'C', '0', '0', 'asset:real-estate:list', 'cascader', 'admin', sysdate(), '独立台账列表');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values ('不动产登记', (select menu_id from (select menu_id from sys_menu where menu_name = '不动产管理' and parent_id = 0 limit 1) t), 2, 'create', 'asset/real-estate/create/index', '1', '0', 'C', '1', '0', 'asset:real-estate:add', 'edit', 'admin', sysdate(), '详情采集页');

-- 4. [存量迁移] 状态机同步迁移逻辑
update `asset_info`
set `asset_status` = case 
    when `asset_status` = 'IDLE' then 'VACANT'
    when `asset_status` in ('USING', 'IN_USE') then 'IN_USE'
    when `asset_status` = 'REPAIR' then 'UNDER_REPAIR'
    when `asset_status` in ('SCRAP', 'SCRAPPED', 'DISPOSED') then 'DISPOSED'
    else 'VACANT'
end
where `asset_type` = 'REAL_ESTATE';
