# 不动产独立管理模块设计规范 (Real Estate Independent Module)

> **File:** `docs/superpowers/specs/2026-03-29-real-estate-independent-module-design.md`
> **Status:** Draft (Pending User Review)
> **Goal:** 为不动产资产建立专业的、独立的、数据驱动的管理模块，支持地图定位与分类扩展字段。

---

## 1. 背景与目标

目前系统的“资产台账”将固定资产与不动产混合管理。由于不动产（房屋、土地、构筑物）具有高度专业化的物理指标（面积、层数）与权属特征（房产证、土地证、使用权类型），通用的资产表单无法满足深度的运维与审计需求。

本方案旨在：
- **UI 解耦**: 为不动产提供专门的 `index.vue`、`create` 等页面，提升操作专业度。
- **数据解耦**: 使用 `real_estate_status` 独立字典，与固定资产的状态机完全隔离开。
- **扩展性增强**: 采用“基础字段 + 分类对应动态字段”的模式，灵活支撑不同类别（房屋、土地、地块）的差异化属性。
- **地理可视化**: 引入地图卡片组件，实现资产地理位置的可视化管理。

---

## 2. 系统架构

### 2.1 数据库方案 (Backend & Data)

我们将复用 `asset_info` 主表（保留通用的折旧、流水、审计能力），但通过以下方式扩展专业能力：

- **核心字段 (Primary Fields)**:
    - `longitude` / `latitude`: 经纬度坐标。
    - `addr_detail`: 详细地址。
    - `real_estate_type`: 不动产分类（房屋、土地、构筑物等）。
    - `title_corp`: 产权单位。
    - `land_cert_no` / `prop_cert_no`: 土地/房产证号。
- **扩展字段 (Extended Fields)**:
    - 存储于 `attr_json` 字段。
    - 不同分类（通过 `categoryId` 触发）动态加载不同的模板项（如面积、层数、容积率等）。

### 2.2 字典定义 (Dictionaries)

新增 `real_estate_status` 字典，代替通用的 `asset_status`：
- `VACANT`: 闲置/交付待用
- `IN_USE`: 自用中
- `LEASED`: 已出租/外借
- `UNDER_REPAIR`: 维修/加固中
- `PENDING_DISPOSAL`: 待处置/拟拆迁
- `DISPOSED`: 已处置

### 2.3 路由设计 (Routing)

| 菜单名 | 路由路径 | 关联组件 |
| :--- | :--- | :--- |
| 不动产管理 | `/asset/real-estate` | `Layout` |
| 不动产台账 | `index` | `asset/real-estate/index.vue` |
| 新增不动产 | `create` | `asset/real-estate/create.vue` |
| 证照库追踪 | `cert-track` | `asset/real-estate/cert-track.vue` (后期阶段) |

---

## 3. UI/UX 设计要点

### 3.1 列表页 (`index.vue`)
- **表格列**: 增加“建筑面积”、“详细地址”、“权属状态”等不动产高频关注列。
- **地图视图切換**: 支持从表格视图一键切换到地图散点视图（后续版本）。

### 3.2 详情与编辑页 (`create.vue`)
- **核心区块**:
    - **位置信息**: 包含地址输入框与经纬度坐标。
    - **证照信息**: 产证编号录入及扫描件上传。
    - **地图卡片**: 右侧或下方集成一个小窗地图，实时显示坐标点。
- **动态渲染**: 根据所选分类，调用 `DynamicFieldRenderer` 加载特定的物理指标项。

---

## 4. 数据迁移计划

编写 SQL 脚本执行以下逻辑：
1. 更新 `sys_menu` 加入不动产专属入口。
2. 扫描 `asset_info` 中 `asset_type='REAL_ESTATE'` 的记录。
3. 将上述记录的原始状态映射至新字典 `real_estate_status`。
4. 清空这类记录的 `asset_type` 字段（或标记为特定前缀），防止其继续出现在“固定资产台账”中。

---

## 5. 待确认项 (TBD)
- **地图选型**: 是使用国内通用的 AMap (高德) 还是更开放的 Leaflet (静态瓦片)？*（建议首选高德，支持地址解析更友好）*。
- **产证管理**: 是否需要多证联动 (一房多证或多证合一)？

---

## 6. 自评 (Self-Review)
- [x] **无占位符**: 已明确核心字段与字典定义。
- [x] **一致性**: 确保与 RuoYi-Vue-Plus 的 `useDict` 及 `DictTag` 模式对齐。
- [x] **范围控制**: 聚焦于不动产主台账建设，租赁合同等深度管理作为下阶段子项目。
