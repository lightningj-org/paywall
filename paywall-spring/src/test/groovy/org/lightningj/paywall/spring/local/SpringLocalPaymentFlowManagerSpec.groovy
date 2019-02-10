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

import org.lightningj.paywall.paymentflow.PaymentFlowMode
import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.spring.TestPaymentHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Unit tests for SpringLocalPaymentFlowManager
 * @author philip 2019-02-10
 */
@ContextConfiguration(classes=[TestPaymentHandler, PaywallProperties,LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class SpringLocalPaymentFlowManagerSpec extends Specification {

    @Autowired
    SpringLocalPaymentFlowManager paymentFlowManager

    def setup(){
        SpringLocalPaymentFlowManager.log = Mock(Logger)
    }

    def "Verify that getPaymentFlowMode return LOCAL"(){
        expect:
        paymentFlowManager.getPaymentFlowMode(null) == PaymentFlowMode.LOCAL
    }

    def "Verify that dependencies are auto injected properly"(){
        expect:
        paymentFlowManager.getTokenGenerator() != null
        paymentFlowManager.getLightningHandler() != null
        paymentFlowManager.getCurrencyConverter() != null
        paymentFlowManager.getOrderRequestGeneratorFactory() != null
        paymentFlowManager.getRequestPolicyFactory() != null
        paymentFlowManager.getPaymentHandler() != null
        paymentFlowManager.paywallProperties != null
    }

    def "Verify that getTokenNotBeforeDuration returns Duration from setting paywall.invoice.defaultvalidity "(){
        expect:
        paymentFlowManager.getTokenNotBeforeDuration().toMinutes() == 3
    }

    def "Verify that getTokenNotBeforeDuration returns Duration from setting paywall.settlement.defaultvalidity"(){
        expect:
        paymentFlowManager.getRegisterNewInvoices()
    }

    def "Verify that getTokenNotBeforeDuration returns default validity if setting is not set"(){
        setup:
        PaywallProperties p = new PaywallProperties()
        p.jwtTokenNotBefore = null
        paymentFlowManager.paywallProperties = p
        expect:
        paymentFlowManager.getTokenNotBeforeDuration() == null
    }

    def "Verify that getRegisterNewInvoices returns default validity if setting is not set"(){
        setup:
        PaywallProperties p = new PaywallProperties()
        p.invoiceRegisterNew = null
        paymentFlowManager.paywallProperties = p
        expect:
        paymentFlowManager.getRegisterNewInvoices() == PaywallProperties.DEFAULT_INVOICE_REGISTER_NEW
    }

    def "Verify that error log is done for invalid setting of jwt token not before."(){
        setup:
        PaywallProperties p = new PaywallProperties()
        p.jwtTokenNotBefore = "abc"
        paymentFlowManager.paywallProperties = p
        when:
        def r = paymentFlowManager.getTokenNotBeforeDuration()
        then:
        r == null
        1 * SpringLocalPaymentFlowManager.log.severe("Error parsing application properties, setting paywall.jwt.notbefore should be an integer value if set, not abc, disabling not before duration in JWT tokens.")
    }

    def "Verify that error log is done for invalid setting of invoice register new"(){
        setup:
        PaywallProperties p = new PaywallProperties()
        p.invoiceRegisterNew = "abc"
        paymentFlowManager.paywallProperties = p
        when:
        def r = paymentFlowManager.getRegisterNewInvoices()
        then:
        r == PaywallProperties.DEFAULT_INVOICE_REGISTER_NEW
        1 * SpringLocalPaymentFlowManager.log.severe("Error parsing application properties, setting paywall.invoice.registernew should be true or false, not abc, using default value: false")
    }

}
