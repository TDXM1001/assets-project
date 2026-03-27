-- ----------------------------
-- 资产业务单据新增页面增量脚本
-- Date: 2026-03-27
-- Scope: sys_menu / sys_role_menu
-- 说明：
-- 1. 业务单据从弹窗新增升级为独立页面。
-- 2. 新增页面需要挂在资产根目录下，不能挂在“业务单据”菜单子节点下，否则动态路由不会下发。
-- 3. 仍然复用现有 asset:order:add 权限，不新增 permission 字符串。
-- 4. 角色授权沿用当前已有“单据新增”权限的角色，保持口径一致。
-- ----------------------------

select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

select @asset_order_create_menu_id := menu_id
from sys_menu
where component = 'asset/order/create/index'
limit 1;

select @asset_order_add_menu_id := menu_id
from sys_menu
where parent_id = (
  select menu_id
  from sys_menu
  where parent_id = @asset_root_id and path = 'order'
  limit 1
)
  and perms = 'asset:order:add'
limit 1;

-- 如果旧菜单已经存在，直接修正父级和路由路径，保证 getRouters 能下发
update sys_menu
set parent_id = @asset_root_id,
    order_num = 11,
    path = 'order/create',
    component = 'asset/order/create/index',
    `query` = '',
    route_name = 'AssetOrderCreate',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:order:add',
    icon = 'edit',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '资产业务单据新增页面'
where @asset_order_create_menu_id is not null
  and menu_id = @asset_order_create_menu_id;

-- 新环境首次执行时，直接插入到资产根目录下
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '新增单据', @asset_root_id, 11, 'order/create', 'asset/order/create/index', '', 'AssetOrderCreate',
  1, 0, 'C', '1', '0', 'asset:order:add', 'edit',
  'admin', sysdate(), '', null, '资产业务单据新增页面'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'order/create'
  );

select @asset_order_create_menu_id := menu_id
from sys_menu
where component = 'asset/order/create/index'
  and parent_id = @asset_root_id
limit 1;

-- 将新页面授权给当前已有“单据新增”权限的角色，保持权限口径一致
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @asset_order_create_menu_id
from sys_role_menu rm
where rm.menu_id = @asset_order_add_menu_id
  and @asset_order_create_menu_id is not null
  and not exists (
    select 1 from sys_role_menu existed
    where existed.role_id = rm.role_id
      and existed.menu_id = @asset_order_create_menu_id
  );
