package com.silverwing.ai.service.tool;

import com.silverwing.ai.client.DeviceClient;
import com.silverwing.ai.client.OrderClient;
import com.silverwing.ai.client.WorkOrderClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于 LangChain4j @Tool 的智能助手
 * 
 * 与传统 IntentRouter 模式的区别：
 * 1. 意图路由由 LLM 自动完成，无需预定义 IntentEnum
 * 2. 工具选择由 LLM 根据 @Tool 注解描述自动判断
 * 3. 代码更简洁，扩展性更强
 * 
 * 使用方式：
 * - 注入 ToolBasedAssistant 实例
 * - 调用 chat(userMessage) 即可
 * 
 * @see DeviceTools 设备查询工具
 * @see OrderTools 订单查询工具
 */
@Slf4j
@Component
public class ToolBasedAssistant {

    private final LogisticsAssistant assistant;

    public ToolBasedAssistant(
            ChatModel chatModel,
            DeviceClient deviceClient,
            OrderClient orderClient,
            WorkOrderClient workOrderClient) {
        
        // 初始化工具实例
        DeviceTools deviceTools = new DeviceTools(deviceClient);
        OrderTools orderTools = new OrderTools(orderClient);
        WorkOrderTools workOrderTools = new WorkOrderTools(workOrderClient);

        // 构建 AiServices，绑定工具
        this.assistant = AiServices.builder(LogisticsAssistant.class)
                .chatModel(chatModel)
                .tools(deviceTools, orderTools, workOrderTools)
                .build();
    }

    /**
     * 处理用户对话
     * LLM 会自动识别用户意图并调用相应的工具
     *
     * @param userMessage 用户输入的自然语言
     * @return AI 回复
     */
    public String chat(String userMessage) {
        log.info("Tool模式处理用户消息: {}", userMessage);
        try {
            String response = assistant.chat(userMessage);
            log.info("Tool模式返回结果: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Tool模式处理失败", e);
            return "抱歉，处理您的请求时出现错误: " + e.getMessage();
        }
    }

    /**
     * AI 助手接口定义
     * 使用 @SystemMessage 定义系统提示词
     */
    @SystemMessage("""
            你是银翼物流平台的智能助手，名叫"银翼小助手"。
            
            你擅长以下任务：
            - 查询设备信息（设备编码、位置、状态）
            - 查询订单信息（订单详情、物流状态）
            - 查询工单信息（工单进度、处理状态）
            - 解答物流相关的常见问题
            
            当用户提出问题时，你应该：
            1. 准确理解用户的意图
            2. 调用合适的工具获取信息
            3. 基于工具返回的结果，用友好的方式回复用户
            
            注意：
            - 如果用户没有提供足够的信息（如设备编码），请先询问
            - 如果查询结果为空，请明确告知用户
            - 保持回答简洁、专业、易懂
            """)
    public interface LogisticsAssistant {
        String chat(String userMessage);
    }
}
