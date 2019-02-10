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
package org.lightningj.paywall.spring.local;

import org.lightningj.paywall.spring.PaywallInterceptor;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import java.util.logging.Logger;

/**
 * Condition to check if Default beans in LocalProfileBeanConfiguration should be used.
 *
 * @author philip 2019-02-8
 */
public class DefaultLocalBeanCondition implements Condition {

    static Logger log = Logger.getLogger(PaywallInterceptor.class.getName());

    /**
     * Determine if the condition matches.
     *
     * @param context  the condition context
     * @param metadata metadata of the {@link AnnotationMetadata class}
     *                 or {@link MethodMetadata method} being checked
     * @return {@code true} if the condition matches and the component can be registered,
     * or {@code false} to veto the annotated component's registration
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enableCustomBeans =  context.getEnvironment().getProperty("paywall.custombeans.enable");
        log.fine("Checking if default local bean should be registered: " + enableCustomBeans);
        return enableCustomBeans == null || !enableCustomBeans.trim().toLowerCase().equals("true");
    }
}
