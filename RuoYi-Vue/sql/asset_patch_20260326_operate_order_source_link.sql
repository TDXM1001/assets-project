-- 为资产业务单据补充来源业务强关联字段，解决 repair -> disposal 只能靠文本猜测的问题

alter table asset_operate_order
  add column source_biz_type varchar(20) default null comment '来源业务类型' after order_type,
  add column source_biz_id bigint(20) default null comment '来源业务ID' after source_biz_type,
  add column source_biz_no varchar(64) default null comment '来源业务单号' after source_biz_id;

alter table asset_operate_order
  add unique key uk_asset_operate_order_source_link (order_type, source_biz_type, source_biz_id, del_flag),
  add key idx_asset_operate_order_source_biz (source_biz_type, source_biz_id);
