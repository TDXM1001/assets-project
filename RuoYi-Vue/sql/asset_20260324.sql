-- ----------------------------
-- 资产管理系统初始化脚本
-- Date: 2026-03-24
-- Scope: MVP 核心业务表
-- ----------------------------


-- ----------------------------
-- 1. 资产分类表
-- ----------------------------

drop table if exists asset_category;
create table asset_category (
  category_id bigint(20) not null auto_increment comment '分类ID',
  parent_id bigint(20) default 0 comment '父级ID',
  ancestors varchar(500) default '' comment '祖级路径',
  category_code varchar(64) not null comment '分类编码',
  category_name varchar(100) not null comment '分类名称',
  order_num int(4) default 0 comment '排序号',
  depreciable_flag char(1) default 'N' comment '是否折旧',
  serial_required_flag char(1) default 'N' comment '是否序列号管理',
  borrowable_flag char(1) default 'N' comment '是否可借用',
  inventory_required_flag char(1) default 'Y' comment '是否纳入盘点',
  useful_life_months int(11) default null comment '使用年限(月)',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (category_id),
  unique key uk_asset_category_code (category_code),
  key idx_asset_category_parent (parent_id)
) engine=innodb comment = '资产分类表';

-- ----------------------------
-- 2. 存放位置表
-- ----------------------------

drop table if exists asset_location;
create table asset_location (
  location_id bigint(20) not null auto_increment comment '位置ID',
  parent_id bigint(20) default 0 comment '父级ID',
  ancestors varchar(500) default '' comment '祖级路径',
  location_code varchar(64) not null comment '位置编码',
  location_name varchar(100) not null comment '位置名称',
  location_type varchar(20) default null comment '位置类型',
  dept_id bigint(20) default null comment '部门ID',
  manager_user_id bigint(20) default null comment '负责人ID',
  order_num int(4) default 0 comment '排序号',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (location_id),
  unique key uk_asset_location_code (location_code),
  key idx_asset_location_parent (parent_id),
  key idx_asset_location_dept (dept_id)
) engine=innodb comment = '资产位置表';

-- ----------------------------
-- 3. 资产台账表
-- ----------------------------

drop table if exists asset_info;
create table asset_info (
  asset_id bigint(20) not null auto_increment comment '资产ID',
  asset_code varchar(64) not null comment '资产编码',
  asset_name varchar(200) not null comment '资产名称',
  category_id bigint(20) not null comment '分类ID',
  brand varchar(100) default null comment '品牌',
  model varchar(100) default null comment '型号',
  specification varchar(255) default null comment '规格型号',
  serial_no varchar(100) default null comment '序列号',
  asset_status varchar(20) not null comment '资产状态',
  asset_source varchar(20) default null comment '资产来源',
  use_org_dept_id bigint(20) default null comment '使用部门ID',
  manage_dept_id bigint(20) default null comment '管理部门ID',
  current_user_id bigint(20) default null comment '当前责任人ID',
  current_location_id bigint(20) default null comment '当前位置ID',
  purchase_date date comment '购置日期',
  inbound_date date comment '入库日期',
  start_use_date date comment '启用日期',
  original_value decimal(18,2) default 0.00 comment '原值',
  residual_value decimal(18,2) default 0.00 comment '残值',
  warranty_expire_date date comment '保修到期日',
  supplier_name varchar(200) default null comment '供应商名称',
  qr_code varchar(200) default null comment '二维码',
  version_no int(11) default 0 comment '版本号',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (asset_id),
  unique key uk_asset_info_code (asset_code),
  key idx_asset_info_category (category_id),
  key idx_asset_info_status (asset_status),
  key idx_asset_info_use_dept (use_org_dept_id),
  key idx_asset_info_manage_dept (manage_dept_id),
  key idx_asset_info_user (current_user_id),
  key idx_asset_info_location (current_location_id),
  key idx_asset_info_serial (serial_no)
) engine=innodb comment = '资产台账表';

-- ----------------------------
-- 4. 资产通用业务单据主表
-- ----------------------------

drop table if exists asset_operate_order;
create table asset_operate_order (
  order_id bigint(20) not null auto_increment comment '单据ID',
  order_no varchar(64) not null comment '单据编号',
  order_type varchar(20) not null comment '单据类型',
  source_biz_type varchar(20) default null comment '来源业务类型',
  source_biz_id bigint(20) default null comment '来源业务ID',
  source_biz_no varchar(64) default null comment '来源业务单号',
  order_status varchar(20) not null comment '单据状态',
  biz_date datetime comment '业务日期',
  apply_user_id bigint(20) default null comment '发起人ID',
  apply_dept_id bigint(20) default null comment '发起部门ID',
  approve_user_id bigint(20) default null comment '审批人ID',
  approve_time datetime comment '审批时间',
  approve_result varchar(20) default null comment '审批结果',
  from_dept_id bigint(20) default null comment '来源部门ID',
  to_dept_id bigint(20) default null comment '目标部门ID',
  from_user_id bigint(20) default null comment '来源人ID',
  to_user_id bigint(20) default null comment '目标人ID',
  from_location_id bigint(20) default null comment '来源位置ID',
  to_location_id bigint(20) default null comment '目标位置ID',
  expected_return_date date comment '预计归还日期',
  disposal_reason varchar(500) default null comment '处置原因',
  disposal_amount decimal(18,2) default 0.00 comment '处置金额/残值',
  attachment_count int(11) default 0 comment '附件数量',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (order_id),
  unique key uk_asset_operate_order_no (order_no),
  unique key uk_asset_operate_order_source_link (order_type, source_biz_type, source_biz_id, del_flag),
  key idx_asset_operate_order_type_status (order_type, order_status),
  key idx_asset_operate_order_source_biz (source_biz_type, source_biz_id),
  key idx_asset_operate_order_apply_user (apply_user_id),
  key idx_asset_operate_order_apply_dept (apply_dept_id)
) engine=innodb comment = '资产通用业务单据主表';


-- ----------------------------
-- 5. 资产通用业务单据明细表
-- ----------------------------

drop table if exists asset_operate_order_item;
create table asset_operate_order_item (
  item_id bigint(20) not null auto_increment comment '明细ID',
  order_id bigint(20) not null comment '单据ID',
  asset_id bigint(20) not null comment '资产ID',
  before_status varchar(20) default null comment '变更前状态',
  after_status varchar(20) default null comment '变更后状态',
  before_user_id bigint(20) default null comment '变更前责任人ID',
  after_user_id bigint(20) default null comment '变更后责任人ID',
  before_dept_id bigint(20) default null comment '变更前部门ID',
  after_dept_id bigint(20) default null comment '变更后部门ID',
  before_location_id bigint(20) default null comment '变更前位置ID',
  after_location_id bigint(20) default null comment '变更后位置ID',
  item_status varchar(20) default null comment '明细处理状态',
  item_result varchar(255) default null comment '处理说明',
  primary key (item_id),
  unique key uk_asset_operate_order_item_order_asset (order_id, asset_id),
  key idx_asset_operate_item_order (order_id),
  key idx_asset_operate_item_asset (asset_id)
) engine=innodb comment = '资产通用业务单据明细表';

-- ----------------------------
-- 6. 资产盘点任务表
-- ----------------------------

drop table if exists asset_inventory_task;
create table asset_inventory_task (
  task_id bigint(20) not null auto_increment comment '任务ID',
  task_no varchar(64) not null comment '任务编号',
  task_name varchar(200) not null comment '任务名称',
  task_scope_type varchar(20) not null comment '盘点范围类型',
  task_status varchar(20) not null comment '任务状态',
  target_dept_id bigint(20) default null comment '目标部门ID',
  target_location_id bigint(20) default null comment '目标位置ID',
  target_category_id bigint(20) default null comment '目标分类ID',
  plan_start_time datetime comment '计划开始时间',
  plan_end_time datetime comment '计划结束时间',
  owner_user_id bigint(20) default null comment '任务负责人ID',
  execute_user_id bigint(20) default null comment '执行人ID',
  summary_total int(11) default 0 comment '应盘数量',
  summary_ok int(11) default 0 comment '正常数量',
  summary_diff int(11) default 0 comment '差异数量',
  status char(1) default '0' comment '状态',
  del_flag char(1) default '0' comment '删除标志',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (task_id),
  unique key uk_asset_inventory_task_no (task_no),
  key idx_asset_inventory_task_status (task_status),
  key idx_asset_inventory_task_owner (owner_user_id),
  key idx_asset_inventory_task_execute (execute_user_id)
) engine=innodb comment = '资产盘点任务表';

-- ----------------------------
-- 7. 资产盘点任务明细表
-- ----------------------------

drop table if exists asset_inventory_task_item;
create table asset_inventory_task_item (
  item_id bigint(20) not null auto_increment comment '盘点明细ID',
  task_id bigint(20) not null comment '任务ID',
  asset_id bigint(20) not null comment '资产ID',
  expected_user_id bigint(20) default null comment '账面责任人ID',
  actual_user_id bigint(20) default null comment '现场责任人ID',
  expected_location_id bigint(20) default null comment '账面位置ID',
  actual_location_id bigint(20) default null comment '现场位置ID',
  expected_status varchar(20) default null comment '账面状态',
  actual_status varchar(20) default null comment '现场状态',
  inventory_result varchar(20) not null comment '盘点结果',
  inventory_desc varchar(500) default null comment '差异说明',
  inventory_time datetime comment '盘点时间',
  inventory_user_id bigint(20) default null comment '盘点人ID',
  process_status varchar(20) default null comment '处理状态',
  process_desc varchar(500) default null comment '处理说明',
  primary key (item_id),
  unique key uk_asset_inventory_task_item_task_asset (task_id, asset_id),
  key idx_asset_inventory_item_task (task_id),
  key idx_asset_inventory_item_asset (asset_id),
  key idx_asset_inventory_item_result (inventory_result)
) engine=innodb comment = '资产盘点任务明细表';

-- ----------------------------
-- 8. 资产事件流水表
-- ----------------------------

drop table if exists asset_event_log;
create table asset_event_log (
  event_id bigint(20) not null auto_increment comment '事件ID',
  asset_id bigint(20) not null comment '资产ID',
  event_type varchar(20) not null comment '事件类型',
  source_order_id bigint(20) default null comment '来源单据ID',
  source_order_type varchar(20) default null comment '来源单据类型',
  before_snapshot longtext comment '变更前快照JSON',
  after_snapshot longtext comment '变更后快照JSON',
  event_desc varchar(500) default null comment '事件说明',
  operator_user_id bigint(20) default null comment '操作人ID',
  operate_time datetime comment '操作时间',
  primary key (event_id),
  key idx_asset_event_asset_time (asset_id, operate_time),
  key idx_asset_event_order (source_order_id),
  key idx_asset_event_type (event_type)
) engine=innodb comment = '资产事件流水表';

-- ----------------------------
-- 9. 资产附件表
-- ----------------------------

drop table if exists asset_attachment;
create table asset_attachment (
  attachment_id bigint(20) not null auto_increment comment '附件ID',
  biz_type varchar(20) not null comment '业务类型',
  biz_id bigint(20) not null comment '业务ID',
  file_name varchar(255) not null comment '文件名',
  file_url varchar(500) not null comment '文件地址',
  file_size bigint(20) default 0 comment '文件大小',
  file_type varchar(50) default null comment '文件类型',
  upload_user_id bigint(20) default null comment '上传人ID',
  upload_time datetime comment '上传时间',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (attachment_id),
  key idx_asset_attachment_biz (biz_type, biz_id)
) engine=innodb comment = '资产附件表';
