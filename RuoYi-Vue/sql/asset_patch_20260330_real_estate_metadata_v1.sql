-- ----------------------------
-- 不动产元数据补丁 (幂等全量修复版)
-- Date: 2026-03-30
-- ----------------------------

-- 0. 清理旧数据 (确保可重复执行)
DELETE FROM `sys_dict_data` WHERE `dict_type` IN ('building_structure', 'land_purpose');
DELETE FROM `sys_dict_type` WHERE `dict_type` IN ('building_structure', 'land_purpose');
DELETE FROM `asset_category` WHERE `category_code` IN ('RE01', 'RE02');

-- 1.1 建筑结构字典 (building_structure)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('建筑结构', 'building_structure', '0', 'admin', SYSDATE(), '不动产管理专用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
VALUES 
(1, '框架结构', 'FRAME', 'building_structure', 'info', 'Y', '0', 'admin', SYSDATE()),
(2, '钢混结构', 'STEEL_CONCRETE', 'building_structure', 'primary', 'N', '0', 'admin', SYSDATE()),
(3, '钢结构', 'STEEL', 'building_structure', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '砖混结构', 'BRICK_CONCRETE', 'building_structure', 'default', 'N', '0', 'admin', SYSDATE());

-- 1.2 土地用途字典 (land_purpose)
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('土地用途', 'land_purpose', '0', 'admin', SYSDATE(), '不动产管理专用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
VALUES 
(1, '住宅用地', 'RESIDENTIAL', 'land_purpose', 'success', 'N', '0', 'admin', SYSDATE()),
(2, '商业用地', 'COMMERCIAL', 'land_purpose', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '工业用地', 'INDUSTRIAL', 'land_purpose', 'primary', 'Y', '0', 'admin', SYSDATE()),
(4, '仓储用地', 'WAREHOUSING', 'land_purpose', 'info', 'N', '0', 'admin', SYSDATE());

-- 2.1 办公用房分类及其动态模板 (RE01)
INSERT INTO `asset_category` (`category_code`, `category_name`, `asset_type`, `parent_id`, `ancestors`, `order_num`, `field_template_json`, `field_template_status`, `field_template_version`, `status`, `del_flag`, `create_by`, `create_time`)
VALUES ('RE01', '办公用房', 'REAL_ESTATE', 0, '0', 1, 
'{
  "status": "0",
  "fields": [
    {"fieldCode": "cert_no", "fieldName": "权属证证号", "componentType": "input", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 1, "status": "0"},
    {"fieldCode": "building_area", "fieldName": "建筑面积(㎡)", "componentType": "number", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 2, "status": "0"},
    {"fieldCode": "usable_area", "fieldName": "套内面积(㎡)", "componentType": "number", "groupName": "权证信息", "orderNum": 3, "status": "0"},
    {"fieldCode": "structure", "fieldName": "建筑结构", "componentType": "select", "dictType": "building_structure", "groupName": "物理指标", "orderNum": 4, "status": "0"},
    {"fieldCode": "floors_up", "fieldName": "地上层数", "componentType": "number", "groupName": "物理指标", "orderNum": 5, "status": "0"},
    {"fieldCode": "build_year", "fieldName": "建成年代", "componentType": "date", "groupName": "物理指标", "orderNum": 6, "status": "0"}
  ]
}', '0', 1, '0', '0', 'admin', SYSDATE());

-- 2.2 工业土地分类及其动态模板 (RE02)
INSERT INTO `asset_category` (`category_code`, `category_name`, `asset_type`, `parent_id`, `ancestors`, `order_num`, `field_template_json`, `field_template_status`, `field_template_version`, `status`, `del_flag`, `create_by`, `create_time`)
VALUES ('RE02', '工业土地', 'REAL_ESTATE', 0, '0', 2, 
'{
  "status": "0",
  "fields": [
    {"fieldCode": "cert_no", "fieldName": "权属证证号", "componentType": "input", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 1, "status": "0"},
    {"fieldCode": "land_expire_date", "fieldName": "土地证到期日", "componentType": "date", "requiredFlag": "1", "groupName": "权证信息", "orderNum": 2, "status": "0"},
    {"fieldCode": "land_area", "fieldName": "宗地面积(㎡)", "componentType": "number", "requiredFlag": "1", "groupName": "利用指标", "orderNum": 3, "status": "0"},
    {"fieldCode": "land_purpose", "fieldName": "土地用途", "componentType": "select", "dictType": "land_purpose", "groupName": "利用指标", "orderNum": 4, "status": "0"}
  ]
}', '0', 1, '0', '0', 'admin', SYSDATE());
