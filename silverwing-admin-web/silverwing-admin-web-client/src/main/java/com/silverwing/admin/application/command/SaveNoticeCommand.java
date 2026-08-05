package com.silverwing.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存通知公告命令。
 */
@Data
@Schema(description = "保存通知公告命令")
public class SaveNoticeCommand {

    @Schema(description = "公告ID，更新时必填")
    private Long id;

    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.notice.title.required}")
    @Size(max = 50, message = "{admin.notice.title.length}")
    private String noticeTitle;

    @Schema(description = "公告类型（1-通知 2-公告）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.notice.type.required}")
    private String noticeType;

    @Schema(description = "公告内容")
    private String noticeContent;

    @Schema(description = "公告状态（0-正常 1-关闭）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
