package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetAttachment;

/**
 * 资产附件 Mapper 接口
 */
public interface AssetAttachmentMapper
{
    List<AssetAttachment> selectAssetAttachmentList(AssetAttachment attachment);

    AssetAttachment selectAssetAttachmentById(Long attachmentId);

    List<AssetAttachment> selectAssetAttachmentByIds(Long[] attachmentIds);

    int countAssetAttachmentByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    int insertAssetAttachment(AssetAttachment attachment);

    int deleteAssetAttachmentByIds(Long[] attachmentIds);
}
