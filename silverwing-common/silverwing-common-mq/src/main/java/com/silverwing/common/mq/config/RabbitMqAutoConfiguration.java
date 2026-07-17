package com.silverwing.common.mq.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ 自动配置：默认注册 JSON 消息转换器，
 * 保证生产端与消费端对象序列化方式一致，避免手动指定序列化器。
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitMqAutoConfiguration {

    /**
     * 为 RabbitTemplate 提供 JSON 消息转换器（基于 Jackson）。
     *
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter rabbitJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
