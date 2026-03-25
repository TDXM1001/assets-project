-- ----------------------------
-- 璧勪骇绠＄悊绯荤粺鍒濆鍖栬剼鏈?-- Date: 2026-03-24
-- Scope: MVP 鏍稿績涓氬姟琛?-- ----------------------------

-- ----------------------------
-- 1銆佽祫浜у垎绫昏〃
-- ----------------------------
drop table if exists asset_category;
create table asset_category (
  category_id bigint(20) not null auto_increment comment '鍒嗙被ID',
  parent_id bigint(20) default 0 comment '鐖跺垎绫籌D',
  ancestors varchar(500) default '' comment '绁栫骇鍒楄〃',
  category_code varchar(64) not null comment '鍒嗙被缂栫爜',
  category_name varchar(100) not null comment '鍒嗙被鍚嶇О',
  order_num int(4) default 0 comment '鏄剧ず椤哄簭',
  depreciable_flag char(1) default 'N' comment '鏄惁鎶樻棫',
  serial_required_flag char(1) default 'N' comment '鏄惁蹇呴』搴忓垪鍙?,
  borrowable_flag char(1) default 'N' comment '鏄惁鍏佽鍊熺敤',
  inventory_required_flag char(1) default 'Y' comment '鏄惁绾冲叆鐩樼偣',
  useful_life_months int(11) default null comment '浣跨敤瀵垮懡(鏈?',
  status char(1) default '0' comment '鐘舵€?0姝ｅ父 1鍋滅敤)',
  del_flag char(1) default '0' comment '鍒犻櫎鏍囧織(0瀛樺湪 2鍒犻櫎)',
  create_by varchar(64) default '' comment '鍒涘缓鑰?,
  create_time datetime comment '鍒涘缓鏃堕棿',
  update_by varchar(64) default '' comment '鏇存柊鑰?,
  update_time datetime comment '鏇存柊鏃堕棿',
  remark varchar(500) default null comment '澶囨敞',
  primary key (category_id),
  unique key uk_asset_category_code (category_code),
  key idx_asset_category_parent (parent_id)
) engine=innodb comment = '璧勪骇鍒嗙被琛?;

-- ----------------------------
-- 2銆佽祫浜т綅缃〃
-- ----------------------------
drop table if exists asset_location;
create table asset_location (
  location_id bigint(20) not null auto_increment comment '浣嶇疆ID',
  parent_id bigint(20) default 0 comment '鐖朵綅缃甀D',
  ancestors varchar(500) default '' comment '绁栫骇鍒楄〃',
  location_code varchar(64) not null comment '浣嶇疆缂栫爜',
  location_name varchar(100) not null comment '浣嶇疆鍚嶇О',
  location_type varchar(20) default null comment '浣嶇疆绫诲瀷',
  dept_id bigint(20) default null comment '鎵€灞為儴闂↖D',
  manager_user_id bigint(20) default null comment '浣嶇疆绠＄悊鍛業D',
  order_num int(4) default 0 comment '鏄剧ず椤哄簭',
  status char(1) default '0' comment '鐘舵€?0姝ｅ父 1鍋滅敤)',
  del_flag char(1) default '0' comment '鍒犻櫎鏍囧織(0瀛樺湪 2鍒犻櫎)',
  create_by varchar(64) default '' comment '鍒涘缓鑰?,
  create_time datetime comment '鍒涘缓鏃堕棿',
  update_by varchar(64) default '' comment '鏇存柊鑰?,
  update_time datetime comment '鏇存柊鏃堕棿',
  remark varchar(500) default null comment '澶囨敞',
  primary key (location_id),
  unique key uk_asset_location_code (location_code),
  key idx_asset_location_parent (parent_id),
  key idx_asset_location_dept (dept_id)
) engine=innodb comment = '璧勪骇浣嶇疆琛?;

-- ----------------------------
-- 3銆佽祫浜т富妗ｈ〃
-- ----------------------------
drop table if exists asset_info;
create table asset_info (
  asset_id bigint(20) not null auto_increment comment '璧勪骇ID',
  asset_code varchar(64) not null comment '璧勪骇缂栫爜',
  asset_name varchar(200) not null comment '璧勪骇鍚嶇О',
  category_id bigint(20) not null comment '鍒嗙被ID',
  brand varchar(100) default null comment '鍝佺墝',
  model varchar(100) default null comment '鍨嬪彿',
  specification varchar(255) default null comment '瑙勬牸鎻忚堪',
  serial_no varchar(100) default null comment '搴忓垪鍙?,
  asset_status varchar(20) not null comment '璧勪骇鐘舵€?,
  asset_source varchar(20) default null comment '璧勪骇鏉ユ簮',
  use_org_dept_id bigint(20) default null comment '浣跨敤閮ㄩ棬ID',
  manage_dept_id bigint(20) default null comment '绠＄悊閮ㄩ棬ID',
  current_user_id bigint(20) default null comment '褰撳墠璐ｄ换浜篒D',
  current_location_id bigint(20) default null comment '褰撳墠浣嶇疆ID',
  purchase_date date comment '閲囪喘鏃ユ湡',
  inbound_date date comment '鍏ュ簱鏃ユ湡',
  start_use_date date comment '鍚敤鏃ユ湡',
  original_value decimal(18,2) default 0.00 comment '鍘熷€?,
  residual_value decimal(18,2) default 0.00 comment '娈嬪€?,
  warranty_expire_date date comment '淇濅慨鎴鏃ユ湡',
  supplier_name varchar(200) default null comment '渚涘簲鍟嗗悕绉?,
  qr_code varchar(200) default null comment '浜岀淮鐮佸€?,
  version_no int(11) default 0 comment '鐗堟湰鍙?,
  status char(1) default '0' comment '鐘舵€?0姝ｅ父 1鍋滅敤)',
  del_flag char(1) default '0' comment '鍒犻櫎鏍囧織(0瀛樺湪 2鍒犻櫎)',
  create_by varchar(64) default '' comment '鍒涘缓鑰?,
  create_time datetime comment '鍒涘缓鏃堕棿',
  update_by varchar(64) default '' comment '鏇存柊鑰?,
  update_time datetime comment '鏇存柊鏃堕棿',
  remark varchar(500) default null comment '澶囨敞',
  primary key (asset_id),
  unique key uk_asset_info_code (asset_code),
  key idx_asset_info_category (category_id),
  key idx_asset_info_status (asset_status),
  key idx_asset_info_use_dept (use_org_dept_id),
  key idx_asset_info_manage_dept (manage_dept_id),
  key idx_asset_info_user (current_user_id),
  key idx_asset_info_location (current_location_id),
  key idx_asset_info_serial (serial_no)
) engine=innodb comment = '璧勪骇涓绘。琛?;

-- ----------------------------
-- 4銆佽祫浜ч€氱敤涓氬姟鍗曟嵁涓昏〃
-- ----------------------------
drop table if exists asset_operate_order;
create table asset_operate_order (
  order_id bigint(20) not null auto_increment comment '鍗曟嵁ID',
  order_no varchar(64) not null comment '鍗曟嵁缂栧彿',
  order_type varchar(20) not null comment '鍗曟嵁绫诲瀷',
  order_status varchar(20) not null comment '鍗曟嵁鐘舵€?,
  biz_date datetime comment '涓氬姟鏃堕棿',
  apply_user_id bigint(20) default null comment '鍙戣捣浜篒D',
  apply_dept_id bigint(20) default null comment '鍙戣捣閮ㄩ棬ID',
  approve_user_id bigint(20) default null comment '瀹℃壒浜篒D',
  approve_time datetime comment '瀹℃壒鏃堕棿',
  approve_result varchar(20) default null comment '瀹℃壒缁撴灉',
  from_dept_id bigint(20) default null comment '鏉ユ簮閮ㄩ棬ID',
  to_dept_id bigint(20) default null comment '鐩爣閮ㄩ棬ID',
  from_user_id bigint(20) default null comment '鏉ユ簮璐ｄ换浜篒D',
  to_user_id bigint(20) default null comment '鐩爣璐ｄ换浜篒D',
  from_location_id bigint(20) default null comment '鏉ユ簮浣嶇疆ID',
  to_location_id bigint(20) default null comment '鐩爣浣嶇疆ID',
  expected_return_date date comment '棰勮褰掕繕鏃ユ湡',
  disposal_reason varchar(500) default null comment '鎶ュ簾鍘熷洜',
  disposal_amount decimal(18,2) default 0.00 comment '澶勭疆閲戦/娈嬪€?,
  attachment_count int(11) default 0 comment '闄勪欢鏁伴噺',
  status char(1) default '0' comment '鐘舵€?0姝ｅ父 1鍋滅敤)',
  del_flag char(1) default '0' comment '鍒犻櫎鏍囧織(0瀛樺湪 2鍒犻櫎)',
  create_by varchar(64) default '' comment '鍒涘缓鑰?,
  create_time datetime comment '鍒涘缓鏃堕棿',
  update_by varchar(64) default '' comment '鏇存柊鑰?,
  update_time datetime comment '鏇存柊鏃堕棿',
  remark varchar(500) default null comment '澶囨敞',
  primary key (order_id),
  unique key uk_asset_operate_order_no (order_no),
  key idx_asset_operate_order_type_status (order_type, order_status),
  key idx_asset_operate_order_apply_user (apply_user_id),
  key idx_asset_operate_order_apply_dept (apply_dept_id)
) engine=innodb comment = '璧勪骇閫氱敤涓氬姟鍗曟嵁涓昏〃';

-- ----------------------------
-- 5銆佽祫浜ч€氱敤涓氬姟鍗曟嵁鏄庣粏琛?-- ----------------------------
drop table if exists asset_operate_order_item;
create table asset_operate_order_item (
  item_id bigint(20) not null auto_increment comment '鏄庣粏ID',
  order_id bigint(20) not null comment '鍗曟嵁ID',
  asset_id bigint(20) not null comment '璧勪骇ID',
  before_status varchar(20) default null comment '鍙樻洿鍓嶇姸鎬?,
  after_status varchar(20) default null comment '鍙樻洿鍚庣姸鎬?,
  before_user_id bigint(20) default null comment '鍙樻洿鍓嶈矗浠讳汉ID',
  after_user_id bigint(20) default null comment '鍙樻洿鍚庤矗浠讳汉ID',
  before_dept_id bigint(20) default null comment '鍙樻洿鍓嶉儴闂↖D',
  after_dept_id bigint(20) default null comment '鍙樻洿鍚庨儴闂↖D',
  before_location_id bigint(20) default null comment '鍙樻洿鍓嶄綅缃甀D',
  after_location_id bigint(20) default null comment '鍙樻洿鍚庝綅缃甀D',
  item_status varchar(20) default null comment '鏄庣粏澶勭悊鐘舵€?,
  item_result varchar(255) default null comment '澶勭悊璇存槑',
  primary key (item_id),
  key idx_asset_operate_item_order (order_id),
  key idx_asset_operate_item_asset (asset_id)
) engine=innodb comment = '璧勪骇閫氱敤涓氬姟鍗曟嵁鏄庣粏琛?;

-- ----------------------------
-- 6銆佽祫浜х洏鐐逛换鍔¤〃
-- ----------------------------
drop table if exists asset_inventory_task;
create table asset_inventory_task (
  task_id bigint(20) not null auto_increment comment '浠诲姟ID',
  task_no varchar(64) not null comment '浠诲姟缂栧彿',
  task_name varchar(200) not null comment '浠诲姟鍚嶇О',
  task_scope_type varchar(20) not null comment '鐩樼偣鑼冨洿绫诲瀷',
  task_status varchar(20) not null comment '浠诲姟鐘舵€?,
  target_dept_id bigint(20) default null comment '鐩爣閮ㄩ棬ID',
  target_location_id bigint(20) default null comment '鐩爣浣嶇疆ID',
  target_category_id bigint(20) default null comment '鐩爣鍒嗙被ID',
  plan_start_time datetime comment '璁″垝寮€濮嬫椂闂?,
  plan_end_time datetime comment '璁″垝缁撴潫鏃堕棿',
  owner_user_id bigint(20) default null comment '浠诲姟璐熻矗浜篒D',
  execute_user_id bigint(20) default null comment '鎵ц浜篒D',
  summary_total int(11) default 0 comment '搴旂洏鏁伴噺',
  summary_ok int(11) default 0 comment '姝ｅ父鏁伴噺',
  summary_diff int(11) default 0 comment '宸紓鏁伴噺',
  status char(1) default '0' comment '鐘舵€?0姝ｅ父 1鍋滅敤)',
  del_flag char(1) default '0' comment '鍒犻櫎鏍囧織(0瀛樺湪 2鍒犻櫎)',
  create_by varchar(64) default '' comment '鍒涘缓鑰?,
  create_time datetime comment '鍒涘缓鏃堕棿',
  update_by varchar(64) default '' comment '鏇存柊鑰?,
  update_time datetime comment '鏇存柊鏃堕棿',
  remark varchar(500) default null comment '澶囨敞',
  primary key (task_id),
  unique key uk_asset_inventory_task_no (task_no),
  key idx_asset_inventory_task_status (task_status),
  key idx_asset_inventory_task_owner (owner_user_id),
  key idx_asset_inventory_task_execute (execute_user_id)
) engine=innodb comment = '璧勪骇鐩樼偣浠诲姟琛?;

-- ----------------------------
-- 7銆佽祫浜х洏鐐逛换鍔℃槑缁嗚〃
-- ----------------------------
drop table if exists asset_inventory_task_item;
create table asset_inventory_task_item (
  item_id bigint(20) not null auto_increment comment '鐩樼偣鏄庣粏ID',
  task_id bigint(20) not null comment '浠诲姟ID',
  asset_id bigint(20) not null comment '璧勪骇ID',
  expected_user_id bigint(20) default null comment '璐﹂潰璐ｄ换浜篒D',
  actual_user_id bigint(20) default null comment '鐜板満璐ｄ换浜篒D',
  expected_location_id bigint(20) default null comment '璐﹂潰浣嶇疆ID',
  actual_location_id bigint(20) default null comment '鐜板満浣嶇疆ID',
  expected_status varchar(20) default null comment '璐﹂潰鐘舵€?,
  actual_status varchar(20) default null comment '鐜板満鐘舵€?,
  inventory_result varchar(20) not null comment '鐩樼偣缁撴灉',
  inventory_desc varchar(500) default null comment '宸紓璇存槑',
  inventory_time datetime comment '鐩樼偣鏃堕棿',
  inventory_user_id bigint(20) default null comment '鐩樼偣浜篒D',
  process_status varchar(20) default null comment '澶勭悊鐘舵€?,
  process_desc varchar(500) default null comment '澶勭悊璇存槑',
  primary key (item_id),
  key idx_asset_inventory_item_task (task_id),
  key idx_asset_inventory_item_asset (asset_id),
  key idx_asset_inventory_item_result (inventory_result)
) engine=innodb comment = '璧勪骇鐩樼偣浠诲姟鏄庣粏琛?;

-- ----------------------------
-- 8銆佽祫浜т簨浠舵祦姘磋〃
-- ----------------------------
drop table if exists asset_event_log;
create table asset_event_log (
  event_id bigint(20) not null auto_increment comment '浜嬩欢ID',
  asset_id bigint(20) not null comment '璧勪骇ID',
  event_type varchar(20) not null comment '浜嬩欢绫诲瀷',
  source_order_id bigint(20) default null comment '鏉ユ簮鍗曟嵁ID',
  source_order_type varchar(20) default null comment '鏉ユ簮鍗曟嵁绫诲瀷',
  before_snapshot longtext comment '鍙樻洿鍓嶅揩鐓SON',
  after_snapshot longtext comment '鍙樻洿鍚庡揩鐓SON',
  event_desc varchar(500) default null comment '浜嬩欢璇存槑',
  operator_user_id bigint(20) default null comment '鎿嶄綔浜篒D',
  operate_time datetime comment '鎿嶄綔鏃堕棿',
  primary key (event_id),
  key idx_asset_event_asset_time (asset_id, operate_time),
  key idx_asset_event_order (source_order_id),
  key idx_asset_event_type (event_type)
) engine=innodb comment = '璧勪骇浜嬩欢娴佹按琛?;

-- ----------------------------
-- 9銆佽祫浜ч檮浠惰〃
-- ----------------------------
drop table if exists asset_attachment;
create table asset_attachment (
  attachment_id bigint(20) not null auto_increment comment '闄勪欢ID',
  biz_type varchar(20) not null comment '涓氬姟绫诲瀷',
  biz_id bigint(20) not null comment '涓氬姟ID',
  file_name varchar(255) not null comment '鏂囦欢鍚?,
  file_url varchar(500) not null comment '鏂囦欢鍦板潃',
  file_size bigint(20) default 0 comment '鏂囦欢澶у皬',
  file_type varchar(50) default null comment '鏂囦欢绫诲瀷',
  upload_user_id bigint(20) default null comment '涓婁紶浜篒D',
  upload_time datetime comment '涓婁紶鏃堕棿',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (attachment_id),
  key idx_asset_attachment_biz (biz_type, biz_id)
) engine=innodb comment = '璧勪骇闄勪欢琛?;

