-- 修复 QA 测试资产名称乱码
-- 说明：
-- 1. 这条 QA 资产的当前名称在 asset_info 中被写成了乱码。
-- 2. 资产事件流水的 before_snapshot / after_snapshot 也保存了同一份乱码名称。
-- 3. 这里同时修正主表和历史快照，避免列表页、详情页和流水页继续显示脏数据。

update asset_info
set asset_name = 'QA测试资产-0326134011',
    update_by = 'admin',
    update_time = sysdate()
where asset_code = 'QA-ASSET-0326134011';

update asset_event_log
set before_snapshot = replace(before_snapshot, 'QA????-0326134011', 'QA测试资产-0326134011'),
    after_snapshot = replace(after_snapshot, 'QA????-0326134011', 'QA测试资产-0326134011')
where asset_id = (
    select asset_id
    from asset_info
    where asset_code = 'QA-ASSET-0326134011'
);
