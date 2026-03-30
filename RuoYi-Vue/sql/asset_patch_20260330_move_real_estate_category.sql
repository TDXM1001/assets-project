-- ---------------------------------------------------------
-- 菜单挪动补丁：将“不动产分类”移动到“不动产管理”目录下
-- 创建时间：2026-03-30
-- ---------------------------------------------------------

-- 1. 获取目标父菜单“不动产管理”的 ID
SET @parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '不动产管理' LIMIT 1);

-- 2. 执行更新：修改父级 ID 并调整排序
UPDATE sys_menu 
SET parent_id = @parent_id, 
    order_num = 10 -- 设置为该目录下的顺序（可根据需要调整）
WHERE menu_name = '不动产分类';

-- 3. 同时确保其资产类型参数（query）依然正确（以防万一）
UPDATE sys_menu 
SET `query` = 'assetType=REAL_ESTATE' 
WHERE menu_name = '不动产分类';

-- 提示信息
SELECT '不动产分类已成功移动到不动产管理目录下' AS result;
