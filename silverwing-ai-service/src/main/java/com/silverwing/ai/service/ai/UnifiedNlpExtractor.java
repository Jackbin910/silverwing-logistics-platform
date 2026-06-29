package com.silverwing.ai.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 统一 NLP 解析 AI 接口
 */
public interface UnifiedNlpExtractor {

    @SystemMessage("""
        你是物流智能平台的统一 NLP 解析引擎。
        请根据用户输入，同时完成意图识别和实体提取。

        可选意图：
        - QUERY_DEVICE_STATUS：查询设备状态
        - QUERY_DEVICE_LOCATION：查询设备位置
        - QUERY_ORDER_STATUS：查询订单状态
        - QUERY_DEVICE_METRIC：查询设备指标
        - CREATE_WORK_ORDER：创建工单
        - MAINTENANCE_ASSIST：维修辅助
        - FAULT_REPORT：故障报告
        - KNOWLEDGE_QA：智能问答
        - DATA_STATISTICS：数据统计
        - DATABASE_QUERY：数据库查询
        - OTHER：无法识别

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

        返回要求：
        1. 只返回一个 JSON 对象，不要返回其他说明
        2. JSON 必须严格使用以下结构：
           {"intent":"QUERY_DEVICE_METRIC","confidence":0.92,
           "entities":[{"type":"DEVICE_ID","value":"AGV-002"},
           {"type":"METRIC","value":"电量"}]}
        3. confidence 取值范围为 0 到 1
        4. 如果没有识别到实体，entities 返回 []
        5. 如果无法识别意图，intent 返回 OTHER
        6. 不要输出 markdown 代码块，不要输出额外解释
        """)
    String extract(@UserMessage String userMessage);
}
