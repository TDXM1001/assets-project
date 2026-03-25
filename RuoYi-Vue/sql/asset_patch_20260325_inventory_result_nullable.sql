-- 资产盘点明细在未扫码前不应提前写入盘点结果，因此允许 inventory_result 为空
alter table asset_inventory_task_item
  modify column inventory_result varchar(20) null comment '盘点结果';
