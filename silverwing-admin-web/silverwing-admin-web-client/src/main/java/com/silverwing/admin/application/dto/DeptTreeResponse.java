package com.silverwing.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门下拉树响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门下拉树响应")
public class DeptTreeResponse {

    @Schema(description = "节点ID（部门ID）")
    private Long id;

    @Schema(description = "节点名称（部门名称）")
    private String label;

    @Schema(description = "子节点")
    private List<DeptTreeResponse> children = new ArrayList<>();

    public DeptTreeResponse(Long id, String label) {
        this.id = id;
        this.label = label;
        this.children = new ArrayList<>();
    }
}
