-- ----------------------------
-- 业务单据新增页面增量脚本
-- Date: 2026-03-27
-- Scope: sys_menu / sys_role_menu
-- 说明：
-- 1. 业务单据从弹窗新增升级为独立页面。
-- 2. 仍然复用现有 asset:order:add 权限，不新增 permission 字符串。
-- 3. 角色授权直接继承当前已经拥有“单据新增”按钮权限的角色，避免角色口径漂移。
-- ----------------------------

select @asset_order_menu_id := menu_id
from sys_menu
where parent_id = (
  select menu_id
  from sys_menu
  where parent_id = 0 and path = 'asset'
  limit 1
)
  and path = 'order'
limit 1;

-- 独立新增页面：隐藏在菜单树中，只作为动态路由入口，不占左侧菜单位
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '新增单据', @asset_order_menu_id, 11, 'create', 'asset/order/create/index', '', 'AssetOrderCreate',
  1, 0, 'C', '1', '0', 'asset:order:add', 'edit',
  'admin', sysdate(), '', null, '业务单据新增页面'
where @asset_order_menu_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_order_menu_id and path = 'create'
  );

select @asset_order_create_menu_id := menu_id
from sys_menu
where parent_id = @asset_order_menu_id and path = 'create'
limit 1;

select @asset_order_add_menu_id := menu_id
from sys_menu
where parent_id = @asset_order_menu_id and perms = 'asset:order:add'
limit 1;

-- 把新增页面授权给当前已经拥有“单据新增”权限的角色，和既有角色口径保持一致
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
