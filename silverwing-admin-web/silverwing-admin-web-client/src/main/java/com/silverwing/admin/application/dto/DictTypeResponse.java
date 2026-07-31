package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字典类型响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典类型响应")
public class DictTypeResponse {

    @ExcelProperty("字典编号")
    @Schema(description = "字典主键")
    private Long id;

    @ExcelProperty("字典名称")
    @Schema(description = "字典名称")
    private String dictName;

    @ExcelProperty("字典类型")
    @Schema(description = "字典类型")
    private String dictType;

    @ExcelProperty("状态")
    @Schema(description = "状态（0-正常 1-停用）")
    private String status;

    @ExcelProperty("创建者")
    @Schema(description = "创建者")
    private String createBy;

    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("更新者")
    @Schema(description = "更新者")
    private String updateBy;

    @ExcelProperty("更新时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @ExcelProperty("备注")
    @Schema(description = "备注")
    private String remark;
}
