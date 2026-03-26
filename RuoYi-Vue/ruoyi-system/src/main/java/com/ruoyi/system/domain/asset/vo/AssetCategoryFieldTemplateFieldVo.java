package com.ruoyi.system.domain.asset.vo;

import java.io.Serializable;

/**
 * 分类字段模板明细
 */
public class AssetCategoryFieldTemplateFieldVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String fieldCode;

    private String fieldName;

    private String dataType;

    private String componentType;

    private String requiredFlag;

    private String readonlyFlag;

    private String dictType;

    private String defaultValue;

    private Integer orderNum;

    private String groupName;

    private String status;

    public String getFieldCode()
    {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode)
    {
        this.fieldCode = fieldCode;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getComponentType()
    {
        return componentType;
    }

    public void setComponentType(String componentType)
    {
        this.componentType = componentType;
    }

    public String getRequiredFlag()
    {
        return requiredFlag;
    }

    public void setRequiredFlag(String requiredFlag)
    {
        this.requiredFlag = requiredFlag;
    }

    public String getReadonlyFlag()
    {
        return readonlyFlag;
    }

    public void setReadonlyFlag(String readonlyFlag)
    {
        this.readonlyFlag = readonlyFlag;
    }

    public String getDictType()
    {
        return dictType;
    }

    public void setDictType(String dictType)
    {
        this.dictType = dictType;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
