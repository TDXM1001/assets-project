package com.ruoyi.system.service.asset;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.asset.AssetAttachment;

/**
 * 资产附件服务接口
 */
public interface IAssetAttachmentService
{
    List<AssetAttachment> selectAssetAttachmentList(AssetAttachment attachment);

    AssetAttachment uploadAssetAttachment(String bizType, Long bizId, MultipartFile file, String uploadBy, Long uploadUserId);

    int deleteAssetAttachmentByIds(Long[] attachmentIds);
}
