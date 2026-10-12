package com.aeon.common.config;

import com.aeon.common.exception.GlobalExceptionHandler;
import com.aeon.common.logging.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(GlobalExceptionHandler.class)
public class CommonWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> reg = new FilterRegistrationBean<>(new CorrelationIdFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
