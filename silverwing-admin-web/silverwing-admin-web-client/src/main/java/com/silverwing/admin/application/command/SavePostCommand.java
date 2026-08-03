package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/更新岗位命令
 *
 * @author silverwing
 */
@Data
public class SavePostCommand {

    /** 岗位ID（更新时必填，字段名与前端 RuoYi 约定 postId 保持一致） */
    private Long postId;

    @NotBlank(message = "{validation.post.postcode.notblank}")
    private String postCode;

    @NotBlank(message = "{validation.post.postname.notblank}")
    private String postName;

    @NotNull(message = "{validation.post.postsort.notnull}")
    private Integer postSort;

    /** 状态（0 正常 1 停用） */
    private String status;

    /** 备注 */
    private String remark;
}
