-- ----------------------------
-- 资产维修页面路由增量脚本
-- Date: 2026-03-27
-- Scope: sys_menu / sys_role_menu
-- 说明：
-- 1. 资产维修从弹窗/抽屉升级为独立页面。
-- 2. 新页面仍挂在资产根目录下，保证动态路由可由后端菜单下发。
-- 3. 复用现有 asset:repair:add / edit / query 权限，不新增 permission 字符串。
-- ----------------------------

select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

select @asset_repair_list_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'repair'
limit 1;

select @asset_repair_add_menu_id := menu_id
from sys_menu
where parent_id = @asset_repair_list_menu_id and perms = 'asset:repair:add'
limit 1;

select @asset_repair_edit_menu_id := menu_id
from sys_menu
where parent_id = @asset_repair_list_menu_id and perms = 'asset:repair:edit'
limit 1;

select @asset_repair_query_menu_id := menu_id
from sys_menu
where parent_id = @asset_repair_list_menu_id and perms = 'asset:repair:query'
limit 1;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '维修新增', @asset_root_id, 12, 'repair/create', 'asset/repair/create/index', '', 'AssetRepairCreate',
  1, 0, 'C', '1', '0', 'asset:repair:add', 'edit',
  'admin', sysdate(), '', null, '资产维修页面新增入口'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'repair/create'
  );

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '维修编辑', @asset_root_id, 13, 'repair/edit/:repairId', 'asset/repair/edit/index', '', 'AssetRepairEdit',
  1, 0, 'C', '1', '0', 'asset:repair:edit', 'edit',
  'admin', sysdate(), '', null, '资产维修页面编辑入口'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'repair/edit/:repairId'
  );

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '维修详情', @asset_root_id, 14, 'repair/detail/:repairId', 'asset/repair/detail/index', '', 'AssetRepairDetail',
  1, 0, 'C', '1', '0', 'asset:repair:query', 'eye',
  'admin', sysdate(), '', null, '资产维修页面详情入口'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'repair/detail/:repairId'
  );

select @asset_repair_create_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'repair/create'
limit 1;

select @asset_repair_edit_page_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'repair/edit/:repairId'
limit 1;

select @asset_repair_detail_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'repair/detail/:repairId'
limit 1;

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @asset_repair_create_menu_id
from sys_role_menu rm
where rm.menu_id = @asset_repair_add_menu_id
  and @asset_repair_create_menu_id is not null
  and not exists (
    select 1 from sys_role_menu existed
    where existed.role_id = rm.role_id
      and existed.menu_id = @asset_repair_create_menu_id
  );

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @asset_repair_edit_page_menu_id
from sys_role_menu rm
where rm.menu_id = @asset_repair_edit_menu_id
  and @asset_repair_edit_page_menu_id is not null
  and not exists (
    select 1 from sys_role_menu existed
    where existed.role_id = rm.role_id
      and existed.menu_id = @asset_repair_edit_page_menu_id
  );

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @asset_repair_detail_menu_id
from sys_role_menu rm
where rm.menu_id = @asset_repair_query_menu_id
  and @asset_repair_detail_menu_id is not null
  and not exists (
    select 1 from sys_role_menu existed
    where existed.role_id = rm.role_id
      and existed.menu_id = @asset_repair_detail_menu_id
  );
