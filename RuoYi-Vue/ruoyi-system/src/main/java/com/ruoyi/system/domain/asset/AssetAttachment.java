package com.ruoyi.system.domain.asset;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产附件对象 asset_attachment
 */
public class AssetAttachment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long attachmentId;

    @Excel(name = "业务类型")
    private String bizType;

    @Excel(name = "业务ID")
    private Long bizId;

    @Excel(name = "文件名")
    private String fileName;

    private String fileUrl;

    @Excel(name = "文件大小")
    private Long fileSize;

    @Excel(name = "文件类型")
    private String fileType;

    private Long uploadUserId;

    @Excel(name = "上传人")
    private String uploadUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上传时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    public Long getAttachmentId()
    {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId)
    {
        this.attachmentId = attachmentId;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public Long getUploadUserId()
    {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId)
    {
        this.uploadUserId = uploadUserId;
    }

    public String getUploadUserName()
    {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName)
    {
        this.uploadUserName = uploadUserName;
    }

    public Date getUploadTime()
    {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime)
    {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("attachmentId", getAttachmentId())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("fileName", getFileName())
            .append("fileUrl", getFileUrl())
            .append("fileSize", getFileSize())
            .append("fileType", getFileType())
            .append("uploadUserId", getUploadUserId())
            .append("uploadUserName", getUploadUserName())
            .append("uploadTime", getUploadTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
