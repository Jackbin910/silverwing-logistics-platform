package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知公告响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知公告响应")
public class NoticeResponse {

    @ExcelProperty("公告编号")
    @Schema(description = "公告ID")
    private Long id;

    @ExcelProperty("公告标题")
    @Schema(description = "公告标题")
    private String noticeTitle;

    @ExcelProperty("公告类型")
    @Schema(description = "公告类型（1-通知 2-公告）")
    private String noticeType;

    /** 公告内容通常为富文本，不参与 Excel 导出 */
    @ExcelIgnore
    @Schema(description = "公告内容")
    private String noticeContent;

    @ExcelProperty("状态")
    @Schema(description = "公告状态（0-正常 1-关闭）")
    private Integer status;

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
