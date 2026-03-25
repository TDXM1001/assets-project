-- 资产附件表补齐若依通用审计字段
-- 说明：历史建表脚本里漏掉了 create_by/create_time/update_by/update_time/remark，
-- 导致附件列表查询和附件上传写入在现库中会报错。本补丁仅用于现库修正。

alter table asset_attachment add column create_by varchar(64) default '' comment '创建者' after upload_time;
alter table asset_attachment add column create_time datetime comment '创建时间' after create_by;
alter table asset_attachment add column update_by varchar(64) default '' comment '更新者' after create_time;
alter table asset_attachment add column update_time datetime comment '更新时间' after update_by;
alter table asset_attachment add column remark varchar(500) default null comment '备注' after update_time;
