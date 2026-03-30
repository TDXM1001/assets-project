-- ----------------------------
-- STORY-E009-S002 资产分类属性补全脚本
-- Date: 2026-03-29
-- Description: 为分类表补齐资产类型字段，确认为前端筛选逻辑供数。
-- ----------------------------

-- 1. 为资产分类表补全“资产类型”字段
-- 默认设置为 FIXED_ASSET (固定资产)，确保存量分类在前端筛选中可见。
ALTER TABLE `asset_category` 
ADD COLUMN `asset_type` varchar(20) NOT NULL DEFAULT 'FIXED_ASSET' COMMENT '资产类型' AFTER `category_name`;

-- 2. 刷新存量分类数据
-- 确保现有的演示数据分类（C01, C02）都被打上 FIXED_ASSET 标签。
UPDATE `asset_category` SET `asset_type` = 'FIXED_ASSET' WHERE `asset_type` IS NULL OR `asset_type` = '';
