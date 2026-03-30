# 不动产元数据与动态模板补丁设计规范 (Real Estate Metadata Patch Design)

## 1. 业务目标
由于不动产模块（房产、土地）与普通固定资产在核心指标（如：建筑面积、产证号、土地用途）上存在显著业务差异，需通过后台元数据配置，激活前端已实现的“分类-模板联动”功能，确保不动产台账采集的专业性与准确性。

## 2. 元数据字典定义 (SysDict)

### 2.1 建筑结构 (building_structure)
| 字典标签 | 字典键值 | 备注 |
| :--- | :--- | :--- |
| 框架结构 | FRAME | 现代办公建筑主流 |
| 钢混结构 | STEEL_CONCRETE | 高层或大型厂房 |
| 钢结构 | STEEL | 工业轻钢厂房 |
| 砖混结构 | BRICK_CONCRETE | 老旧或低层办公房 |

### 2.2 土地用途 (land_purpose)
| 字典标签 | 字典键值 | 备注 |
| :--- | :--- | :--- |
| 住宅用地 | RESIDENTIAL | |
| 商业用地 | COMMERCIAL | |
| 工业用地 | INDUSTRIAL | |
| 仓储用地 | WAREHOUSING | |

## 3. 资产分类与动态模板 (AssetCategory)

### 3.1 办公用房 (OFFICE_BUILDING)
- **资产类型**: `REAL_ESTATE`
- **动态字段 JSON 设计**:
```json
{
  "fields": [
    {
      "fieldCode": "cert_no",
      "fieldName": "权属证证号",
      "componentType": "input",
      "requiredFlag": "1",
      "groupName": "权证信息",
      "orderNum": 1
    },
    {
      "fieldCode": "building_area",
      "fieldName": "建筑面积(㎡)",
      "componentType": "number",
      "requiredFlag": "1",
      "groupName": "权证信息",
      "orderNum": 2
    },
    {
      "fieldCode": "usable_area",
      "fieldName": "套内面积(㎡)",
      "componentType": "number",
      "groupName": "权证信息",
      "orderNum": 3
    },
    {
      "fieldCode": "structure",
      "fieldName": "建筑结构",
      "componentType": "select",
      "dictType": "building_structure",
      "groupName": "物理指标",
      "orderNum": 4
    },
    {
      "fieldCode": "floors_up",
      "fieldName": "地上层数",
      "componentType": "number",
      "groupName": "物理指标",
      "orderNum": 5
    },
    {
      "fieldCode": "build_year",
      "fieldName": "建成年代",
      "componentType": "date",
      "groupName": "物理指标",
      "orderNum": 6
    }
  ]
}
```

### 3.2 工业土地 (INDUSTRIAL_LAND)
- **资产类型**: `REAL_ESTATE`
- **动态字段 JSON 设计**:
```json
{
  "fields": [
    {
      "fieldCode": "cert_no",
      "fieldName": "权属证证号",
      "componentType": "input",
      "requiredFlag": "1",
      "groupName": "权证信息",
      "orderNum": 1
    },
    {
      "fieldCode": "land_expire_date",
      "fieldName": "土地证到期日",
      "componentType": "date",
      "requiredFlag": "1",
      "groupName": "权证信息",
      "orderNum": 2
    },
    {
      "fieldCode": "land_area",
      "fieldName": "宗地面积(㎡)",
      "componentType": "number",
      "requiredFlag": "1",
      "groupName": "利用指标",
      "orderNum": 3
    },
    {
      "fieldCode": "land_purpose",
      "fieldName": "土地用途",
      "componentType": "select",
      "dictType": "land_purpose",
      "groupName": "利用指标",
      "orderNum": 4
    }
  ]
}
```

## 4. 实施顺序
1. `sys_dict_type` & `sys_dict_data` 初始化。
2. `asset_category` 插入办公用房与土地分类（注意 `asset_type` 必须为 `REAL_ESTATE`）。
3. 验证 `getCategoryFieldTemplate` API 是否返回正确 JSON。
4. 验证前端 `InfoDynamicFields` 能否根据返回的 JSON 分组渲染。
