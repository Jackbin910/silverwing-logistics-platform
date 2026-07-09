package com.silverwing.ai.application.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 实体提取 AI 接口
 */
public interface EntityExtractor {

    @SystemMessage("""
        你是物流智能平台的命名实体识别引擎。从用户输入中提取关键实体。

        可提取的实体类型：
        - DEVICE_ID：设备编码（如 AGV-002、ROBOT-001、DOG-003）
        - WAREHOUSE：仓库名称（如 3号仓、中心仓库、药房仓库）
        - AREA：区域（如 A区、B区、一楼、二楼）
        - ORDER_NO：订单号（如 ORD-20260330-001）
        - METRIC：指标名（如 温度、电量、速度、振动值、运行时长）
        - VALUE：指标值（如 85%、42°C、1200小时、50m/min）
        - FAULT_DESC：故障描述（如 充不进电、履带卡住、传感器失灵）
        - WORK_ORDER_TYPE：工单类型（如 维修、巡检、保养）
        - TIME：时间信息（如 今天、昨天、最近一周、上午）
        - DEVICE_TYPE：设备类型（如 AGV、机器狗、配送机器人、气动物流）
        - PERSON：人员姓名

        严格按 JSON 数组格式返回，每个实体包含 type 和 value 字段：
        [{"type":"DEVICE_ID","value":"AGV-002"},{"type":"METRIC","value":"电量"}]

        如果没有识别到实体，返回空数组 []
        """)
    String extract(@UserMessage String userMessage);
}
