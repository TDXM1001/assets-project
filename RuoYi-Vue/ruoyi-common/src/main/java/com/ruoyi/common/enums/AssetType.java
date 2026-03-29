package com.ruoyi.common.enums;

/**
 * 资产类型枚举
 * 对应数据库 asset_type 字典
 */
public enum AssetType
{
    /** 固定资产 */
    FIXED_ASSET("FIXED_ASSET", "固定资产"),
    
    /** 不动产 */
    REAL_ESTATE("REAL_ESTATE", "不动产"),
    
    /** 低值易耗品 */
    LOW_VALUE("LOW_VALUE", "低值易耗品");

    private final String code;
    private final String info;

    AssetType(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}
