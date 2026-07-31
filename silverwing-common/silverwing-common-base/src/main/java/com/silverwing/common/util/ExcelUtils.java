package com.silverwing.common.util;

import com.alibaba.excel.EasyExcel;
import com.silverwing.common.i18n.MessageUtils;
import jakarta.servlet.http.HttpServletResponse;

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
public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * 导出 Excel 并写入响应。
     * <p>文件名与 sheet 名均取自国际化文案（code 为 {@code nameKey}）。</p>
     *
     * @param response 响应对象
     * @param data     数据列表
     * @param head     表头类型（含 {@code @ExcelProperty} 注解）
     * @param nameKey  国际化 code，用于文件名与 sheet 名
     * @param <T>      数据类型
     * @throws IOException IO 异常
     */
    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> head, String nameKey)
            throws IOException {
        String name = MessageUtils.get(nameKey);
        String fileName = URLEncoder.encode(name, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), head)
                .sheet(name)
                .doWrite(data);
    }
}
