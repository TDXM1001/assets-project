# 资产主子表重构 (Asset Relational Redesign) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 `asset_info` 宽表重构为“核心属性主表”与“按类型的专属物理子表”（固定资产、不动产、低值易耗品），彻底消灭数据库空列，提升系统扩展性和健壮性。

**Architecture:** 
1. 数据库层：创建 3 张子表，用 `asset_id` 1:1 关联主表。清洗老数据并物理裁剪 `asset_info` 的冗余列。
2. 实体层：将 `AssetInfo.java` 中专属列移除，建立新的 `AssetFixedInfo`、`AssetRealEstateInfo` 子实体与对应的 Mapper。
3. 聚合逻辑层：改造后端 `AssetInfoServiceImpl`，保持前端提交的合并表单 (扁平化 JSON) 的 `payload` 不变，在 `insert` 和 `update` 逻辑中，根据 `assetType` 枚举将数据拆解后下沉写入特定的子表引擎。

**Tech Stack:** MySQL 8, Spring Boot (Java), MyBatis, Vue 3.

---

### Task 1: 物理数据库 DDL 重构与数据清洗

**Files:**
- Create: `sql/asset_patch_20260329_relational_tables.sql`

**Step 1: 编写物理 DDL 脚本**
```sql
-- 1. 创建固定资产物理扩展表
CREATE TABLE `asset_fixed_info` (
  `asset_id` bigint(20) NOT NULL COMMENT '主键，关联主表',
  `brand` varchar(64) DEFAULT NULL COMMENT '品牌',
  `model` varchar(128) DEFAULT NULL COMMENT '型号',
  `specification` varchar(255) DEFAULT NULL COMMENT '规格',
  `serial_no` varchar(128) DEFAULT NULL COMMENT '序列号',
  `warranty_expire_date` date DEFAULT NULL COMMENT '维保到期日',
  PRIMARY KEY (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定资产特有属性表';

-- 2. 创建不动产物理扩展表
CREATE TABLE `asset_real_estate_info` (
  `asset_id` bigint(20) NOT NULL COMMENT '主键，关联主表',
  `property_cert_no` varchar(128) DEFAULT NULL COMMENT '产权证号',
  `property_address` varchar(512) DEFAULT NULL COMMENT '坐落地址',
  `land_area` decimal(15,2) DEFAULT NULL COMMENT '占地面积(m2)',
  `floor_area` decimal(15,2) DEFAULT NULL COMMENT '建筑面积(m2)',
  PRIMARY KEY (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产特有属性表';

-- 3. 数据无损清洗：抽取存量数据填充至对应子表 (INSERT INTO ... SELECT)
-- ... 

-- 4. 瘦身：主表剥离废弃专有列
ALTER TABLE `asset_info` 
  DROP COLUMN `brand`,
  DROP COLUMN `model`,
  DROP COLUMN `specification`,
  DROP COLUMN `serial_no`,
  DROP COLUMN `warranty_expire_date`;
```

**Step 2: 执行数据库补丁**
Run: `python scripts/execute_patch.py sql/asset_patch_20260329_relational_tables.sql`
Expected: 成功执行无报错，主表已完成剥离，子表建立。

**Step 3: Commit**
```bash
git add sql/
git commit -m "chore(database): 创建主子表物理架构并执行主表瘦身与数据清洗"
```

### Task 2: 后端实体域分离与主表查改同步 (Domain & Mapper)

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/domain/asset/AssetInfo.java`
- Modify: `ruoyi-system/src/main/resources/mapper/asset/AssetInfoMapper.xml`

**Step 1: 清理 AssetInfo.java 及 Mapper**
将实体 `AssetInfo` 中的 `brand`, `model`, `specification`, `serialNo`, `warrantyExpireDate` 等全部删除，同时更新 `AssetInfoMapper.xml` 中涉及的 `resultMap`、`insert` 与 `update` 节点，摘除对应的 SQL 映射。

**Step 2: 验证编译检查**
Run: `mvn clean compile -pl ruoyi-system`
Expected: 编译通过（由于关联清理可能有少量编译报错，在 Task 3 中修复）。

**Step 3: Commit**
```bash
git add ruoyi-system/
git commit -m "refactor(backend): 剥离 AssetInfo 实体中的非法资产强类型关联字段"
```

### Task 3: 聚合接口层的拆包与装配重塑 (Controller & Service)

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/asset/impl/AssetInfoServiceImpl.java`
- Modify: 或者如果需要保持给前端响应结构的稳定，引入对应的 `AssetInfoVO` 与 `AssetInfoDTO`。

**Step 1: 重铸插入逻辑**
因为我们不希望牵扯大量的前端 Vue 表单重构，前端仍发送形如 `{ assetCode, assetName, brand, model... }` 的 JSON 体。
引入 `AssetFixedInfoMapper`。在 `AssetInfoServiceImpl.insertAssetInfo` 方法中：
先存 `asset_info` 拿到 `assetId`，接着判断：
如果类型为 `FIXED_ASSET` 或 `LOW_VALUE`，提取参数构造 `AssetFixedInfo` 并插入子表；若为 `REAL_ESTATE` 提入不动产表。

**Step 2: 重铸查询联表逻辑**
如果是分页查询或详情查询（`selectAssetInfoById`），需要在代码中进行聚合装配，或者在 MyBatis 中启用 association 一比一连接动态拉取扩展属性并暴露给前端展现。

**Step 3: 启动后端并验证功能不破窗**
Run: `mvn spring-boot:run` （执行主类查错）。
Expected: 后端启动成功。

**Step 4: Commit**
```bash
git add .
git commit -m "refactor(backend): Service层接管主子表路由存储，屏蔽端侧差异"
```

### Task 4: 前端适配与梳理 (Vue UI)

**Files:**
- Modify: `art-design-pro/src/views/asset/info/create/index.vue`
- Modify: `art-design-pro/src/views/asset/info/modules/info-dialog.vue`

**Step 1: 物理隔离的强前端展示**
当前端表单选定 `assetType === 'REAL_ESTATE'` 时，强行 `v-if="false"` 隐藏品牌、型号和序列号（这些字段本应被扔进不动产的扩展模板或物理结构中）。明确界面录入边界。

**Step 2: Verify Lint**
Run: `npm run lint`
Expected: 0 errors

**Step 3: Commit**
```bash
git add art-design-pro/
git commit -m "refactor(ui): 前端配合底层隔离，实施基于资产类型的强 UI 隐藏逻辑"
```
