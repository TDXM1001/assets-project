# 分类字段模板落地

**ID：** STORY-E006-S003
**史诗：** E006 页面化与模板化能力
**优先级：** Must Have
**故事点：** 5

## 用户故事

作为产品管理员，我希望按资产分类配置字段模板和渲染规则，以便不同类型的资产可以收集不同字段，而不需要把所有差异都写死在表单里。

## 验收标准

- [x] 分类字段模板支持字段编码、字段名称、数据类型、组件类型、必填、只读、字典来源、默认值、排序、分组、版本和状态。
- [x] 模板可以按分类启用或停用。
- [x] 资产新增页面可以读取模板配置并渲染扩展字段。
- [x] 必填、只读和组件映射规则可以生效。
- [x] 模板变更不会破坏已有资产的历史数据。

## 技术说明

- 建议先采用受控模板，避免直接做成完全自由的低代码表单引擎。
- 核心报表字段继续保留在 `asset_info` 中，不要沉到动态模板里。
- 动态字段值可以先用 JSON 或扩展表承载，优先保证可落地和可维护。
- 字段组件映射应保持固定可控，避免前端出现不可预期组件。

## 依赖

- `asset_category` 分类规则字段
- `asset_info` 核心台账字段
- 资产新增页面的扩展字段接入点

## 完成定义

- [x] 代码完成
- [x] 测试通过
- [x] 文档更新
- [x] 代码审查通过
- [x] 分类模板能驱动资产新增页渲染

## 本次实现口径

- 分类模板采用 `asset_category.field_template_json + field_template_version + field_template_status` 的最小闭环。
- 资产扩展字段采用 `asset_info.extra_fields_json + template_version` 承载，避免把差异字段继续堆进固定列。
- 后端新增分类模板查询/保存接口，资产保存时会校验模板必填字段，并保留历史扩展字段键值。
- 前端新增分类字段模板弹窗和资产扩展字段渲染组件，当前支持 `input / textarea / number / select / radio / date` 六类组件。
- 本次数据库变更通过增量脚本 `RuoYi-Vue/sql/asset_patch_20260326_category_field_template.sql` 和 `RuoYi-Vue/sql/asset_patch_20260327_category_template_history.sql` 落库，不回写基线 SQL。

## 已完成校验

- [x] 后端编译：`mvn -pl ruoyi-admin,ruoyi-system -am -DskipTests compile`
- [x] 前端定点 ESLint：模板相关 API、页面和组件已通过
- [x] 本地数据库已执行分类模板增量 SQL
