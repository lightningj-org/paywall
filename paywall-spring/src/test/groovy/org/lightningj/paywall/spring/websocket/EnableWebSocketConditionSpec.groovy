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
package org.lightningj.paywall.spring.websocket

import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.spring.TestPaymentHandler
import org.lightningj.paywall.spring.local.LocalProfileBeanConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Unit tests for EnableWebSocketCondition
 * @author philip 2019-05-15
 */
@ContextConfiguration(classes=[TestPaymentHandler, PaywallProperties, LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class EnableWebSocketConditionSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    private Environment env

    EnableWebSocketCondition condition = new EnableWebSocketCondition()
    ConditionContext context

    def setup(){
        context = Mock(ConditionContext)
        context.getEnvironment() >> { env }

        condition.log = Mock(Logger)
    }

    def "Verify that match returns true if property is not set to true"(){
        expect:
        condition.matches(context, null)
    }

    def "Verify that match returns true if property is set to true"(){
        setup:
        ConditionContext context = Mock(ConditionContext)
        Environment mockedEnv = Mock(Environment)
        mockedEnv.getProperty("paywall.websocket.enable") >> "true"
        context.getEnvironment() >> mockedEnv
        when:
        def result = condition.matches(context, null)
        then:
        result
        0 * condition.log.severe(_)
    }

    def "Verify that match returns false if property is set to false"(){
        setup:
        ConditionContext context = Mock(ConditionContext)
        Environment mockedEnv = Mock(Environment)
        mockedEnv.getProperty("paywall.websocket.enable") >> "false"
        context.getEnvironment() >> mockedEnv
        when:
        def result = condition.matches(context, null)
        then:
        !result
        0 * condition.log.severe(_)
    }

    def "Verify that match returns false if property is set to invalid value and severe logging is performed"(){
        setup:
        ConditionContext context = Mock(ConditionContext)
        Environment mockedEnv = Mock(Environment)
        mockedEnv.getProperty("paywall.websocket.enable") >> "invalidboolean"
        context.getEnvironment() >> mockedEnv
        when:
        def result = condition.matches(context, null)
        then:
        !result
        1 * condition.log.severe("Invalid paywall configuration, setting: 'paywall.websocket.enable' should be either 'true' or 'false', not 'invalidboolean'. Assuming false.")
    }
}
