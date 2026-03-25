-- ----------------------------
-- 资产管理角色与授权初始化脚本
-- Date: 2026-03-24
-- Scope: sys_role / sys_role_menu / sys_role_dept
-- ----------------------------

-- 新增角色
insert into sys_role (
  role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  '资产管理员', 'asset_admin', 100, '1', 1, 1,
  '0', '0', 'admin', sysdate(), '', null, '资产模块管理员'
where not exists (
  select 1 from sys_role where role_key = 'asset_admin'
);

insert into sys_role (
  role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  '部门主管(资产)', 'asset_dept_manager', 101, '4', 1, 1,
  '0', '0', 'admin', sysdate(), '', null, '资产模块部门主管角色'
where not exists (
  select 1 from sys_role where role_key = 'asset_dept_manager'
);

insert into sys_role (
  role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  '资产使用人', 'asset_user', 102, '5', 1, 1,
  '0', '0', 'admin', sysdate(), '', null, '资产模块普通使用人角色'
where not exists (
  select 1 from sys_role where role_key = 'asset_user'
);

insert into sys_role (
  role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  '财务审计(资产)', 'asset_auditor', 103, '1', 1, 1,
  '0', '0', 'admin', sysdate(), '', null, '资产模块财务审计角色'
where not exists (
  select 1 from sys_role where role_key = 'asset_auditor'
);

select @asset_admin_role_id := role_id from sys_role where role_key = 'asset_admin' limit 1;
select @asset_dept_manager_role_id := role_id from sys_role where role_key = 'asset_dept_manager' limit 1;
select @asset_user_role_id := role_id from sys_role where role_key = 'asset_user' limit 1;
select @asset_auditor_role_id := role_id from sys_role where role_key = 'asset_auditor' limit 1;

-- 回查资产菜单根节点
select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

-- 资产管理员：全模块授权
insert into sys_role_menu (role_id, menu_id)
select @asset_admin_role_id, m.menu_id
from sys_menu m
where (
  m.menu_id = @asset_root_id
  or m.parent_id = @asset_root_id
  or m.perms like 'asset:%'
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_admin_role_id
    and rm.menu_id = m.menu_id
);

-- 部门主管：查看、审批、差异处理
insert into sys_role_menu (role_id, menu_id)
select @asset_dept_manager_role_id, m.menu_id
from sys_menu m
where (
  m.menu_id = @asset_root_id
  or m.path in ('dashboard', 'info', 'order', 'inventory', 'event')
  or m.perms in (
    'asset:dashboard:view',
    'asset:info:list',
    'asset:info:query',
    'asset:order:list',
    'asset:order:query',
    'asset:order:approve',
    'asset:order:reject',
    'asset:order:export',
    'asset:inventory:list',
    'asset:inventory:query',
    'asset:inventory:processDiff',
    'asset:event:list',
    'asset:event:query'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_dept_manager_role_id
    and rm.menu_id = m.menu_id
);

-- 资产使用人：我的台账、我的单据
insert into sys_role_menu (role_id, menu_id)
select @asset_user_role_id, m.menu_id
from sys_menu m
where (
  m.menu_id = @asset_root_id
  or m.path in ('dashboard', 'info', 'order')
  or m.perms in (
    'asset:dashboard:view',
    'asset:info:list',
    'asset:info:query',
    'asset:order:list',
    'asset:order:query',
    'asset:order:add',
    'asset:order:submit'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_user_role_id
    and rm.menu_id = m.menu_id
);

-- 财务审计：只读与导出
insert into sys_role_menu (role_id, menu_id)
select @asset_auditor_role_id, m.menu_id
from sys_menu m
where (
  m.menu_id = @asset_root_id
  or m.path in ('info', 'order', 'event')
  or m.perms in (
    'asset:info:list',
    'asset:info:query',
    'asset:info:export',
    'asset:order:list',
    'asset:order:query',
    'asset:order:export',
    'asset:event:list',
    'asset:event:query',
    'asset:event:export'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_auditor_role_id
    and rm.menu_id = m.menu_id
);

-- 如需给资产管理员配置自定义部门范围，请按真实 dept_id 手工执行下面语句。
-- 前提：asset_admin 的 data_scope 改为 '2'
--
-- insert into sys_role_dept (role_id, dept_id)
-- select @asset_admin_role_id, d.dept_id
-- from sys_dept d
-- where d.dept_id in (100, 101, 102)
-- and not exists (
--   select 1 from sys_role_dept rd
--   where rd.role_id = @asset_admin_role_id
--     and rd.dept_id = d.dept_id
-- );
