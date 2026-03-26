-- 资产模块 QA 数据清理脚本
-- 用途：
-- 1. 清理由浏览器联调、导入联调、角色联调产生的 QA 样本数据
-- 2. 仅删除业务样本，不动菜单、角色、字典等基础配置
-- 3. 脚本按依赖顺序删除，支持重复执行

-- 删除 QA 附件
DELETE FROM asset_attachment
WHERE biz_type IN ('ASSET_INFO', 'ASSET_ORDER')
  AND (
    biz_id IN (
      SELECT t.asset_id
      FROM (
        SELECT asset_id
        FROM asset_info
        WHERE asset_code LIKE 'QA-%' OR asset_name LIKE 'QA%'
      ) t
    )
    OR biz_id IN (
      SELECT t.order_id
      FROM (
        SELECT order_id
        FROM asset_operate_order
        WHERE order_no LIKE 'QA-%'
      ) t
    )
  );

-- 删除 QA 流水
DELETE FROM asset_event_log
WHERE asset_id IN (
    SELECT t.asset_id
    FROM (
      SELECT asset_id
      FROM asset_info
      WHERE asset_code LIKE 'QA-%' OR asset_name LIKE 'QA%'
    ) t
  )
  OR (
    source_order_type IN ('ASSIGN', 'BORROW', 'RETURN', 'TRANSFER', 'DISPOSAL')
    AND source_order_id IN (
      SELECT t.order_id
      FROM (
        SELECT order_id
        FROM asset_operate_order
        WHERE order_no LIKE 'QA-%'
      ) t
    )
  )
  OR (
    source_order_type = 'INVENTORY_TASK'
    AND source_order_id IN (
      SELECT t.task_id
      FROM (
        SELECT task_id
        FROM asset_inventory_task
        WHERE task_no LIKE 'QA-%' OR task_name LIKE 'QA%'
      ) t
    )
  );

-- 删除 QA 单据明细
DELETE FROM asset_operate_order_item
WHERE order_id IN (
    SELECT t.order_id
    FROM (
      SELECT order_id
      FROM asset_operate_order
      WHERE order_no LIKE 'QA-%'
    ) t
  )
  OR asset_id IN (
    SELECT t.asset_id
    FROM (
      SELECT asset_id
      FROM asset_info
      WHERE asset_code LIKE 'QA-%' OR asset_name LIKE 'QA%'
    ) t
  );

-- 删除 QA 盘点明细
DELETE FROM asset_inventory_task_item
WHERE task_id IN (
    SELECT t.task_id
    FROM (
      SELECT task_id
      FROM asset_inventory_task
      WHERE task_no LIKE 'QA-%' OR task_name LIKE 'QA%'
    ) t
  )
  OR asset_id IN (
    SELECT t.asset_id
    FROM (
      SELECT asset_id
      FROM asset_info
      WHERE asset_code LIKE 'QA-%' OR asset_name LIKE 'QA%'
    ) t
  );

-- 删除 QA 单据
DELETE FROM asset_operate_order
WHERE order_no LIKE 'QA-%';

-- 删除 QA 盘点任务
DELETE FROM asset_inventory_task
WHERE task_no LIKE 'QA-%' OR task_name LIKE 'QA%';

-- 删除 QA 资产主档
DELETE FROM asset_info
WHERE asset_code LIKE 'QA-%' OR asset_name LIKE 'QA%';

-- 删除 QA 分类
DELETE FROM asset_category
WHERE category_code LIKE 'QA%' OR category_name LIKE 'QA%';

-- 删除 QA 位置
DELETE FROM asset_location
WHERE location_code LIKE 'QA%' OR location_name LIKE 'QA%';
