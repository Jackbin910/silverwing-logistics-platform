package com.silverwing.biz.post.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import com.silverwing.common.exception.BusinessException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 岗位聚合根
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostAggregate extends DomainEntity {

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

    /** 关联用户数（派生字段，列表/导出用） */
    private Integer userCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建者 */
    private String createBy;

    /** 更新者 */
    private String updateBy;

    /**
     * 校验岗位编码是否已存在（排除自身）
     */
    public void checkPostCodeUnique(Long existingId, boolean exists) {
        if (exists) {
            throw new BusinessException(
                    com.silverwing.common.domain.ResultCode.DATA_ALREADY_EXISTS,
                    "post.code.exists");
        }
    }

    /**
     * 校验岗位名称是否已存在（排除自身）
     */
    public void checkPostNameUnique(Long existingId, boolean exists) {
        if (exists) {
            throw new BusinessException(
                    com.silverwing.common.domain.ResultCode.DATA_ALREADY_EXISTS,
                    "post.name.exists");
        }
    }
}
