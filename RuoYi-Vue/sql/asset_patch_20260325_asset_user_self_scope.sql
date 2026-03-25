-- ----------------------------
-- 资产角色数据范围补丁
-- Date: 2026-03-25
-- Scope: sys_role
-- ----------------------------

-- 资产使用人的可见范围应由“本人数据”驱动，避免误命中 sys_role_dept 导致查不到数据。
update sys_role
set data_scope = '5',
    update_by = 'admin',
    update_time = sysdate(),
    remark = concat(ifnull(remark, ''), if(remark is null or remark = '', '', '；'), '2026-03-25 调整为仅本人数据权限')
where role_key = 'asset_user'
  and data_scope <> '5';
