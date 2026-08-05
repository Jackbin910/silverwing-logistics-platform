package com.silverwing.ai.application.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 重命名会话命令
 *
 * @author silverwing
 */
@Data
public class RenameConversationCommand {

    /**
     * 新标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
}
