-- ----------------------------
-- 资产维修模块增量脚本
-- Date: 2026-03-26
-- Scope: asset_repair_order / sys_menu / sys_role_menu
-- ----------------------------

create table if not exists asset_repair_order (
  repair_id bigint(20) not null auto_increment comment '维修单ID',
  repair_no varchar(64) not null comment '维修单号',
  asset_id bigint(20) not null comment '资产ID',
  asset_code varchar(64) not null comment '资产编码快照',
  asset_name varchar(200) not null comment '资产名称快照',
  repair_status varchar(20) not null comment '维修状态',
  report_time datetime not null comment '报修时间',
  apply_user_id bigint(20) default null comment '发起人ID',
  apply_dept_id bigint(20) default null comment '发起部门ID',
  approve_user_id bigint(20) default null comment '审批人ID',
  approve_time datetime default null comment '审批时间',
  approve_result varchar(20) default null comment '审批结果',
  fault_desc varchar(500) not null comment '故障描述',
  repair_mode varchar(20) default null comment '送修方式',
  vendor_name varchar(200) default null comment '维修供应商',
  repair_cost decimal(12,2) default null comment '维修费用',
  downtime_hours decimal(10,2) default null comment '停用时长(小时)',
  rework_flag char(1) default '0' comment '是否返修',
  send_repair_time datetime default null comment '送修时间',
  finish_time datetime default null comment '完成时间',
  result_type varchar(20) default null comment '完成结果',
  before_status varchar(20) default null comment '维修前状态',
  after_status varchar(20) default null comment '维修后状态',
  attachment_count int(11) default 0 comment '附件数量',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (repair_id),
  unique key uk_asset_repair_order_no (repair_no),
  key idx_asset_repair_asset_id (asset_id),
  key idx_asset_repair_status (repair_status),
  key idx_asset_repair_report_time (report_time)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='资产维修单表';

select @asset_root_id := menu_id
from sys_menu
where parent_id = 0 and path = 'asset'
limit 1;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '维修管理', @asset_root_id, 7, 'repair', 'asset/repair/index', '', 'AssetRepair',
  1, 0, 'C', '0', '0', 'asset:repair:list', 'operation',
  'admin', sysdate(), '', null, '资产维修管理菜单'
where @asset_root_id is not null
  and not exists (
    select 1 from sys_menu where parent_id = @asset_root_id and path = 'repair'
  );

select @asset_repair_menu_id := menu_id
from sys_menu
where parent_id = @asset_root_id and path = 'repair'
limit 1;

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修查询', @asset_repair_menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:query', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修新增', @asset_repair_menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:add', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修修改', @asset_repair_menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:edit', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修删除', @asset_repair_menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:remove', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修提交', @asset_repair_menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:submit', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:submit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修审批', @asset_repair_menu_id, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:approve', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:approve');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修驳回', @asset_repair_menu_id, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:reject', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:reject');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修完成', @asset_repair_menu_id, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:finish', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:finish');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修取消', @asset_repair_menu_id, 9, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:cancel', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:cancel');

insert into sys_menu (menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '维修导出', @asset_repair_menu_id, 10, '#', '', '', '', 1, 0, 'F', '0', '0', 'asset:repair:export', '#', 'admin', sysdate(), '', null, ''
where @asset_repair_menu_id is not null and not exists (select 1 from sys_menu where parent_id = @asset_repair_menu_id and perms = 'asset:repair:export');

select @asset_admin_role_id := role_id from sys_role where role_key = 'asset_admin' limit 1;
select @asset_dept_manager_role_id := role_id from sys_role where role_key = 'asset_dept_manager' limit 1;
select @asset_user_role_id := role_id from sys_role where role_key = 'asset_user' limit 1;
select @asset_auditor_role_id := role_id from sys_role where role_key = 'asset_auditor' limit 1;

insert into sys_role_menu (role_id, menu_id)
select @asset_admin_role_id, m.menu_id
from sys_menu m
where (
  m.path = 'repair'
  or m.perms like 'asset:repair:%'
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_admin_role_id
    and rm.menu_id = m.menu_id
);

insert into sys_role_menu (role_id, menu_id)
select @asset_dept_manager_role_id, m.menu_id
from sys_menu m
where (
  m.path = 'repair'
  or m.perms in (
    'asset:repair:list',
    'asset:repair:query',
    'asset:repair:approve',
    'asset:repair:reject',
    'asset:repair:export'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_dept_manager_role_id
    and rm.menu_id = m.menu_id
);

insert into sys_role_menu (role_id, menu_id)
select @asset_user_role_id, m.menu_id
from sys_menu m
where (
  m.path = 'repair'
  or m.perms in (
    'asset:repair:list',
    'asset:repair:query',
    'asset:repair:add',
    'asset:repair:submit'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_user_role_id
    and rm.menu_id = m.menu_id
);

insert into sys_role_menu (role_id, menu_id)
select @asset_auditor_role_id, m.menu_id
from sys_menu m
where (
  m.path = 'repair'
  or m.perms in (
    'asset:repair:list',
    'asset:repair:query',
    'asset:repair:export'
  )
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.role_id = @asset_auditor_role_id
    and rm.menu_id = m.menu_id
);
