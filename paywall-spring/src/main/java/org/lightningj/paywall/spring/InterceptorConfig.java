/*
 * ***********************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public License   *
 *  (LGPL-3.0-or-later)                                                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration to add PaymentRequired annotation.
 *
 * @author philip 2019-02-22
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


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
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getPaywallInterceptor()).addPathPatterns("/**");
    }
}
