package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告已读用户分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公告已读用户分页查询")
public class NoticeReadUserPageQuery extends PageRequest {

    @Schema(description = "公告ID")
    private Long noticeId;

    @Schema(description = "账号或昵称模糊搜索值")
    private String searchValue;
}
