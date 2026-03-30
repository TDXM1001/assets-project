-- ---------------------------------------------------------
-- 【紧急修复】不动产与固定资产分类管理菜单隔离脚本 (健壮版)
-- Date: 2026-03-30
-- Author: Antigravity
-- Fix: 解决 parent_id 为 NULL 导致的后端报错
-- ---------------------------------------------------------

-- 1. 数据清理：清理之前由于异常产生的记录
DELETE FROM sys_menu WHERE menu_name IN ('固定资产分类', '不动产分类');

-- 2. 定位父级菜单 ID (优先按 path 匹配，其次按名称，最后兜底为 0)
SET @asset_parent_id = (
  SELECT menu_id FROM sys_menu 
  WHERE (path = 'asset' OR menu_name = '资产管理') 
  AND parent_id = 0 
  LIMIT 1
);

-- 若仍然未找到父级，强制设为 0 (挂载到顶级)
SET @asset_parent_id = IFNULL(@asset_parent_id, 0);

-- 3. 隐藏原有的通用“分类管理”菜单
-- 通过路径 'category' 定位更准确
UPDATE sys_menu 
SET visible = '1' 
WHERE path = 'category' AND parent_id = @asset_parent_id;

-- 4. 注入“固定资产分类”菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('固定资产分类', @asset_parent_id, 2, 'fixedCategory', 'asset/category/index', 'assetType=FIXED_ASSET', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree', 'admin', SYSDATE(), '隔离后的固定资产分类管理');

-- 5. 注入“不动产分类”菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('不动产分类', @asset_parent_id, 3, 'realEstateCategory', 'asset/category/index', 'assetType=REAL_ESTATE', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree', 'admin', SYSDATE(), '隔离后的不动产分类管理');

-- 6. 【关键】检查并修复 sys_menu 表中所有可能存在的 NULL parent_id
-- 虽然本脚本已修正，但为防万一，执行一次全表健康检查
UPDATE sys_menu SET parent_id = 0 WHERE parent_id IS NULL;
