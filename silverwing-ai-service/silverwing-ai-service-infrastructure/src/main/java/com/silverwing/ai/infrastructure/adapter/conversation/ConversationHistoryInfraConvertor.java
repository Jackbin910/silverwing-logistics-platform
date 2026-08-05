package com.silverwing.ai.infrastructure.adapter.conversation;

import cn.hutool.json.JSONUtil;
import com.silverwing.ai.domain.model.ConversationMessage;
import com.silverwing.ai.domain.model.ConversationRecord;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationMessagePO;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 会话历史基础设施转换器
 * <p>
 * 负责领域模型与持久化对象（父子表 PO）之间的双向转换。
 * </p>
 *
 * @author silverwing
 */
@Mapper(componentModel = "spring")
public interface ConversationHistoryInfraConvertor {

    /**
     * 会话父表 PO -> 领域模型
     */
    @Mapping(target = "messages", ignore = true)
    ConversationRecord toConversationRecord(AiConversationPO po);

    /**
     * 消息子表 PO -> 领域模型
     */
    ConversationMessage toConversationMessage(AiConversationMessagePO po);

    /**
     * 消息子表 PO 列表 -> 领域模型列表
     */
    List<ConversationMessage> toConversationMessageList(List<AiConversationMessagePO> poList);

    /**
     * NlpParseResult -> 实体JSON字符串
     */
    @Named("parseResultToJson")
    default String parseResultToJson(NlpParseResult parseResult) {
        if (parseResult == null) {
            return null;
        }
        return JSONUtil.toJsonStr(parseResult);
    }
}
