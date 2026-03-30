-- ---------------------------------------------------------
-- 不动产与固定资产分类管理菜单隔离脚本
-- Date: 2026-03-30
-- Author: Antigravity
-- ---------------------------------------------------------

-- 1. 定位父级菜单“资产管理”
SET @asset_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '资产管理' AND parent_id = 0 LIMIT 1);

-- 2. 隐藏原有的通用“分类管理”菜单（设为不可见，但不删除）
-- 这样如果用户之前收藏了该页面，仍能通过 URL 访问，但侧边栏将不再显示。
UPDATE sys_menu 
SET visible = '1' 
WHERE menu_name = '分类管理' AND parent_id = @asset_parent_id;

-- 3. 注入“固定资产分类”菜单 (固定资产路径：fixedCategory)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '固定资产分类', @asset_parent_id, 2, 'fixedCategory', 'asset/category/index', 'assetType=FIXED_ASSET', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree', 'admin', SYSDATE(), '隔离后的固定资产分类管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '固定资产分类' AND parent_id = @asset_parent_id);

-- 4. 注入“不动产分类”菜单 (不动产路径：realEstateCategory)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '不动产分类', @asset_parent_id, 3, 'realEstateCategory', 'asset/category/index', 'assetType=REAL_ESTATE', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree', 'admin', SYSDATE(), '隔离后的不动产分类管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '不动产分类' AND parent_id = @asset_parent_id);

-- 5. 如果需要，同步更新系统内的权限引用（通常不需要，因为 perms 一致，会自动继承原权限）
