# 资产类型主子表关系模型重构设计

## 1. 业务背景
当前资产台账（`asset_info`）采用了“一张胖表包打天下”的模式，将包含固定资产独有的品牌型号、不动产独有的面积/产权、低值易耗品的特定信息全部糅合在一张物理表中。随着业务深入，这导致了数据库出现大量空列，同时也使得 Java 实体极度臃肿，违背了数据库设计的完整性和系统的可维护性，是强烈的“坏气味”。

基于 gstack 的 Boil the Lake 原则，我们不采用 JSON 黑洞的妥协方案，而是决定建立一套严谨的**主子表关系模型（Master-Detail Tables）**，为未来长线发展打下基础。

## 2. 数据库物理架构设计

### 2.1 主表瘦身 - `asset_info`（核心生命线主表）
剔除只针对特定资产类型的列，只保留 100% 通用的资金流、权属流和生命周期属性：
- **身份标识**：`asset_id` (PK), `asset_code`, `asset_name`, `asset_type`, `category_id`, `qr_code`
- **生命周期与状态**：`asset_status`, `asset_source`, `version_no`, `status`, `del_flag`
- **权属流转**：`use_org_dept_id`, `manage_dept_id`, `current_user_id`, `current_location_id`
- **财务属性**：`original_value`, `residual_value` (基准计算底座)
- **通用日期**：`purchase_date`, `inbound_date`, `start_use_date`
- **其他通用**：`supplier_name`, `extra_fields_json` (处理极度冷门的碎片字段), 创建/修改审计字段

**被剔除的物理列**：
`brand`（品牌）, `model`（型号）, `specification`（规格）, `serial_no`（序列号）, `warranty_expire_date`（质保到期日）。

### 2.2 物理子表设计

#### 子表 1：`asset_fixed_info` (固定资产扩展表)
承接核心的物理硬件属性。
- `asset_id` (PK & FK -> `asset_info.asset_id`)
- `brand` (varchar 64) - 品牌
- `model` (varchar 128) - 型号
- `specification` (varchar 255) - 规格
- `serial_no` (varchar 128) - 序列号 / SN
- `warranty_expire_date` (date) - 质保到期日

#### 子表 2：`asset_real_estate_info` (不动产扩展表)
承接房地产特有属性。
- `asset_id` (PK & FK -> `asset_info.asset_id`)
- `property_cert_no` (varchar 128) - 产权证号
- `property_address` (varchar 512) - 坐落地址
- `land_area` (decimal) - 占地面积 (m2)
- `floor_area` (decimal) - 建筑面积 (m2)

#### 子表 3：`asset_low_value_info` (低值易耗品扩展表)
低值易耗品生命周期极短，通常只关心规格，不记录严格的维保。
- `asset_id` (PK & FK -> `asset_info.asset_id`)
- `brand` (varchar 64) - 品牌
- `model` (varchar 128) - 型号
- `specification` (varchar 255) - 规格

## 3. Java 架构映射方案
在 `AssetInfo` 主实体中，增加针对子实体的引用块。这不破坏现有前端 UI 提交的扁平化 JSON 结构，后端在 Controller 层通过 `assetType` 分发策略，将扁平化参数拆分后保存入主表和对应的子表引擎中。这样即可保持前端零重构或低重构。
