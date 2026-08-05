package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统访问记录分页查询条件。
 * <p>承载前端列表查询入参，含账号、IP、状态与时间区间过滤。</p>
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogininforPageQuery extends PageRequest {

    @Schema(description = "用户账号（模糊）")
    private String userName;

    @Schema(description = "登录IP（模糊）")
    private String ipaddr;

    @Schema(description = "登录状态（0成功 1失败）")
    private Integer status;

    @Schema(description = "访问时间-开始")
    private String beginTime;

    @Schema(description = "访问时间-结束")
    private String endTime;
}
