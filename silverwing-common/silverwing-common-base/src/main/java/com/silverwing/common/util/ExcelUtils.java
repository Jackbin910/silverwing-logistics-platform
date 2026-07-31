package com.silverwing.common.util;

import com.alibaba.excel.EasyExcel;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.i18n.MessageUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具类（基于 EasyExcel）。
 * <p>
 * 统一项目内 Excel 导出入口，处理响应头、文件名（国际化文案）与流写入，
 * 避免各 Controller 重复实现导出逻辑。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * 导出 Excel 并写入响应。
     * <p>文件名与 sheet 名均取自国际化文案（code 为 {@code nameKey}）。</p>
     * <p>导出过程中的 IO 异常会被捕获并转为业务异常，同时重置响应以避免向客户端
     * 返回半个损坏的 Excel 流；最终由全局异常处理器返回标准 {@code Result} 提示。</p>
     *
     * @param response 响应对象
     * @param data     数据列表
     * @param head     表头类型（含 {@code @ExcelProperty} 注解）
     * @param nameKey  国际化 code，用于文件名与 sheet 名
     * @param <T>      数据类型
     */
    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> head, String nameKey) {
        String name = MessageUtils.get(nameKey);
        String fileName = URLEncoder.encode(name, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), head)
                    .sheet(name)
                    .doWrite(data);
        } catch (IOException e) {
            // 导出失败：重置响应，避免已写入的附件头/半截流污染后续返回
            resetResponseSafely(response);
            log.error("Excel 导出失败 [nameKey={}]：{}", nameKey, e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                    "admin.export.io.error", e.getMessage());
        }
    }

    /**
     * 安全地重置响应。
     * <p>若响应尚未提交则清除已设置的头与缓冲；已提交（流已发往客户端）则无法回滚，
     * 仅记录日志，交由其自然失败。</p>
     *
     * @param response 响应对象
     */
    private static void resetResponseSafely(HttpServletResponse response) {
        if (response.isCommitted()) {
            log.warn("Excel 导出失败，但响应已提交，无法回滚为正常错误提示");
            return;
        }
        try {
            response.reset();
        } catch (IllegalStateException ex) {
            log.warn("Excel 导出失败，响应重置被拒绝：{}", ex.getMessage());
        }
    }
}
