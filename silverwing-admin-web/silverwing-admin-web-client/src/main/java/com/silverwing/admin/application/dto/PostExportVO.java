package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位导出视图对象
 * <p>字段上的 {@link ExcelProperty} 注解用于 EasyExcel 导出标题映射。</p>
 */
@Data
public class PostExportVO implements Serializable {

    @ExcelProperty("岗位编号")
    private Long id;

    @ExcelProperty("岗位编码")
    private String postCode;

    @ExcelProperty("岗位名称")
    private String postName;

    @ExcelProperty("显示顺序")
    private Integer postSort;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("关联用户数")
    private Integer userCount;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
