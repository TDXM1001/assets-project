-- 资产单据/盘点明细表补齐唯一约束
-- 说明：同一单据/任务下同一资产只应出现一次，避免重复落明细导致完成/盘点汇总失真。

alter table asset_operate_order_item
  add unique key uk_asset_operate_order_item_order_asset (order_id, asset_id);

alter table asset_inventory_task_item
  add unique key uk_asset_inventory_task_item_task_asset (task_id, asset_id);
