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
package org.lightningj.paywall.spring

import org.lightningj.paywall.spring.local.LocalProfileBeanConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Unit tests for SpringPaymentHandler
 * @author philip 2019-02-10
 */
@ContextConfiguration(classes = [TestPaymentHandler, PaywallProperties, LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class SpringPaymentHandlerSpec extends Specification {

    @Autowired
    SpringPaymentHandler paymentHandler

    def setup() {
        SpringPaymentHandler.log = Mock(Logger)
    }

    def "Verify that all beans are injected"() {
        expect:
        paymentHandler.paywallProperties != null
        paymentHandler.getLightningHandler() != null
    }

    def "Verify that getDefaultInvoiceValidity returns Duration from setting paywall.invoice.defaultvalidity "() {
        expect:
        paymentHandler.getDefaultInvoiceValidity().toMinutes() == 1
    }

    def "Verify that getDefaultSettlementValidity returns Duration from setting paywall.settlement.defaultvalidity"() {
        expect:
        paymentHandler.getDefaultSettlementValidity().toMinutes() == 2
    }

    def "Verify that getDefaultInvoiceValidity returns default validity if setting is not set"() {
        setup:
        PaywallProperties p = new PaywallProperties()
        p.invoiceDefaultValidity = null
        paymentHandler.paywallProperties = p
        expect:
        paymentHandler.getDefaultInvoiceValidity().toHours() == 1
    }

    def "Verify that getDefaultSettlementValidity returns default validity if setting is not set"() {
        setup:
        PaywallProperties p = new PaywallProperties()
        p.settlmentDefaultValidity = null
        paymentHandler.paywallProperties = p
        expect:
        paymentHandler.getDefaultSettlementValidity().toHours() == 24
    }

    def "Verify that error log is done for invalid setting of default invoice validity"() {
        setup:
        PaywallProperties p = new PaywallProperties()
        p.invoiceDefaultValidity = "abc"
        paymentHandler.paywallProperties = p
        when:
        def r = paymentHandler.getDefaultInvoiceValidity()
        then:
        r.toHours() == 1
        1 * SpringPaymentHandler.log.severe("Error parsing application properties, setting paywall.invoice.defaultvalidity should be an integer, not abc, using default value: 3600")
    }

    def "Verify that error log is done for invalid setting of default settlement validity"() {
        setup:
        PaywallProperties p = new PaywallProperties()
        p.settlmentDefaultValidity = "abc"
        paymentHandler.paywallProperties = p
        when:
        def r = paymentHandler.getDefaultSettlementValidity()
        then:
        r.toHours() == 24
        1 * SpringPaymentHandler.log.severe("Error parsing application properties, setting paywall.settlement.defaultvalidity should be an integer, not abc, using default value: 86400")
    }
}
