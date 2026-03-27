-- 多资产维修明细表
create table if not exists asset_repair_order_item (
    repair_item_id      bigint(20)      not null auto_increment comment '维修明细ID',
    repair_id           bigint(20)      not null comment '维修单ID',
    asset_id            bigint(20)      not null comment '资产ID',
    asset_code          varchar(64)              default '' comment '资产编码',
    asset_name          varchar(128)             default '' comment '资产名称',
    fault_desc          varchar(500)             default null comment '故障描述',
    before_status       varchar(32)              default null comment '维修前状态',
    after_status        varchar(32)              default null comment '维修后状态',
    result_type         varchar(32)              default null comment '维修结果',
    repair_cost         decimal(12, 2)           default null comment '维修费用',
    downtime_hours      decimal(10, 1)           default null comment '停用时长',
    rework_flag         char(1)                  default '0' comment '是否返修',
    sort_order          int(11)                  default 1 comment '排序号',
    status              char(1)                  default '0' comment '状态',
    del_flag            char(1)                  default '0' comment '删除标记',
    create_by           varchar(64)              default '' comment '创建者',
    create_time         datetime                 default current_timestamp comment '创建时间',
    update_by           varchar(64)              default '' comment '更新者',
    update_time         datetime                 default current_timestamp on update current_timestamp comment '更新时间',
    remark              varchar(500)             default null comment '备注',
    primary key (repair_item_id),
    key idx_asset_repair_order_item_repair_id (repair_id),
    key idx_asset_repair_order_item_asset_id (asset_id)
) engine=innodb auto_increment=1 comment='资产维修单明细表';

-- 将历史单资产维修单回填为一条默认明细，兼容多资产页面读取。
insert into asset_repair_order_item (
    repair_id,
    asset_id,
    asset_code,
    asset_name,
    fault_desc,
    before_status,
    after_status,
    result_type,
    repair_cost,
    downtime_hours,
    rework_flag,
    sort_order,
    status,
    del_flag,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
)
select r.repair_id,
       r.asset_id,
       ifnull(r.asset_code, ''),
       ifnull(r.asset_name, ''),
       r.fault_desc,
       r.before_status,
       r.after_status,
       r.result_type,
       r.repair_cost,
       r.downtime_hours,
       ifnull(r.rework_flag, '0'),
       1,
       ifnull(r.status, '0'),
       '0',
       ifnull(r.create_by, ''),
       ifnull(r.create_time, sysdate()),
       ifnull(r.update_by, ''),
       ifnull(r.update_time, sysdate()),
       r.remark
from asset_repair_order r
where r.del_flag = '0'
  and r.asset_id is not null
  and not exists (
      select 1
      from asset_repair_order_item i
      where i.repair_id = r.repair_id
        and i.del_flag = '0'
  );
