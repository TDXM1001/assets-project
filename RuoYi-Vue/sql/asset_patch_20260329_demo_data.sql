-- ----------------------------
-- 固定资产管理系统展示模拟数据
-- Date: 2026-03-29
-- Scope: 三级分类、两个位置、五项固定资产及事件流水
-- ----------------------------

-- 1. 模拟分类 (演示专用)
INSERT INTO `asset_category` (`category_code`, `category_name`, `parent_id`, `depreciable_flag`, `useful_life_months`, `create_by`) 
VALUES ('C01', '电子设备', 0, 'Y', 36, 'admin');
SET @cat_elec = LAST_INSERT_ID();

INSERT INTO `asset_category` (`category_code`, `category_name`, `parent_id`, `depreciable_flag`, `useful_life_months`, `create_by`) 
VALUES ('C02', '办公家具', 0, 'Y', 60, 'admin');
SET @cat_furn = LAST_INSERT_ID();

-- 2. 模拟位置 (演示专用)
INSERT INTO `asset_location` (`location_code`, `location_name`, `parent_id`, `location_type`, `create_by`) 
VALUES ('L01', '上海总部-研发部', 0, 'OFFICE', 'admin');
SET @loc_hq = LAST_INSERT_ID();

INSERT INTO `asset_location` (`location_code`, `location_name`, `parent_id`, `location_type`, `create_by`) 
VALUES ('L02', '杭州研发中心-01机房', 0, 'IDC', 'admin');
SET @loc_hz = LAST_INSERT_ID();

-- 3. 核心资产模拟数据
-- 资产1：MacBook Pro (在用)
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `asset_status`, `asset_source`, `use_org_dept_id`, `current_location_id`, `purchase_date`, `original_value`, `residual_value`, `create_by`, `create_time`)
VALUES ('FA202603001', 'MacBook Pro 14 M3', @cat_elec, 'Apple', 'M3 Pro 18G', 'USING', 'PURCHASE', 103, @loc_hq, '2026-03-01', 14999.00, 500.00, 'admin', NOW());

-- 资产2：戴尔服务器 (在用)
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `asset_status`, `asset_source`, `use_org_dept_id`, `current_location_id`, `purchase_date`, `original_value`, `residual_value`, `create_by`, `create_time`)
VALUES ('FA202603002', 'PowerEdge R750', @cat_elec, 'Dell', 'R750 Rack', 'USING', 'PURCHASE', 101, @loc_hz, '2025-12-20', 45000.00, 2000.00, 'admin', NOW());

-- 资产3：人体工学椅 (闲置)
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `asset_status`, `asset_source`, `current_location_id`, `purchase_date`, `original_value`, `create_by`, `create_time`)
VALUES ('FA202603003', '赫曼米勒 Aeron', @cat_furn, 'Herman Miller', 'Gen3 B Size', 'IDLE', 'PURCHASE', @loc_hq, '2026-02-15', 8500.00, 'admin', NOW());

-- 资产4：激光打印机 (维修中)
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `asset_status`, `asset_source`, `use_org_dept_id`, `current_location_id`, `purchase_date`, `original_value`, `create_by`, `create_time`)
VALUES ('FA202603004', '惠普 M401n', @cat_elec, 'HP', 'LaserJet M401', 'REPAIR', 'PURCHASE', 103, @loc_hq, '2024-05-10', 2300.00, 'admin', NOW());

-- 资产5：会议室长桌 (闲置)
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `asset_status`, `asset_source`, `current_location_id`, `purchase_date`, `original_value`, `create_by`, `create_time`)
VALUES ('FA202603005', '定制胡桃木会议桌', @cat_furn, '定制', '3.5米', 'IDLE', 'CONSTRUCT', @loc_hq, '2025-01-05', 12000.00, 'admin', NOW());

-- 4. 模拟初始事件日志
INSERT INTO `asset_event_log` (`asset_id`, `event_type`, `event_desc`, `operator_user_id`, `operate_time`)
SELECT asset_id, 'INBOUND', '初始化系统模拟数据入库', 1, NOW() FROM `asset_info` WHERE asset_code LIKE 'FA202603%';
