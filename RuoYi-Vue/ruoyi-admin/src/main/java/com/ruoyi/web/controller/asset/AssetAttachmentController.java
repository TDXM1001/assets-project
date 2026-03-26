package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.asset.AssetAttachment;
import com.ruoyi.system.service.asset.IAssetAttachmentService;

/**
 * 资产附件管理
 */
@RestController
@RequestMapping("/asset/attachment")
public class AssetAttachmentController extends BaseController
{
    @Autowired
    private IAssetAttachmentService assetAttachmentService;

    /**
     * 查询附件列表
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:info:query,asset:order:query,asset:inventory:query,asset:repair:query')")
    @GetMapping("/list")
    public TableDataInfo list(AssetAttachment attachment)
    {
        startPage();
        List<AssetAttachment> list = assetAttachmentService.selectAssetAttachmentList(attachment);
        return getDataTable(list);
    }

    /**
     * 上传附件
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:info:edit,asset:order:edit,asset:inventory:edit,asset:repair:edit')")
    @Log(title = "资产附件", businessType = BusinessType.INSERT)
    @PostMapping("/upload/{bizType}/{bizId}")
    public AjaxResult upload(@PathVariable String bizType, @PathVariable Long bizId,
        @RequestParam("file") MultipartFile file)
    {
        AssetAttachment attachment = assetAttachmentService.uploadAssetAttachment(bizType, bizId, file,
            getUsername(), getUserId());
        return AjaxResult.success("上传成功", attachment);
    }

    /**
     * 删除附件
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:info:edit,asset:order:edit,asset:inventory:edit,asset:repair:edit')")
    @Log(title = "资产附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachmentIds}")
    public AjaxResult remove(@PathVariable Long[] attachmentIds)
    {
        return toAjax(assetAttachmentService.deleteAssetAttachmentByIds(attachmentIds));
    }
}
