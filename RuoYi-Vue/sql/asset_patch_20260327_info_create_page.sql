-- ----------------------------
-- 资产台账新增页面增量脚本
-- Date: 2026-03-27
-- Scope: sys_menu / sys_role_menu
-- 说明：
-- 1. 资产台账从弹窗新增升级为独立页面。
-- 2. 新增页面需要挂在资产根目录下，不能挂在“资产台账”菜单子节点下，否则动态路由不会下发。
-- 3. 仍然复用现有 asset:info:add 权限，不新增 permission 字符串。
-- ----------------------------

select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

select @asset_info_create_menu_id := menu_id
from sys_menu
where component = 'asset/info/create/index'
limit 1;

-- 如果旧菜单已经存在，直接修正父级和路由路径，保证 getRouters 能下发
update sys_menu
set parent_id = @asset_root_id,
    order_num = 9,
    path = 'info/create',
    component = 'asset/info/create/index',
    `query` = '',
    route_name = 'AssetInfoCreate',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:info:add',
    icon = 'edit',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '资产台账新增页面'
where @asset_info_create_menu_id is not null
  and menu_id = @asset_info_create_menu_id;

-- 新环境首次执行时，直接插入到资产根目录下
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '新增资产', @asset_root_id, 9, 'info/create', 'asset/info/create/index', '', 'AssetInfoCreate',
  1, 0, 'C', '1', '0', 'asset:info:add', 'edit',
  'admin', sysdate(), '', null, '资产台账新增页面'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'info/create'
  );

select @asset_info_create_menu_id := menu_id
from sys_menu
where component = 'asset/info/create/index'
  and parent_id = @asset_root_id
limit 1;

select @asset_admin_role_id := role_id
from sys_role
where role_key = 'asset_admin'
limit 1;

-- 确保资产管理员能直接访问新增页
insert into sys_role_menu (role_id, menu_id)
select @asset_admin_role_id, @asset_info_create_menu_id
from dual
where @asset_admin_role_id is not null
  and @asset_info_create_menu_id is not null
  and not exists (
    select 1 from sys_role_menu rm
    where rm.role_id = @asset_admin_role_id
      and rm.menu_id = @asset_info_create_menu_id
  );
