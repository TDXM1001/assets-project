-- ----------------------------
-- STORY-RE002 不动产存量数据迁移与菜单安装脚本
-- Date: 2026-03-30
-- Description: 建立动态路由菜单，并将原台账中的不动产数据同步至新状态机。
-- ----------------------------

-- 1. 定位资产管理根目录 (parentId=0, path='asset')
select @asset_root_id := menu_id 
from sys_menu 
where parent_id = 0 and path = 'asset' 
limit 1;

-- 2. 插入“不动产管理”一级菜单 (C 类型)
insert into sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '不动产管理', @asset_root_id, 8, 'real-estate', 'asset/real-estate/index', 'RealEstateIndex', 1, 0, 'C', '0', '0', 'asset:real-estate:list', 'cascader', 'admin', sysdate(), '不动产管理独立模块'
where not exists (select 1 from sys_menu where parent_id = @asset_root_id and path = 'real-estate');

-- 3. 插入“新增不动产”隐藏路由 (用于跳转)
insert into sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '新增不动产', @asset_root_id, 9, 'real-estate/create', 'asset/real-estate/create/index', 'RealEstateCreate', 1, 0, 'C', '1', '0', 'asset:real-estate:add', 'edit', 'admin', sysdate(), '不动产编辑/新增入口'
where not exists (select 1 from sys_menu where parent_id = @asset_root_id and path = 'real-estate/create');

-- 4. 赋予基础按钮权限
select @re_menu_id := menu_id from sys_menu where parent_id = @asset_root_id and path = 'real-estate' limit 1;

-- 导出与删除
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '不动产导出', @re_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:real-estate:export', '#', 'admin', sysdate()
where not exists (select 1 from sys_menu where parent_id = @re_menu_id and perms = 'asset:real-estate:export');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '不动产删除', @re_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:real-estate:remove', '#', 'admin', sysdate()
where not exists (select 1 from sys_menu where parent_id = @re_menu_id and perms = 'asset:real-estate:remove');

-- 5. 存量数据状态机迁移
-- 将 asset_type='REAL_ESTATE' 的旧状态映射到新字典 real_estate_status
-- 核心映射关系: IDLE -> VACANT, USING -> IN_USE, REPAIR -> UNDER_REPAIR, SCRAP -> DISPOSED
update `asset_info`
set `asset_status` = case 
    when `asset_status` = 'IDLE' then 'VACANT'
    when `asset_status` in ('USING', 'IN_USE') then 'IN_USE'
    when `asset_status` = 'REPAIR' then 'UNDER_REPAIR'
    when `asset_status` in ('SCRAP', 'SCRAPPED', 'DISPOSED') then 'DISPOSED'
    else 'VACANT'
end
where `asset_type` = 'REAL_ESTATE';
