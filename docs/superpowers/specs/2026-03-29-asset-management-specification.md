# 固定资产管理系统 (Fixed Asset Management System) 业务与技术规范说明书

## 1. 项目概况
本项目是一套基于 **RuoYi-Vue** 深度定制开发的固定资产管理系统。旨在通过规范化的业务单据流转，实现固定资产从“采购入库”到“日常运维”再到“处置报废”的全生命周期闭环管理。

---

## 2. 业务架构与逻辑

### 2.1 核心业务领域 (Domain)
系统围绕以下核心实体展开：
*   **固定资产台账 (`AssetInfo`)**：中心数据源，存储固定资产的基础属性（编号、名称、分类、状态、位置、保管人等）。
*   **资产分类 (`AssetCategory`)**：树形目录结构，支持关联“扩展属性模板”，实现不同类型资产（如电子设备 vs. 房地产）的差异化字段管理。
*   **存放地点 (`AssetLocation`)**：层级化管理资产的物理空间位置。
*   **业务单据 (`AssetOperateOrder`)**：所有资产变动均通过“单据”驱动，确保每一步变动可审计。

### 2.2 资产全生命周期状态机
资产的状态由业务单据的操作结果自动驱动，严禁手动直接修改资产状态：

| 业务动作 (单据类型) | 起始状态 | 目标状态 | 说明 |
| :--- | :--- | :--- | :--- |
| **资产入库 (`INBOUND`)** | - | `闲置` | 新资产进入系统，默认闲置。 |
| **领用 (`OUTBOUND`)** | `闲置` | `在使用` | 领用完成后，资产状态更新，保管人更新为申领人。 |
| **借用 (`BORROW`)** | `闲置` | `在使用` | 需记录预计归还日期。 |
| **归还 (`RETURN`)** | `在使用` | `闲置` | 领用或借用后的资产退回仓库。 |
| **资产转移 (`TRANSFER`)** | `不限` | `不限` | 仅改变所属部门、存放地点或保管人，不改变运行状态。 |
| **报修 (`REPAIR`)** | `在使用/闲置` | `维修中` | 发起报修单后，资产进入不可用状态。 |
| **维修完成** | `维修中` | `闲置/在使用` | 维修结束后恢复其原有的可用性。 |
| **报废 (`DISPOSAL`)** | `不限` | `已报废` | 终态，资产不再出现在活跃台账中。 |

---

## 3. 技术栈架构

### 3.1 后端 (Backend)
*   **核心框架**：Spring Boot 3.x
*   **权限安全**：Spring Security + JWT + Redis
*   **持久层**：MyBatis
*   **模块划分**：
    *   `ruoyi-system` -> `asset` 包：包含所有的资产业务逻辑、Service 及 Mapper。
    *   `ruoyi-admin` -> `asset` 控制器：暴露 RESTful API。

### 3.2 前端 (Frontend)
*   **核心框架**：Vue 3.x (SFC, Script Setup)
*   **构建工具**：Vite
*   **样式方案**：Tailwind CSS (响应式布局与微调) + SCSS (核心设计变量)
*   **组件库**：Element Plus (二次封装)

---

## 4. 前端设计规范与封装组件
为保证后续开发风格一致，必须严格遵循现有的组件封装规范，避免直接使用 Element Plus 原生组件堆砌。

### 4.1 UI 设计风格
*   采用 **“Art Design Pro”** 风格，强调简洁明快、高信息密度的后台交互。
*   **色彩体系**：使用 SCSS 变量定义的主色调（蓝色系），背景多用浅灰/白色卡片阴影进行模块区隔（`ArtTableCard` 等）。

### 4.2 核心封装组件 (Encapsulated Components)
开发新功能时，优先使用以下预封装组件：

| 组件名称 | 路径 (src/components/core/...) | 用途说明 |
| :--- | :--- | :--- |
| **`ArtTable`** | `tables/art-table` | 统一的表格容器，已封装分页、加载状态、自适应高度。 |
| **`ArtTableHeader`** | `tables/art-table-header` | 表格上方工具栏，包含刷新、列显示控制、批量操作区域。 |
| **`ArtSearchBar`** | `forms/art-search-bar` | 统一的搜索过滤区，支持响应式折叠，配置项驱动。 |
| **`AssetPageShell`** | `views/asset/shared` | 资产业务页面的外层壳组件，包含标准的 Header 和 Footer。 |
| **`AssetRowActionBar`** | `views/asset/shared` | 表格行内操作栏，统一各模块“详情、编辑、删除”等按钮的排版。 |
| **`AssetAttachmentDrawer`**| `views/asset/shared` | 资产附件/图片上传与查看的侧边抽屉，可复用于任何关联附件的业务。 |
| **`AssetEventDrawer`** | `views/asset/shared` | 资产变更日志（生命周期记录）的侧边查看器。 |

### 4.3 核心 Composables (Hooks)
*   **`useTable`** (`src/hooks/core/useTable.ts`)：
    *   **核心功能**：封装了分页逻辑 (`pageNum`, `pageSize`)、搜索参数同步、表格 API 调用。
    *   **使用要求**：开发列表页时必须通过此 Hook 管理状态，确保搜索、刷新逻辑统一。
*   **`useAssetRoleScope`** (`src/views/asset/shared/use-asset-role-scope.ts`)：
    *   **数据隔离**：自动判断当前用户是“仅能看本人数据”还是“全量管理权限”，用于搜索条件的自动注入。

---

## 5. API 命名与权限规范

为了保持系统一致性，所有新开发的资产相关功能应遵循以下命名准则：

### 5.1 API 路径规范
后端 Controller 路径应遵循 `/asset/[module]` 格式：
*   资产台账：`/asset/info`
*   业务单据：`/asset/order`
*   报修管理：`/asset/repair`
*   盘点管理：`/asset/inventory`

### 5.2 权限标识 (Permission Tokens)
权限字符串统一采用 `asset:[module]:[action]` 格式：
*   `asset:order:list` - 分页查询
*   `asset:order:query` - 单个查询
*   `asset:order:add` - 新增
*   `asset:order:edit` - 修改
*   `asset:order:remove` - 删除
*   `asset:order:approve` - 审批通过
*   `asset:order:reject` - 审批驳回

---

## 6. 核心数据库表结构摘要

开发新功能或关联查询时请参考以下核心表：

| 表名 | 说明 | 关键字段 |
| :--- | :--- | :--- |
| `asset_info` | 资产台账表 | `asset_id`, `asset_no`, `category_id`, `asset_status`, `location_id`, `user_id` |
| `asset_category` | 资产分类表 | `category_id`, `parent_id`, `category_name`, `template_id` |
| `asset_operate_order` | 业务单据主表 | `order_id`, `order_no`, `order_type`, `order_status`, `apply_user_id` |
| `asset_operate_order_item` | 单据明细表 | `item_id`, `order_id`, `asset_id` (记录单据关联的多个资产) |
| `asset_event_log` | 资产变更日志 | `log_id`, `asset_id`, `order_id`, `event_type`, `content` |

---

## 7. 核心业务流转详解 (以单据为例)

### 7.1 单据提交与审批流
1.  **新增单据 (`DRAFT`)**：前端调用 `POST /asset/order` 创建草稿。
2.  **提交审核 (`SUBMITTED`)**：调用 `POST /asset/order/submit/{id}`，单据进入不可编辑状态，保管人此时尚未变更。
3.  **审批流程 (`APPROVING` -> `APPROVED`)**：管理员调用 `approve` 接口。
4.  **业务执行 (`FINISHED`)**：调用 `finish` 接口。**核心逻辑：** 只有在 `finish` 阶段，系统才会真正修改 `asset_info` 表中的资产状态和保管人信息，并生成 `asset_event_log`。

### 7.2 报废与维修的特殊处理
*   **报废**：报废完成后，资产在台账列表中将被过滤（状态标记为 `已报废`）。
*   **维修**：报修单完成后，系统会提示是否恢复资产为“闲置”或“在使用”。

---

## 8. 命名规范与编码建议

### 8.1 后端 Java 规范
*   **Service 层**：接口命名以 `IAsset...Service` 开头，实现类以 `Asset...ServiceImpl` 结尾。
*   **持久层**：Mapper 接口与 XML 文件名保持一致。
*   **VO/DTO**：返回给前端的聚合对象建议放置在 `domain.vo` 包下。

### 8.2 前端 Vue/TS 规范
*   **页面文件**：使用 kebab-case，例如 `asset-order-create.vue`。
*   **组件引用**：优先使用 `script setup` 语法糖。
*   **状态管理**：资产相关的临时状态建议存放在 `store/modules/asset.ts`。

---

## 9. 后续开发建议与最佳实践
1.  **单据逻辑收口**：业务逻辑应尽可能在后端 `AssetOperateOrderServiceImpl` 中完成，通过单据类型执行不同的 `AssetInfo` 更新策略。
2.  **前端页面复用**：
    *   详情展示应优先使用 `OrderDetailDrawer` (抽屉式)。
    *   复杂表单应使用独立路由页面 (`/asset/order/create`) 而非对话框。
3.  **字典依赖**：状态、分类、单据类型必须通过 `useDict` (RuoYi 字典系统) 获取，严禁前端硬编码。

## 10. 动态路由与 SQL 增量管理规范

为了保证系统功能的平滑扩展及环境一致性，本项目采用了**基于数据库驱动的动态路由**及**面向 Patch 的 SQL 增量管理**模式。

### 10.1 动态菜单与路由 (Dynamic Routes)
*   **机制**：前端不硬编码业务路由，所有菜单项、路径、权限标识均存储在 `sys_menu` 表中。
*   **加载流程**：用户登录后，前端通过 API 获取路由配置并动态挂载。
*   **开发要求**：新增页面时，必须编写对应的 SQL 语句插入 `sys_menu` 表，并分配相应的 `perms` (权限标识)。

### 10.2 SQL 补丁管理 (SQL Patch Management)
当开发涉及到数据库变更（建表、增删字段、添加字典项、配置菜单）时，必须遵循以下流程：
1.  **创建 Patch 文件**：在 `RuoYi-Vue/sql/` 目录下创建以 `asset_patch_YYYYMMDD_功能描述.sql` 命名的文件。
2.  **编写 SQL**：确保 SQL 具有幂等性（例如使用 `INSERT IGNORE` 或先 `DELETE` 后 `INSERT`），避免重复执行报错。
3.  **注册补丁**：必须在后端 `RebuildDatabase.java` 类的 `files` 数组末尾按序添加该文件名。

### 10.3 数据库重建与增量执行工具 (`RebuildDatabase`)
本项目提供了一个基于 JDBC 的自动化工具，用于一键重置或增量更新数据库环境：
*   **工具路径**：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/RebuildDatabase.java`
*   **技术原理**：使用 `MyBatis ScriptRunner` 通过 JDBC 连接顺序执行 `sql/` 目录下的所有脚本。
*   **使用场景**：
    *   **环境初始化**：新同学接入项目时，直接运行该类的 `main` 方法即可构建出最新的数据库。
    *   **增量同步**：在开发分支拉取后，如果包含新的 SQL Patch，运行该工具以确保本地库结构与代码同步。

---

## 11. 文档维护
*   **创建日期**：2026-03-29
*   **最新修订**：2026-03-29 (增加动态路由与 JDBC SQL 管理规范)
*   **作者**：Antigravity (AI Assistant)
*   **存储路径**：`docs/superpowers/specs/2026-03-29-asset-management-specification.md`
