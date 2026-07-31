package com.silverwing.admin.trigger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色导出视图对象
 * <p>字段上的 {@link ExcelProperty} 注解用于 EasyExcel 导出标题映射。</p>
 */
@Data
public class RoleExportVO implements Serializable {

    @ExcelProperty("角色编号")
    private Long id;

    @ExcelProperty("角色编码")
    private String roleCode;

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("显示顺序")
    private Integer roleSort;

    @ExcelProperty("数据范围")
    private Integer dataScope;

    @ExcelProperty("角色状态")
    private Integer status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
