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
package org.lightningj.paywall.spring.local

import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.spring.TestPaymentHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

/**
 * Unit tests for DefaultLocalBeanCondition
 * @author philip 2019-02-10
 */
@ContextConfiguration(classes=[TestPaymentHandler, PaywallProperties,LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class DefaultLocalBeanConditionSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    private Environment env

    DefaultLocalBeanCondition condition = new DefaultLocalBeanCondition()
    ConditionContext context

    def setup(){
        context = Mock(ConditionContext)
        context.getEnvironment() >> { env }
    }

    def "Verify that match returns true if property is not set to true"(){
        expect:
        condition.matches(context, null)
    }

    def "Verify that match returns false if property is set to true"(){
        setup:
        ConditionContext context = Mock(ConditionContext)
        Environment mockedEnv = Mock(Environment)
        mockedEnv.getProperty("paywall.custombeans.enable") >> "true"
        context.getEnvironment() >> mockedEnv
        expect:
        !condition.matches(context, null)
    }
}
