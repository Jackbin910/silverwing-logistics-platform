package com.silverwing.admin.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位响应对象
 *
 * @author silverwing
 */
@Data
public class PostResponse implements Serializable {

    /** 岗位ID（与前端 RuoYi 约定字段名 postId 保持一致） */
    private Long postId;

    /** 岗位编码 */
    private String postCode;

    /** 岗位名称 */
    private String postName;

    /** 显示顺序 */
    private Integer postSort;

    /** 状态（0 正常 1 停用） */
    private String status;

    /** 备注 */
    private String remark;

    /** 关联用户数 */
    private Integer userCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
