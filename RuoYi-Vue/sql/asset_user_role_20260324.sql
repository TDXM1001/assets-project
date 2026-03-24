-- ----------------------------
-- 资产角色分配到测试用户脚本
-- Date: 2026-03-24
-- Scope: sys_user_role
-- 说明:
-- 1. admin 绑定 asset_admin，便于用超级管理员账号直接验证资产模块联调
-- 2. ry 绑定 asset_user，便于验证普通用户在动态菜单模式下的实际可见范围
-- 3. 脚本按用户名查用户 ID，支持重复执行
-- ----------------------------

select @admin_user_id := user_id
from sys_user
where user_name = 'admin'
  and del_flag = '0'
limit 1;

select @ry_user_id := user_id
from sys_user
where user_name = 'ry'
  and del_flag = '0'
limit 1;

select @asset_admin_role_id := role_id
from sys_role
where role_key = 'asset_admin'
limit 1;

select @asset_user_role_id := role_id
from sys_role
where role_key = 'asset_user'
limit 1;

select @asset_dept_manager_role_id := role_id
from sys_role
where role_key = 'asset_dept_manager'
limit 1;

select @asset_auditor_role_id := role_id
from sys_role
where role_key = 'asset_auditor'
limit 1;

-- admin：资产管理员
insert into sys_user_role (user_id, role_id)
select @admin_user_id, @asset_admin_role_id
from dual
where @admin_user_id is not null
  and @asset_admin_role_id is not null
  and not exists (
    select 1
    from sys_user_role ur
    where ur.user_id = @admin_user_id
      and ur.role_id = @asset_admin_role_id
  );

-- ry：资产使用人
insert into sys_user_role (user_id, role_id)
select @ry_user_id, @asset_user_role_id
from dual
where @ry_user_id is not null
  and @asset_user_role_id is not null
  and not exists (
    select 1
    from sys_user_role ur
    where ur.user_id = @ry_user_id
      and ur.role_id = @asset_user_role_id
  );

-- 如果你想让 ry 直接测试审批或盘点差异处理，可以按需放开下面其中一段
--
-- insert into sys_user_role (user_id, role_id)
-- select @ry_user_id, @asset_dept_manager_role_id
-- from dual
-- where @ry_user_id is not null
--   and @asset_dept_manager_role_id is not null
--   and not exists (
--     select 1
--     from sys_user_role ur
--     where ur.user_id = @ry_user_id
--       and ur.role_id = @asset_dept_manager_role_id
--   );
--
-- insert into sys_user_role (user_id, role_id)
-- select @ry_user_id, @asset_auditor_role_id
-- from dual
-- where @ry_user_id is not null
--   and @asset_auditor_role_id is not null
--   and not exists (
--     select 1
--     from sys_user_role ur
--     where ur.user_id = @ry_user_id
--       and ur.role_id = @asset_auditor_role_id
--   );
