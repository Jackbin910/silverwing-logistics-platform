package com.silverwing.biz.post.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.post.domain.model.aggregate.SysPostAggregate;
import com.silverwing.biz.post.infrastructure.dao.po.SysPostPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 岗位基础设施转换器（防腐层）
 * <p>负责 PO（SysPostPO）与领域实体（SysPostAggregate）之间的互转，使用 MapStruct 编译期生成。
 * 放置于 infrastructure 层，避免 domain 层反向依赖 dbsdk 层产生循环依赖。</p>
 *
 * @author silverwing
 */
@Mapper
public interface PostInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    PostInfraConvertor INSTANCE = Mappers.getMapper(PostInfraConvertor.class);

    /**
     * 领域实体 -> 持久化对象
     */
    SysPostPO toPo(SysPostAggregate post);

    /**
     * 持久化对象 -> 领域实体
     */
    SysPostAggregate toDomain(SysPostPO po);
}
