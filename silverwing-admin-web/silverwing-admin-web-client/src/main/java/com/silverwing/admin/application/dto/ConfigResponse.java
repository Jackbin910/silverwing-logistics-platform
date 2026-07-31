package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 参数配置响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "参数配置响应")
public class ConfigResponse {

    @ExcelProperty("参数编号")
    @Schema(description = "参数主键")
    private Long id;

    @ExcelProperty("参数名称")
    @Schema(description = "参数名称")
    private String configName;

    @ExcelProperty("参数键名")
    @Schema(description = "参数键名")
    private String configKey;

    @ExcelProperty("参数键值")
    @Schema(description = "参数键值")
    private String configValue;

    @ExcelProperty("系统内置")
    @Schema(description = "系统内置（Y-是 N-否）")
    private String configType;

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
