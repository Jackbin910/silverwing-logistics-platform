package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典数据分页查询")
public class DictDataPageQuery extends PageRequest {

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典键值")
    private String dictValue;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "状态（0-正常 1-停用）")
    private String status;
}
