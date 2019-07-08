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
package org.lightningj.paywall.spring.controller

import org.lightningj.paywall.paymentflow.ExpectedTokenType
import org.lightningj.paywall.paymentflow.PaymentFlow
import org.lightningj.paywall.paymentflow.PaymentFlowManager
import org.lightningj.paywall.paymentflow.SettlementResult
import org.lightningj.paywall.spring.PaywallExceptionHandler
import org.lightningj.paywall.spring.response.SettlementResponse
import org.lightningj.paywall.vo.Settlement
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

import java.time.Instant

import static org.lightningj.paywall.web.HTTPConstants.HEADER_PAYWALL_MESSAGE
import static org.lightningj.paywall.web.HTTPConstants.HEADER_PAYWALL_MESSAGE_VALUE

/**
 * Unit test for CheckSettlementController
 */
class CheckSettlementControllerSpec extends Specification {

    CheckSettlementController controller = new CheckSettlementController()
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/someuri.json")
    MockHttpServletResponse response = new MockHttpServletResponse()

    def setup() {
        controller.paymentFlowManager = Mock(PaymentFlowManager)
        controller.paywallExceptionHandler = Mock(PaywallExceptionHandler)
    }

    def "Verify that checkSettlement() returns unsettled response if payment isn't settled"() {
        setup:
        def paymentFlow = Mock(PaymentFlow)
        when:
        SettlementResponse resp = controller.checkSettlement(request, response)

        then:
        !resp.settled
        response.contentType == "application/json"
        response.getHeader(HEADER_PAYWALL_MESSAGE) == HEADER_PAYWALL_MESSAGE_VALUE
        1 * controller.paymentFlowManager.getPaymentFlowFromToken(!null, ExpectedTokenType.INVOICE_TOKEN) >> {
            paymentFlow
        }
        1 * paymentFlow.isSettled() >> { false }
    }

    def "Verify that checkSettlement() returns settled response if payment isn settled"() {
        setup:
        def paymentFlow = Mock(PaymentFlow)
        when:
        SettlementResponse resp = controller.checkSettlement(request, response)

        then:
        resp.settled
        resp.token == "SomeToken"
        response.contentType == "application/json"
        response.getHeader(HEADER_PAYWALL_MESSAGE) == HEADER_PAYWALL_MESSAGE_VALUE
        1 * controller.paymentFlowManager.getPaymentFlowFromToken(!null, ExpectedTokenType.INVOICE_TOKEN) >> {
            paymentFlow
        }
        1 * paymentFlow.isSettled() >> { true }
        1 * paymentFlow.getSettlement() >> {
            Settlement settlement = new Settlement("abc".getBytes(), null, Instant.ofEpochMilli(10000), null, true)
            return new SettlementResult(settlement, "SomeToken")
        }
    }

    def "Verify that handleException calls underlying paywallExceptionHandler bean."() {
        setup:
        Exception e = new IOException("asdf")
        when:
        controller.handleException(request, response, e)
        response.getHeader(HEADER_PAYWALL_MESSAGE) == HEADER_PAYWALL_MESSAGE_VALUE
        then:
        1 * controller.paywallExceptionHandler.handleException(request, response, e)
    }
}
