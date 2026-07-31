package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.client.IamPostClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位命令服务（CQRS 写侧）
 * <p>仅做用例编排，通过 {@link IamPostClient} 防腐层端口访问 post 上下文。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final IamPostClient iamPostClient;

    public PostResponse create(SavePostCommand command) {
        return iamPostClient.create(command);
    }

    public void update(Long id, SavePostCommand command) {
        iamPostClient.update(id, command);
    }

    public void delete(List<Long> ids) {
        iamPostClient.delete(ids);
    }
}
