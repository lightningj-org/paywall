package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.spring.PaywallInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {


    // TODO Add config
    @Bean
    PaywallInterceptor getPaywallInterceptor(){
        return new PaywallInterceptor();
    }

    /**
     * Override this method to add Spring MVC interceptors for
     * pre- and post-processing of controller invocation.
     *
     * @param registry
     * @see InterceptorRegistry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getPaywallInterceptor()).addPathPatterns("/**");
    }
}
