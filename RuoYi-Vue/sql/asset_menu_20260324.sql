-- ----------------------------
-- 资产管理菜单初始化脚本
-- Date: 2026-03-24
-- Scope: sys_menu
-- ----------------------------

-- 一级目录
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '资产管理', 0, 20, 'asset', null, '', '',
  1, 0, 'M', '0', '0', '', 'education',
  'admin', sysdate(), '', null, '资产管理目录'
where not exists (
  select 1 from sys_menu where parent_id = 0 and path = 'asset'
);

select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

-- 资产看板
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '资产看板', @asset_root_id, 1, 'dashboard', 'asset/dashboard/index', '', 'AssetDashboard',
  1, 0, 'C', '0', '0', 'asset:dashboard:view', 'dashboard',
  'admin', sysdate(), '', null, '资产看板菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'dashboard'
);

-- 分类管理
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '分类管理', @asset_root_id, 2, 'category', 'asset/category/index', '', 'AssetCategory',
  1, 0, 'C', '0', '0', 'asset:category:list', 'tree',
  'admin', sysdate(), '', null, '分类管理菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'category'
);

select @asset_category_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'category'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '分类查询', @asset_category_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:category:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_category_menu_id and perms = 'asset:category:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '分类新增', @asset_category_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:category:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_category_menu_id and perms = 'asset:category:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '分类修改', @asset_category_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:category:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_category_menu_id and perms = 'asset:category:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '分类删除', @asset_category_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:category:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_category_menu_id and perms = 'asset:category:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '分类导出', @asset_category_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:category:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_category_menu_id and perms = 'asset:category:export');

-- 存放位置
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '存放位置', @asset_root_id, 3, 'location', 'asset/location/index', '', 'AssetLocation',
  1, 0, 'C', '0', '0', 'asset:location:list', 'tree',
  'admin', sysdate(), '', null, '存放位置菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'location'
);

select @asset_location_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'location'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '位置查询', @asset_location_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:location:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_location_menu_id and perms = 'asset:location:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '位置新增', @asset_location_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:location:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_location_menu_id and perms = 'asset:location:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '位置修改', @asset_location_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:location:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_location_menu_id and perms = 'asset:location:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '位置删除', @asset_location_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:location:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_location_menu_id and perms = 'asset:location:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '位置导出', @asset_location_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:location:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_location_menu_id and perms = 'asset:location:export');

-- 资产台账
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '资产台账', @asset_root_id, 4, 'info', 'asset/info/index', '', 'AssetInfo',
  1, 0, 'C', '0', '0', 'asset:info:list', 'example',
  'admin', sysdate(), '', null, '资产台账菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'info'
);

select @asset_info_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'info'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账查询', @asset_info_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账新增', @asset_info_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账修改', @asset_info_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账删除', @asset_info_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账导出', @asset_info_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:export');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '台账导入', @asset_info_menu_id, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:import', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:import');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '查看流水', @asset_info_menu_id, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:viewEvent', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:viewEvent');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '打印标签', @asset_info_menu_id, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:info:printLabel', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_info_menu_id and perms = 'asset:info:printLabel');

-- 业务单据
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '业务单据', @asset_root_id, 5, 'order', 'asset/order/index', '', 'AssetOrder',
  1, 0, 'C', '0', '0', 'asset:order:list', 'form',
  'admin', sysdate(), '', null, '业务单据菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'order'
);

select @asset_order_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'order'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据查询', @asset_order_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据新增', @asset_order_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据修改', @asset_order_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据删除', @asset_order_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据提交', @asset_order_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:submit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:submit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据审批', @asset_order_menu_id, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:approve', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:approve');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据驳回', @asset_order_menu_id, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:reject', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:reject');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据完成', @asset_order_menu_id, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:finish', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:finish');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据取消', @asset_order_menu_id, 9, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:cancel', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:cancel');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '单据导出', @asset_order_menu_id, 10, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:order:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_order_menu_id and perms = 'asset:order:export');

-- 盘点任务
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '盘点任务', @asset_root_id, 6, 'inventory', 'asset/inventory/index', '', 'AssetInventory',
  1, 0, 'C', '0', '0', 'asset:inventory:list', 'clipboard',
  'admin', sysdate(), '', null, '盘点任务菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'inventory'
);

select @asset_inventory_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'inventory'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '盘点查询', @asset_inventory_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '盘点新增', @asset_inventory_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '盘点修改', @asset_inventory_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '盘点删除', @asset_inventory_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '开始盘点', @asset_inventory_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:start', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:start');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '结束盘点', @asset_inventory_menu_id, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:finish', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:finish');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '处理差异', @asset_inventory_menu_id, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:processDiff', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:processDiff');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '盘点导出', @asset_inventory_menu_id, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:inventory:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_inventory_menu_id and perms = 'asset:inventory:export');

-- 资产流水
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '资产流水', @asset_root_id, 7, 'event', 'asset/event/index', '', 'AssetEvent',
  1, 1, 'C', '0', '0', 'asset:event:list', 'history',
  'admin', sysdate(), '', null, '资产流水菜单'
where not exists (
  select 1 from sys_menu where parent_id = @asset_root_id and path = 'event'
);

select @asset_event_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'event'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '流水查询', @asset_event_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:event:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_event_menu_id and perms = 'asset:event:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '流水导出', @asset_event_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:event:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where parent_id = @asset_event_menu_id and perms = 'asset:event:export');
