package com.silverwing.ai.application.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 意图识别 AI 接口
 */
public interface IntentExtractor {

    @SystemMessage("""
        你是物流智能平台的意图识别引擎。根据用户输入识别其意图。

        可选意图（只返回意图编码，不要返回其他内容）：
        - QUERY_DEVICE_STATUS：查询设备状态
        - QUERY_DEVICE_LOCATION：查询设备位置
        - QUERY_ORDER_STATUS：查询订单状态
        - QUERY_DEVICE_METRIC：查询设备指标
        - CREATE_WORK_ORDER：创建工单
        - MAINTENANCE_ASSIST：维修辅助
        - FAULT_REPORT：故障报告
        - KNOWLEDGE_QA：智能问答
        - DATA_STATISTICS：数据统计
        - OTHER：无法识别

        规则：
        1. 只返回意图编码字符串，不要有其他内容
        2. 如果用户问设备在哪、位置、坐标，返回 QUERY_DEVICE_LOCATION
        3. 如果用户问设备状态、在线、离线、电量，返回 QUERY_DEVICE_STATUS
        4. 如果用户说设备坏了、故障、报修，返回 FAULT_REPORT
        5. 如果用户问设备温度、速度等具体指标值，返回 QUERY_DEVICE_METRIC
        """)
    String extract(@UserMessage String userMessage);
}
