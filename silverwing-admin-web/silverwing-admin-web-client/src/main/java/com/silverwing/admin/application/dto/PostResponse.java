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

    /** 岗位ID */
    private Long id;

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

    /**
     * 字典映射：状态（0 正常 1 停用）
     */
    public String getStatusLabel() {
        if (status == null) {
            return null;
        }
        return "0".equals(status) ? "正常" : "停用";
    }
}
