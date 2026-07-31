package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字典数据响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典数据响应")
public class DictDataResponse {

    @ExcelProperty("字典编码")
    @Schema(description = "字典数据主键")
    private Long id;

    @ExcelProperty("字典排序")
    @Schema(description = "字典排序")
    private Long dictSort;

    @ExcelProperty("字典标签")
    @Schema(description = "字典标签")
    private String dictLabel;

    @ExcelProperty("字典键值")
    @Schema(description = "字典键值")
    private String dictValue;

    @ExcelProperty("字典类型")
    @Schema(description = "字典类型")
    private String dictType;

    @ExcelProperty("样式属性")
    @Schema(description = "样式属性（其他样式扩展）")
    private String cssClass;

    @ExcelProperty("表格回显样式")
    @Schema(description = "表格字典样式")
    private String listClass;

    @ExcelProperty("是否默认")
    @Schema(description = "是否默认（Y-是 N-否）")
    private String isDefault;

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
