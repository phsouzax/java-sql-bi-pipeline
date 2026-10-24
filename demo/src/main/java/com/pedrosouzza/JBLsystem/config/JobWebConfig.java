package com.pedrosouzza.JBLsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class JobWebConfig implements WebMvcConfigurer {
    private final JobTokenInterceptor jobTokenInterceptor;

    public JobWebConfig(JobTokenInterceptor jobTokenInterceptor) {
        this.jobTokenInterceptor = jobTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jobTokenInterceptor).addPathPatterns("/api/v1/jobs/fipe-precos/**");
    }
}
