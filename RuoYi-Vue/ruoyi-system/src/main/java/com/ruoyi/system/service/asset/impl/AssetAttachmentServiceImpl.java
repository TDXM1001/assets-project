package com.ruoyi.system.service.asset.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.system.domain.asset.AssetAttachment;
import com.ruoyi.system.mapper.asset.AssetAttachmentMapper;
import com.ruoyi.system.mapper.asset.AssetOperateOrderMapper;
import com.ruoyi.system.mapper.asset.AssetRepairOrderMapper;
import com.ruoyi.system.service.asset.IAssetAttachmentService;
import com.ruoyi.system.service.asset.IAssetInfoService;
import com.ruoyi.system.service.asset.IAssetInventoryTaskService;
import com.ruoyi.system.service.asset.IAssetOperateOrderService;
import com.ruoyi.system.service.asset.IAssetRepairOrderService;

/**
 * 资产附件服务实现
 */
@Service
public class AssetAttachmentServiceImpl implements IAssetAttachmentService
{
    private static final String BIZ_ASSET_INFO = "ASSET_INFO";
    private static final String BIZ_ASSET_ORDER = "ASSET_ORDER";
    private static final String BIZ_ASSET_INVENTORY = "ASSET_INVENTORY";
    private static final String BIZ_ASSET_REPAIR = "ASSET_REPAIR";

    @Autowired
    private AssetAttachmentMapper assetAttachmentMapper;

    @Autowired
    private AssetOperateOrderMapper assetOperateOrderMapper;

    @Autowired
    private AssetRepairOrderMapper assetRepairOrderMapper;

    @Autowired
    private IAssetInfoService assetInfoService;

    @Autowired
    private IAssetOperateOrderService assetOperateOrderService;

    @Autowired
    private IAssetInventoryTaskService assetInventoryTaskService;

    @Autowired
    private IAssetRepairOrderService assetRepairOrderService;

    @Override
    public List<AssetAttachment> selectAssetAttachmentList(AssetAttachment attachment)
    {
        validateBizQuery(attachment.getBizType(), attachment.getBizId());
        return assetAttachmentMapper.selectAssetAttachmentList(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetAttachment uploadAssetAttachment(String bizType, Long bizId, MultipartFile file, String uploadBy,
        Long uploadUserId)
    {
        validateBizWrite(bizType, bizId);
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传附件不能为空");
        }

        String filePath = null;
        try
        {
            filePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);

            AssetAttachment attachment = new AssetAttachment();
            attachment.setBizType(bizType);
            attachment.setBizId(bizId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileUrl(getServerUrl() + filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(FileUploadUtils.getExtension(file));
            attachment.setUploadUserId(uploadUserId);
            attachment.setUploadTime(new Date());
            attachment.setCreateBy(uploadBy);

            if (assetAttachmentMapper.insertAssetAttachment(attachment) <= 0)
            {
                throw new ServiceException("附件信息落库失败");
            }

            syncAttachmentCount(bizType, bizId);
            return attachment;
        }
        catch (Exception e)
        {
            if (StringUtils.isNotBlank(filePath))
            {
                deletePhysicalFile(filePath);
            }
            throw e instanceof ServiceException ? (ServiceException) e : new ServiceException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetAttachmentByIds(Long[] attachmentIds)
    {
        if (attachmentIds == null || attachmentIds.length == 0)
        {
            throw new ServiceException("请选择需要删除的附件");
        }

        List<AssetAttachment> attachments = assetAttachmentMapper.selectAssetAttachmentByIds(attachmentIds);
        if (attachments == null || attachments.isEmpty())
        {
            return 0;
        }

        for (AssetAttachment attachment : attachments)
        {
            validateBizWrite(attachment.getBizType(), attachment.getBizId());
        }

        int rows = assetAttachmentMapper.deleteAssetAttachmentByIds(attachmentIds);
        if (rows > 0)
        {
            deletePhysicalFiles(attachments);
            refreshAttachmentCount(attachments);
        }
        return rows;
    }

    /**
     * 附件列表查询只允许访问可见业务的附件。
     */
    private void validateBizQuery(String bizType, Long bizId)
    {
        if (StringUtils.isBlank(bizType) || bizId == null)
        {
            throw new ServiceException("业务类型和业务ID不能为空");
        }
        requireVisibleBiz(bizType, bizId);
    }

    /**
     * 上传和删除都要先确认父业务本身对当前用户可见。
     */
    private void validateBizWrite(String bizType, Long bizId)
    {
        validateBizQuery(bizType, bizId);
    }

    private void requireVisibleBiz(String bizType, Long bizId)
    {
        String normalizedBizType = StringUtils.upperCase(StringUtils.trim(bizType));
        switch (normalizedBizType)
        {
            case BIZ_ASSET_INFO:
                if (StringUtils.isNull(assetInfoService.selectAssetInfoById(bizId)))
                {
                    throw new ServiceException("资产不存在或无权限访问");
                }
                return;
            case BIZ_ASSET_ORDER:
                if (StringUtils.isNull(assetOperateOrderService.selectAssetOperateOrderById(bizId)))
                {
                    throw new ServiceException("业务单据不存在或无权限访问");
                }
                return;
            case BIZ_ASSET_INVENTORY:
                if (StringUtils.isNull(assetInventoryTaskService.selectAssetInventoryTaskById(bizId)))
                {
                    throw new ServiceException("盘点任务不存在或无权限访问");
                }
                return;
            case BIZ_ASSET_REPAIR:
                if (StringUtils.isNull(assetRepairOrderService.selectAssetRepairOrderById(bizId)))
                {
                    throw new ServiceException("维修单不存在或无权限访问");
                }
                return;
            default:
                throw new ServiceException("不支持的业务类型：" + bizType);
        }
    }

    private void syncAttachmentCount(String bizType, Long bizId)
    {
        String normalizedBizType = StringUtils.upperCase(bizType);
        if (StringUtils.equals(normalizedBizType, BIZ_ASSET_ORDER))
        {
            int count = assetAttachmentMapper.countAssetAttachmentByBiz(BIZ_ASSET_ORDER, bizId);
            assetOperateOrderMapper.updateAttachmentCount(bizId, count);
            return;
        }
        if (StringUtils.equals(normalizedBizType, BIZ_ASSET_REPAIR))
        {
            int count = assetAttachmentMapper.countAssetAttachmentByBiz(BIZ_ASSET_REPAIR, bizId);
            assetRepairOrderMapper.updateAttachmentCount(bizId, count);
        }
    }

    private void refreshAttachmentCount(List<AssetAttachment> attachments)
    {
        Map<Long, Long> orderBizMap = attachments.stream()
            .filter(item -> StringUtils.equals(StringUtils.upperCase(item.getBizType()), BIZ_ASSET_ORDER))
            .filter(item -> item.getBizId() != null)
            .collect(Collectors.toMap(
                AssetAttachment::getBizId,
                AssetAttachment::getBizId,
                (left, right) -> left));

        for (Long orderId : orderBizMap.keySet())
        {
            int count = assetAttachmentMapper.countAssetAttachmentByBiz(BIZ_ASSET_ORDER, orderId);
            assetOperateOrderMapper.updateAttachmentCount(orderId, count);
        }

        Map<Long, Long> repairBizMap = attachments.stream()
            .filter(item -> StringUtils.equals(StringUtils.upperCase(item.getBizType()), BIZ_ASSET_REPAIR))
            .filter(item -> item.getBizId() != null)
            .collect(Collectors.toMap(
                AssetAttachment::getBizId,
                AssetAttachment::getBizId,
                (left, right) -> left));

        for (Long repairId : repairBizMap.keySet())
        {
            int count = assetAttachmentMapper.countAssetAttachmentByBiz(BIZ_ASSET_REPAIR, repairId);
            assetRepairOrderMapper.updateAttachmentCount(repairId, count);
        }
    }

    private void deletePhysicalFiles(List<AssetAttachment> attachments)
    {
        for (AssetAttachment attachment : attachments)
        {
            if (StringUtils.isBlank(attachment.getFileUrl()))
            {
                continue;
            }
            deletePhysicalFile(FileUtils.stripPrefix(attachment.getFileUrl()));
        }
    }

    private void deletePhysicalFile(String filePath)
    {
        try
        {
            FileUtils.deleteFile(RuoYiConfig.getProfile() + filePath);
        }
        catch (Exception e)
        {
            // 文件清理失败不应阻断业务提交，数据库记录一致性优先。
        }
    }

    private String getServerUrl()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath)
            .toString();
    }
}
