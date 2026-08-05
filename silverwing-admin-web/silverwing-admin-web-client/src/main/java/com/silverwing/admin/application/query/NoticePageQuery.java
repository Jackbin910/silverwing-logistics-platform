package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知公告分页查询")
public class NoticePageQuery extends PageRequest {

    @Schema(description = "公告标题")
    private String noticeTitle;

    @Schema(description = "公告类型（1-通知 2-公告）")
    private String noticeType;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "公告状态（0-正常 1-关闭）")
    private Integer status;
}
