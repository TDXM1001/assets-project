SET @current_schema = DATABASE();

CREATE TABLE IF NOT EXISTS asset_category_template_history (
    snapshot_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '快照主键',
    category_id bigint(20) NOT NULL COMMENT '分类ID',
    field_template_version int NOT NULL COMMENT '模板版本号',
    field_template_status char(1) NOT NULL DEFAULT '1' COMMENT '模板状态（0正常 1停用）',
    field_template_json longtext NULL COMMENT '模板快照JSON',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_asset_category_template_version (category_id, field_template_version),
    KEY idx_asset_category_template_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类模板历史快照';

INSERT INTO asset_category_template_history (
    category_id,
    field_template_version,
    field_template_status,
    field_template_json,
    create_by,
    create_time,
    update_by,
    update_time
)
SELECT c.category_id,
       c.field_template_version,
       COALESCE(c.field_template_status, '1'),
       c.field_template_json,
       COALESCE(NULLIF(c.update_by, ''), c.create_by, 'system'),
       COALESCE(c.update_time, c.create_time, NOW()),
       COALESCE(c.update_by, c.create_by, 'system'),
       COALESCE(c.update_time, c.create_time, NOW())
FROM asset_category c
WHERE c.del_flag = '0'
  AND c.field_template_version IS NOT NULL
  AND c.field_template_version > 0
  AND c.field_template_json IS NOT NULL
  AND c.field_template_json <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM asset_category_template_history h
      WHERE h.category_id = c.category_id
        AND h.field_template_version = c.field_template_version
  );
