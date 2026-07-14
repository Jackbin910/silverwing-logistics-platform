package com.silverwing.ai.config;


import org.dromara.dynamictp.core.DtpRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAsyncConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        AsyncTaskExecutor asyncExecutor = new TaskExecutorAdapter(DtpRegistry.getExecutor("chatStreamExecutor"));
        configurer.setTaskExecutor(asyncExecutor);
        configurer.setDefaultTimeout(300_000L);
    }
}
