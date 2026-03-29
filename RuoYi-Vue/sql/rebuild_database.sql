-- =============================================================
-- 数据库完整重建脚本
-- 功能: 删除旧库 → 创建新库 → 执行所有建表和补丁脚本
-- 日期: 2026-03-29
-- 用法: mysql -u root -proot < rebuild_database.sql
-- =============================================================

-- 强制删除并重建数据库
DROP DATABASE IF EXISTS `my-assets`;
CREATE DATABASE `my-assets` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `my-assets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================
-- 第一阶段: 若依基础系统表 (ry_20250522.sql)
-- =============================================================
source ry_20250522.sql;

-- =============================================================
-- 第二阶段: Quartz 定时任务表 (quartz.sql)
-- =============================================================
source quartz.sql;

-- =============================================================
-- 第三阶段: 资产业务核心表 (asset_20260324.sql)
-- =============================================================
source asset_20260324.sql;

-- =============================================================
-- 第四阶段: 资产菜单初始化 (asset_menu_20260324.sql)
-- =============================================================
source asset_menu_20260324.sql;

-- =============================================================
-- 第五阶段: 资产角色初始化 (asset_role_20260324.sql)
-- =============================================================
source asset_role_20260324.sql;

-- =============================================================
-- 第六阶段: 用户角色绑定 (asset_user_role_20260324.sql)
-- =============================================================
source asset_user_role_20260324.sql;

-- =============================================================
-- 第七阶段: 增量补丁 (按日期与依赖顺序执行)
-- =============================================================

-- 2026-03-25 补丁
source asset_patch_20260325_asset_user_self_scope.sql;
source asset_patch_20260325_attachment_audit_columns.sql;
source asset_patch_20260325_inventory_result_nullable.sql;
source asset_patch_20260325_order_inventory_unique.sql;

-- 2026-03-26 补丁
source asset_patch_20260326_asset_status_repairing.sql;
source asset_patch_20260326_category_field_template.sql;
source asset_patch_20260326_cleanup_qa_data.sql;
source asset_patch_20260326_operate_order_source_link.sql;
source asset_patch_20260326_repair_module.sql;

-- 2026-03-27 补丁
source asset_patch_20260327_asset_status_repairing_fix.sql;
source asset_patch_20260327_category_template_history.sql;
source asset_patch_20260327_info_create_page.sql;
source asset_patch_20260327_order_create_page.sql;
source asset_patch_20260327_qa_asset_name_fix.sql;
source asset_patch_20260327_repair_multi_item.sql;
source asset_patch_20260327_repair_page_routes.sql;

-- 2026-03-29 补丁
source asset_patch_20260329_asset_type_foundation.sql;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- 完成！数据库 `my-assets` 已重建
-- =============================================================
SELECT '数据库重建完成!' AS result;
