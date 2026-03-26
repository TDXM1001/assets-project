SET @current_schema = DATABASE();

SET @ddl = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @current_schema
              AND TABLE_NAME = 'asset_category'
              AND COLUMN_NAME = 'field_template_version'
        ),
        'SELECT 1',
        'ALTER TABLE asset_category ADD COLUMN field_template_version int NULL COMMENT ''字段模板版本号'' AFTER useful_life_months'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @current_schema
              AND TABLE_NAME = 'asset_category'
              AND COLUMN_NAME = 'field_template_status'
        ),
        'SELECT 1',
        'ALTER TABLE asset_category ADD COLUMN field_template_status char(1) NOT NULL DEFAULT ''1'' COMMENT ''字段模板状态（0正常 1停用）'' AFTER field_template_version'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @current_schema
              AND TABLE_NAME = 'asset_category'
              AND COLUMN_NAME = 'field_template_json'
        ),
        'SELECT 1',
        'ALTER TABLE asset_category ADD COLUMN field_template_json longtext NULL COMMENT ''字段模板JSON'' AFTER field_template_status'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @current_schema
              AND TABLE_NAME = 'asset_info'
              AND COLUMN_NAME = 'template_version'
        ),
        'SELECT 1',
        'ALTER TABLE asset_info ADD COLUMN template_version int NULL COMMENT ''模板版本号'' AFTER version_no'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @current_schema
              AND TABLE_NAME = 'asset_info'
              AND COLUMN_NAME = 'extra_fields_json'
        ),
        'SELECT 1',
        'ALTER TABLE asset_info ADD COLUMN extra_fields_json longtext NULL COMMENT ''扩展字段JSON'' AFTER template_version'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
