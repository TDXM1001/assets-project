# Architecture: 资产管理系统

Generated on 2026-03-26

## 0. 文档信息

| 项目 | 内容 |
|---|---|
| 产品名称 | 资产管理系统 |
| 项目级别 | Level 2 |
| 规划阶段 | BMAD / Phase 3 |
| 架构风格 | Modular Monolith + Layered Architecture |
| 后端技术栈 | Java 17, Spring Boot 3.5.11, Spring Security, MyBatis, Druid, PageHelper, Springdoc, POI |
| 前端技术栈 | Vue 3.5, Vite 7, TypeScript, Element Plus, Pinia, Vue Router, ECharts |
| 数据存储 | MySQL-compatible relational database, current environment: `my-assets` |
| 输入文档 | [PRD](./prd-资产管理系统-2026-03-26.md), [产品方案](./资产管理系统-产品方案.md), [开发任务拆分清单](./资产管理系统-开发任务拆分清单.md), [业务流程演练稿](./资产管理系统-业务流程演练稿.md), [菜单与权限设计](./资产管理系统-菜单与权限设计.md) |

## 1. 架构结论

这套系统不适合做微服务拆分，也不需要为资产模块单独引入事件总线或 BPM 引擎。当前最合适的形态是：

1. 以 `RuoYi-Vue` 为后端基础的模块化单体。
2. 以 `art-design-pro` 为前端基础的动态菜单驱动应用。
3. 以“主档 + 单据 + 流水 + 看板”作为领域骨架。
4. 以服务层事务保证状态变更一致性。

当前仓库已经存在资产模块的实际实现，因此本架构稿的目标不是重新发明系统，而是把已经存在的模块边界、接口契约、数据责任和权限口径正式冻结。

## 2. 系统上下文

```text
Browser
  -> Vue 3 / Element Plus UI
  -> REST API
  -> Spring Boot Asset Module
  -> MyBatis / Transactional Service Layer
  -> MySQL + File Storage
```

### 关键外部依赖

1. 认证与授权：若依 `sys_menu`、`sys_role`、`sys_role_menu`、`sys_user_role`、`@PreAuthorize`。
2. 数据字典：若依字典体系，前端通过字典标签渲染状态与类型。
3. 文件上传：复用若依现有文件能力，资产附件只是业务关联层。
4. 操作日志：复用若依 `@Log`。
5. 导入导出：复用若依 `ExcelUtil` 和 POI。

## 3. 当前代码边界

### 后端包结构

- Controller: `com.ruoyi.web.controller.asset`
- Service: `com.ruoyi.system.service.asset`
- Mapper: `com.ruoyi.system.mapper.asset`
- Domain / VO: `com.ruoyi.system.domain.asset`

### 已存在的控制器

1. `AssetDashboardController`
2. `AssetCategoryController`
3. `AssetLocationController`
4. `AssetInfoController`
5. `AssetOrderController`
6. `AssetInventoryController`
7. `AssetRepairController`
8. `AssetEventController`
9. `AssetAttachmentController`

### 前端页面结构

- 页面目录：`art-design-pro/src/views/asset/*`
- 共享能力：`art-design-pro/src/views/asset/shared/*`
- 现有页面：`dashboard`, `category`, `location`, `info`, `order`, `inventory`, `repair`, `event`

### 重要前端约束

1. 资产模块不再新增静态路由文件来承载菜单。
2. 菜单由后端 `sys_menu` 驱动，前端走现有动态路由管线。
3. 页面内按钮权限统一用 `v-auth`，角色与数据范围统一用共享 composable。

## 4. 领域划分

### 4.1 核心聚合

| 领域 | 聚合 / 实体 | 职责 | 当前状态 |
|---|---|---|---|
| 分类 | `AssetCategory` | 维护分类树、规则和统计口径 | 已实现 |
| 位置 | `AssetLocation` | 维护存放位置树 | 已实现 |
| 台账 | `AssetInfo` | 资产当前快照 | 已实现 |
| 单据 | `AssetOperateOrder` + `AssetOperateOrderItem` | 入库、领用、归还、调拨、处置等通用业务动作 | 已实现 |
| 盘点 | `AssetInventoryTask` + `AssetInventoryTaskItem` | 批量校验账实差异 | 已实现 |
| 维修 | `AssetRepairOrder` | 维修申请、审批、完成 | 已实现 |
| 流水 | `AssetEventLog` | 记录前后值、操作者和来源单据 | 已实现 |
| 附件 | `AssetAttachment` | 图片、单据、证明、标签等业务附件 | 已实现 |
| 看板 | 读模型 | 汇总指标、待办、趋势 | 已实现 |

### 4.2 领域之间的关系

1. `AssetCategory` 和 `AssetLocation` 为 `AssetInfo` 提供基础维度。
2. `AssetInfo` 保存资产的当前状态快照，不保存完整历史。
3. `AssetOperateOrder` 负责驱动资产生命周期变化。
4. `AssetInventoryTask` 负责批量校验与差异处理，不等同于普通单据。
5. `AssetRepairOrder` 是独立聚合，因为维修状态和审批路径与通用单据不同。
6. `AssetEventLog` 是所有关键变化的审计主线，采用追加式写入。
7. `AssetAttachment` 通过 `bizType + bizId` 关联到不同业务对象。

### 4.3 状态分层

| 状态层 | 说明 | 责任对象 |
|---|---|---|
| 资产状态 | 当前物理/责任状态，如闲置、在用、借出、维修中、已报废 | `AssetInfo` |
| 单据状态 | 单据流程，如草稿、已提交、已审批、已完成、已取消 | `AssetOperateOrder` / `AssetRepairOrder` |
| 盘点状态 | 任务流程，如草稿、进行中、已结束；明细处理状态如待处理、已处理 | `AssetInventoryTask` / `AssetInventoryTaskItem` |
| 审计状态 | 流水记录是否已写入，不做回写修改 | `AssetEventLog` |

## 5. 数据架构

### 5.1 核心表

| 表 | 类型 | 关键字段 | 作用 |
|---|---|---|---|
| `asset_category` | 树表 | `parent_id`, `category_code`, `status` | 分类规则与统计口径 |
| `asset_location` | 树表 | `parent_id`, `location_code`, `status` | 存放位置与组织空间 |
| `asset_info` | 主档表 | `asset_code`, `category_id`, `location_id`, `dept_id`, `user_id`, `asset_status` | 资产当前快照 |
| `asset_operate_order` | 头表 | `order_type`, `order_status`, `biz_type`, `biz_id` | 生命周期动作头单 |
| `asset_operate_order_item` | 明细表 | `before_*`, `after_*` | 前后值与变更范围 |
| `asset_inventory_task` | 头表 | `task_no`, `task_status`, `scope_type` | 盘点任务 |
| `asset_inventory_task_item` | 明细表 | `expected_*`, `actual_*`, `inventory_result`, `process_status` | 盘点差异明细 |
| `asset_repair_order` | 业务表 | `repair_status`, `approve_*`, `finish_*` | 维修流程 |
| `asset_event_log` | 流水表 | `event_type`, `source_order_type`, `source_order_id`, `before_snapshot`, `after_snapshot` | 审计追踪 |
| `asset_attachment` | 关联表 | `biz_type`, `biz_id`, `file_url` | 附件管理 |

### 5.2 索引建议

为满足列表分页、树查询和看板统计，建议确保以下字段具备合适索引：

1. `asset_info.asset_code`
2. `asset_info.category_id`
3. `asset_info.location_id`
4. `asset_info.dept_id`
5. `asset_info.user_id`
6. `asset_info.asset_status`
7. `asset_operate_order.order_no`
8. `asset_operate_order.order_type`
9. `asset_operate_order.order_status`
10. `asset_inventory_task.task_no`
11. `asset_event_log.asset_id`
12. `asset_event_log.event_time`

### 5.3 写入原则

1. 业务单据先落库，再驱动主档变化。
2. 完成动作与回写主档必须在同一事务内完成。
3. `asset_event_log` 只能追加，不能就地改写。
4. 不把审批状态和资产状态混在一张字段里。

## 6. 服务与事务设计

### 6.1 事务边界

当前服务层已经在关键命令方法上使用 `@Transactional(rollbackFor = Exception.class)`，这部分应作为架构基线保留。

| 服务 | 关键命令 | 事务责任 |
|---|---|---|
| `AssetOperateOrderServiceImpl` | `insert/update/delete/submit/approve/reject/finish/cancel` | 保证单据状态机和主档回写一致 |
| `AssetInventoryTaskServiceImpl` | `insert/update/delete/start/scan/finish/processDiff` | 保证盘点任务、明细和回写一致 |
| `AssetInfoServiceImpl` | `insert/update/delete/import` | 保证台账唯一性和导入一致性 |
| `AssetRepairOrderServiceImpl` | `insert/update/delete/submit/approve/reject/finish/cancel` | 保证维修流程状态一致 |

### 6.2 状态机

#### 通用业务单据

`DRAFT -> SUBMITTED -> APPROVED -> FINISHED`

补充分支：

- `DRAFT -> REJECTED`
- `DRAFT / SUBMITTED -> CANCELED`

#### 盘点任务

`DRAFT -> RUNNING -> FINISHED`

盘点明细通过 `processStatus` 管理处理结果，不用单据状态替代明细状态。

#### 维修单

`DRAFT -> SUBMITTED -> APPROVED -> FINISHED`

补充分支同样支持驳回和取消。

### 6.3 关键业务规则

1. 资产主档只保留当前值。
2. 每次关键变更都写事件流水。
3. 盘点差异不是简单改单，而是先记录差异、再处理差异、最后回写主档。
4. 调拨不一定改变资产物理状态，但一定会改变归属和责任信息。

## 7. API 设计

### 7.1 资源分组

| 前缀 | 资源 |
|---|---|
| `/asset/dashboard` | 汇总、待办、趋势 |
| `/asset/category` | 分类 |
| `/asset/location` | 位置 |
| `/asset/info` | 资产台账 |
| `/asset/order` | 通用业务单据 |
| `/asset/inventory` | 盘点任务 |
| `/asset/repair` | 维修单 |
| `/asset/event` | 事件流水 |
| `/asset/attachment` | 业务附件 |

### 7.2 典型动作接口

| 资源 | 典型动作 |
|---|---|
| `dashboard` | `summary`, `todo`, `trend` |
| `info` | `list`, `query`, `add`, `edit`, `remove`, `export`, `import`, `importTemplate` |
| `order` | `list`, `query`, `add`, `edit`, `remove`, `submit`, `approve`, `reject`, `finish`, `cancel`, `export`, `linked` |
| `inventory` | `list`, `query`, `add`, `edit`, `remove`, `start`, `scan`, `finish`, `processDiff`, `export` |
| `repair` | `list`, `query`, `add`, `edit`, `remove`, `submit`, `approve`, `reject`, `finish`, `cancel`, `export` |
| `event` | `list`, `query`, `asset/{assetId}`, `export` |
| `attachment` | `list`, `upload/{bizType}/{bizId}`, `remove` |

### 7.3 接口安全约定

1. 后端接口使用 `@PreAuthorize("@ss.hasPermi('asset:module:action')")`。
2. 所有按钮权限与接口权限同名。
3. 事件流水和附件查询必须以业务权限为前提，不单独开洞。

## 8. 前端架构

### 8.1 页面结构

| 页面 | 职责 |
|---|---|
| `views/asset/dashboard/index.vue` | 资产运营看板 |
| `views/asset/category/index.vue` | 分类管理 |
| `views/asset/location/index.vue` | 位置管理 |
| `views/asset/info/index.vue` | 资产台账 |
| `views/asset/order/index.vue` | 业务单据 |
| `views/asset/inventory/index.vue` | 盘点任务 |
| `views/asset/repair/index.vue` | 维修管理 |
| `views/asset/event/index.vue` | 资产流水 |

### 8.2 共享组件与组合式逻辑

| 文件 | 作用 |
|---|---|
| `views/asset/shared/use-asset-role-scope.ts` | 统一资产角色口径和数据范围能力 |
| `views/asset/shared/asset-event-drawer.vue` | 流水详情抽屉 |
| `views/asset/shared/asset-attachment-drawer.vue` | 附件抽屉 |
| `views/asset/info/modules/info-filter-tree.vue` | 分类 / 位置树筛选 |

### 8.3 UI 模式

1. 列表页：筛选条 + 表格 + 操作区。
2. 详情页：抽屉承载主信息和关联信息。
3. 新建/编辑/审批：弹窗表单。
4. 看板页：卡片 + 待办 + 趋势图表。

### 8.4 路由策略

资产模块不再使用独立静态路由文件来硬编码页面入口，而是通过后端菜单树进入前端动态路由管线。这一点对资产模块尤为重要，因为菜单、权限和页面入口会随着业务收敛持续调整。

### 8.5 角色范围前端控制

前端已经存在 `useAssetRoleScope`，用于统一以下场景：

1. 区分资产管理员、部门主管、资产使用人、审计角色。
2. 控制“本人范围”视图。
3. 控制分类盘点、位置盘点等特定范围能力。

## 9. 安全与数据范围

### 9.1 权限层级

1. 功能权限：菜单、按钮、接口。
2. 数据权限：全部、本部门及以下、本人相关。
3. 业务动作权限：提交、审批、驳回、完成、差异处理。

### 9.2 默认数据范围

| 角色 | 默认范围 |
|---|---|
| 超级管理员 | 全部数据 |
| 资产管理员 | 全部数据 |
| 部门主管 | 本部门及子部门 |
| 资产使用人 | 本人相关资产 + 本人发起单据 |
| 财务/审计 | 全部数据，只读或受限操作 |

### 9.3 安全实现

1. 后端用 `@PreAuthorize` 确保接口不可越权调用。
2. 前端用 `v-auth` 隐藏无权限按钮。
3. SQL 层补充数据范围过滤，不能只靠按钮隐藏。
4. 附件上传做文件类型和大小约束，避免业务污染。
5. 审计通过 `asset_event_log` 和若依操作日志双通道留痕。

## 10. 非功能设计

### 10.1 性能

1. 所有列表接口分页查询。
2. 台账和流水查询使用合适索引。
3. 看板拆成 summary / todo / trend 三类聚合接口，避免一个超大接口。
4. 导出走独立导出动作，避免和分页查询混在一起。

### 10.2 一致性与可靠性

1. 单据完成、盘点处理、维修完成等状态转换都在事务内处理。
2. 任一步骤失败都整体回滚。
3. 任何关键动作都要先做状态前置校验，再写主档，再写流水。
4. 业务异常优先用 `ServiceException` 快速失败，避免半成功半失败。

### 10.3 可维护性

1. 模块以 `category / location / info / order / inventory / repair / event / dashboard / attachment` 划分。
2. 前后端权限命名统一为 `asset:module:action`。
3. 状态码、字典、业务常量统一收敛到共享定义。
4. 不在一期引入 BPM 引擎，减少状态机复杂度。

### 10.4 可观测性

1. 关键命令接口保留若依 `@Log`。
2. 事件流水作为业务审计主证据。
3. 看板用于暴露待办、异常和趋势，不作为唯一审计依据。

## 11. 设计决策与取舍

### 已确定决策

1. 采用模块化单体，不拆微服务。
2. 采用动态菜单驱动前端入口，不新增静态资产路由模块。
3. 采用通用业务单据承载入库、领用、调拨、归还、处置。
4. 盘点独立成聚合，不混在普通单据里。
5. 维修独立成聚合，保留独立状态机和审批路径。
6. 事件流水作为追加式审计链，不做覆盖式编辑。

### 主要取舍

| 取舍 | 选择 | 原因 |
|---|---|---|
| 微服务 vs 单体 | 单体 | 资产模块当前规模和团队节奏更适合快速迭代 |
| 多单据入口 vs 通用单据 | 通用单据 | 避免入口过多导致权限和菜单膨胀 |
| 盘点合并到单据 | 不合并 | 盘点的业务语义是校验差异，不是发起动作 |
| 维修独立还是通用 | 独立 | 维修审批与结束动作不同于通用单据 |
| 静态路由 vs 动态路由 | 动态路由 | 与若依菜单系统一致，减少配置漂移 |

## 12. 需求覆盖与追踪

| PRD 需求组 | 架构覆盖点 |
|---|---|
| FR-01 ~ FR-03 | 分类/位置/台账数据模型与页面分层 |
| FR-04 ~ FR-08 | 单据、维修、事件流水、事务边界 |
| FR-09 ~ FR-10 | 盘点任务与差异处理模型 |
| FR-11 ~ FR-12 | 看板读模型与事件流水查询 |
| FR-13 ~ FR-14 | 菜单、角色、权限、导出和数据范围 |
| NFR-01 | 索引、分页、聚合接口拆分 |
| NFR-02 | 事件流水追加式记录 |
| NFR-03 | 事务与状态机 |
| NFR-04 | 前后端权限 + SQL 数据范围 |
| NFR-05 | 前端空态 / 异常态 / 加载态 |
| NFR-06 | 模块边界与统一命名 |

## 13. 风险与待确认问题

### 风险

1. “本人范围”如果只做前端过滤，容易出现越权查询。
2. 如果把盘点差异处理简化成直接改台账，会丢失审计证据。
3. 如果后续再把单据拆得过细，菜单和权限维护成本会快速上升。

### 待确认

1. 维修审批是否归部门主管还是财务/审计。
2. 盘点差异处理是否允许部门主管直接处理。
3. 看板是否按角色展示不同卡片组合。

## 14. 下一步

1. 先跑 `/solutioning-gate-check`，确认架构没有阻塞项。
2. 然后进入 `/sprint-planning`，把现有资产模块拆成可执行故事。
3. 若需要继续收口，再补开发计划与故事级别的实施约束。
