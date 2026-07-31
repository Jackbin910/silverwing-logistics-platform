package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部门响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门响应")
public class DeptResponse {

    @ExcelProperty("部门ID")
    @Schema(description = "部门ID")
    private Long deptId;

    @ExcelProperty("父部门ID")
    @Schema(description = "父部门ID")
    private Long parentId;

    @ExcelProperty("祖级列表")
    @Schema(description = "祖级列表")
    private String ancestors;

    @ExcelProperty("部门名称")
    @Schema(description = "部门名称")
    private String deptName;

    @ExcelProperty("显示顺序")
    @Schema(description = "显示顺序")
    private Integer orderNum;

    @ExcelProperty("负责人")
    @Schema(description = "负责人")
    private String leader;

    @ExcelProperty("联系电话")
    @Schema(description = "联系电话")
    private String phone;

    @ExcelProperty("邮箱")
    @Schema(description = "邮箱")
    private String email;

    @ExcelProperty("部门状态")
    @Schema(description = "部门状态（0-正常 1-停用）")
    private String status;

    @ExcelProperty("父部门名称")
    @Schema(description = "父部门名称")
    private String parentName;

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
}
