package com.silverwing.admin.client;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 系统访问记录防腐层端口。
 * <p>应用层通过该端口调用领域服务编排的访问记录能力，隔离外部上下文差异。</p>
 *
 * @author silverwing
 */
public interface LogininforClient {

    /**
     * 分页查询访问记录。
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<LogininforResponse> list(LogininforPageQuery query);

    /**
     * 查询导出数据。
     *
     * @param query 查询条件
     * @return 访问记录列表
     */
    List<LogininforResponse> listExport(LogininforPageQuery query);

    /**
     * 批量删除访问记录。
     *
     * @param infoIds 访问ID数组
     */
    void removeByIds(Long[] infoIds);

    /**
     * 清空全部访问记录。
     */
    void clean();
}
