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
package org.lightningj.paywall.springboot2.functional

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import geb.spock.GebSpec
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.spring.APIError
import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.spring.response.SettlementResponse
import org.lightningj.paywall.spring.websocket.WebSocketSettledPaymentHandler
import org.lightningj.paywall.springboot2.functional.pages.TestPaywallJSPage
import org.lightningj.paywall.springboot2.paymenthandler.*
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.Base58
import org.lightningj.paywall.web.HTTPConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.TestPropertySource
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.lang.reflect.Type
import java.security.SecureRandom
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

import static org.lightningj.paywall.spring.websocket.PaywallWebSocketConfig.CHECK_SETTLEMENT_QUEUE_PREFIX

/**
 * Functional test of Paywall Javascript library using Geb to run inside browser driver
 * and verify that the javascript library is still functioning.
 *
 * @author philip 2019-07-09
 *
 */
@Stepwise
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/test_application.properties")
class PaywallJavaScriptFunctionalSpec extends GebSpec {

    @LocalServerPort
    int randomServerPort

    @Autowired
    ArticleDataRepository articleDataRepository

    @Autowired
    DemoMinimalPaymentDataRepository demoPaymentDataRepository

    @Autowired
    DemoPaymentHandler demoPaymentHandler


    static final String UNSENT = "0"
    static final String OPENED = "1"
    static final String HEADERS_RECEIVED = "2"
    static final String LOADING = "3"
    static final String DONE = "4"



    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup() {

        if (articleDataRepository.findByArticleId("abc123") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abc123"
            articleData.price = 10
            articleDataRepository.save(articleData)
        }
        if (articleDataRepository.findByArticleId("abc456") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abc456"
            articleData.price = 20
            articleDataRepository.save(articleData)
        }
        if (articleDataRepository.findByArticleId("abcPayPerRequest") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abcPayPerRequest"
            articleData.price = 15
            articleDataRepository.save(articleData)
        }

        demoPaymentHandler.settlementDuration = Duration.ofMinutes(5)
        browser.setBaseUrl("http://localhost:${randomServerPort}")
    }


    def "Create a on method paywalled request that isn't per request and verify that correct events are triggered."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Request payment"
        onMethodNoPayPerRequestButton.click()

        report("test1")
        then: "Verify that invoice panel is shown and populated"
        invoicePanel.displayed
        invoiceDescription == "Some description"
        invoiceAmount == "0.1"
        invoiceBolt11.startsWith "ln"
        invoiceNodeInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        showInvoiceTimeRemaining.displayed
        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_INVOICE") == 1
        findEventText("PAYWALL_INVOICE")[0] =~ '"type":"invoice"'
        findEventCount("MAIN_ONSTATECHANGE") == 1
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        when: "Simulate invoice payment"
        payinvoiceButton.click()
        then: "Verify that invoice panel is hidden"
        !invoicePanel.displayed
        and: "Verify that SETTLED and on ready state change/load events have been triggered"
        findEventCount("PAYWALL_SETTLED") == 1
        findEventText("PAYWALL_SETTLED")[0] =~ '"settled":true'
        findEventCount("MAIN_ONSTATECHANGE") == 5
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[2] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[3] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[4] == DONE

        findEventCount("MAIN_ONLOADSTART") == 1
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ "PocService1, Poc1!"

        and: "Verify time remaining of settlement panel is shown"
        showSettlementTimeRemainingPanel.displayed
        when: "Try to regenerate the call once more and verify that it is successful"
        reUsePaywallReq1Button.click()

        then: "Verify that ready state change/load event are triggered, but no new paywall events"
        findEventCount("PAYWALL_SETTLED") == 1
        findEventCount("PAYWALL_INVOICE") == 1
        findEventCount("MAIN_ONSTATECHANGE") == 9
        findEventText("MAIN_ONSTATECHANGE")[5] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[6] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[7] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[8] == DONE

        findEventCount("MAIN_ONLOADSTART") == 2
        findEventCount("MAIN_PROGRESS") == 2
        findEventCount("MAIN_ONLOADEND") == 2
        findEventCount("MAIN_ONLOAD") == 2
        findEventText("MAIN_ONLOAD")[1] =~ "PocService1, Poc1!"

        cleanup:
        clearAll()
        report("test")
    }

    def "Create a on method paywalled request that is per request and verify that correct events are triggered and state is EXECUTED in the end. Also verifying that posting data trigger upload events."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Request payment"
        onMethodPayPerRequestButton.click()

        then: "Verify that invoice panel is shown and populated"
        invoicePanel.displayed
        invoiceDescription == "Some description"
        invoiceAmount == "0.15"
        invoiceBolt11.startsWith "ln"
        invoiceNodeInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        showInvoiceTimeRemaining.displayed
        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_INVOICE") == 1
        findEventText("PAYWALL_INVOICE")[0] =~ '"type":"invoice"'
        findEventCount("MAIN_ONSTATECHANGE") == 1
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        when: "Simulate invoice payment"
        payinvoiceButton.click()
        then: "Verify that invoice panel is hidden"
        !invoicePanel.displayed
        and: "Verify that SETTLED and on ready state change/load events have been triggered"
        findEventCount("PAYWALL_SETTLED") == 1
        findEventText("PAYWALL_SETTLED")[0] =~ '"settled":true'
        findEventCount("MAIN_ONSTATECHANGE") == 5
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[2] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[3] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[4] == DONE

        findEventCount("MAIN_ONLOADSTART") == 1
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ "PocService1, Pay Per Request!"


        findEventCount("MAIN_UPLOAD_ONLOADSTART") == 1
        findEventCount("MAIN_UPLOAD_PROGRESS") == 1
        findEventCount("MAIN_UPLOAD_ONLOADEND") == 1
        findEventCount("MAIN_UPLOAD_ONLOAD") == 1
        findEventText("MAIN_UPLOAD_ONLOAD")[0] =~ '"isTrusted":true'

        findEventCount("PAYWALL_EXECUTED") == 1
        findEventText("PAYWALL_EXECUTED")[0] =~ '"settled":true'

        cleanup:
        clearAll()
        report("test")
    }

    def "Create a on class paywalled request that is and verify that correct events are triggered and that all services are paywalled"(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Request payment"
        onClassRequest1Button.click()

        then: "Verify that invoice panel is shown and populated"
        invoicePanel.displayed
        invoiceDescription == "Some description"
        invoiceAmount == "0.2"
        invoiceBolt11.startsWith "ln"
        invoiceNodeInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        showInvoiceTimeRemaining.displayed
        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_INVOICE") == 1
        findEventText("PAYWALL_INVOICE")[0] =~ '"type":"invoice"'
        findEventCount("MAIN_ONSTATECHANGE") == 1
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        when: "Simulate invoice payment"
        payinvoiceButton.click()
        then: "Verify that invoice panel is hidden"
        !invoicePanel.displayed
        and: "Verify that SETTLED and on ready state change/load events have been triggered"
        findEventCount("PAYWALL_SETTLED") == 1
        findEventText("PAYWALL_SETTLED")[0] =~ '"settled":true'
        findEventCount("MAIN_ONSTATECHANGE") == 5
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[2] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[3] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[4] == DONE

        findEventCount("MAIN_ONLOADSTART") == 1
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ "PocService2, poc2_1!"


        and: "Verify time remaining of settlement panel is shown"
        showSettlementTimeRemainingPanel.displayed

        when: "Request payment from another service protected by same paywall class annotation"
        onClassRequest2Button.click()

        then: "Verify that invoice panel is shown"
        invoicePanel.displayed
        invoiceDescription == "Some description"
        invoiceAmount == "0.2"
        invoiceBolt11.startsWith "ln"
        invoiceNodeInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"

        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_INVOICE") == 2
        findEventText("PAYWALL_INVOICE")[1] =~ '"type":"invoice"'
        findEventCount("MAIN_ONSTATECHANGE") == 6
        findEventText("MAIN_ONSTATECHANGE")[5] == OPENED
        when: "Simulate invoice payment"
        payinvoiceButton.click()
        then: "Verify that invoice panel is hidden"
        !invoicePanel.displayed
        and: "Verify that SETTLED and on ready state change/load events have been triggered"
        findEventCount("PAYWALL_SETTLED") == 2
        findEventText("PAYWALL_SETTLED")[1] =~ '"settled":true'
        findEventCount("MAIN_ONSTATECHANGE") == 10
        findEventText("MAIN_ONSTATECHANGE")[6] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[7] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[8] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[9] == DONE

        findEventCount("MAIN_ONLOADSTART") == 2
        findEventCount("MAIN_PROGRESS") == 2
        findEventCount("MAIN_ONLOADEND") == 2
        findEventCount("MAIN_ONLOAD") == 2
        findEventText("MAIN_ONLOAD")[1] =~ "PocService2, poc2_2!"


        and: "Verify time remaining of settlement panel is shown"
        showSettlementTimeRemainingPanel.displayed

        cleanup:
        clearAll()
    }

    def "Verify that it is possible to perform regular, non-paywalled requests using PaywallHttpRequest."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Create a request to a non-paywalled service"
        makeNonPaymentRequestButton.click()

        then: "Verify that invoice panel is shown and populated"
        !invoicePanel.displayed
        then: "Verify that  on ready state change/load events have been triggered"
        findEventCount("PAYWALL_INVOICE") == 0
        findEventCount("PAYWALL_SETTLED") == 0
        findEventCount("MAIN_ONSTATECHANGE") == 4
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[2] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[3] == DONE

        findEventCount("MAIN_ONLOADSTART") == 0 // Special case that onloadstart isn't called.
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ "PocService1, No Payment Required!"

        cleanup:
        clearAllButton.click()
    }

    def "Verify PaywallHttpRequest causing timeout in underlying XMLHttpRequest triggers correct events."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Create a request with short timeout"
        makeTimeoutRequestButton.click()

        report("timeout")
        then: "Verify that invoice panel is not shown"
        !invoicePanel.displayed
        then: "Verify that  on ready state change/timeout events have been triggered"
        findEventCount("PAYWALL_INVOICE") == 0
        findEventCount("PAYWALL_SETTLED") == 0
        findEventCount("MAIN_ONSTATECHANGE") == 2
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == DONE

        findEventCount("MAIN_TIMEOUT") == 1

        cleanup:
        clearAllButton.click()
    }

    def "Verify PaywallHttpRequest calling abort trigger expected events."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Create a request with that will be aborted"
        makeAbortRequestButton.click()

        then: "Verify that invoice panel is not shown"
        !invoicePanel.displayed
        then: "Verify that  on ready state change/abort events have been triggered"
        findEventCount("PAYWALL_INVOICE") == 0
        findEventCount("PAYWALL_SETTLED") == 0
        findEventCount("MAIN_ONSTATECHANGE") == 2
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == DONE

        findEventCount("MAIN_ABORT") == 1

        cleanup:
        clearAllButton.click()
    }

    def "Verify PaywallHttpRequest when network error occurred trigger expected error events."(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Create a request with that will be aborted"
        makeErrorRequestButton.click()
        Thread.sleep(1000)

        then: "Verify that invoice panel is not shown"
        !invoicePanel.displayed
        then: "Verify that  on ready state change/abort events have been triggered"
        findEventCount("PAYWALL_INVOICE") == 0
        findEventCount("PAYWALL_SETTLED") == 0
        findEventCount("MAIN_ONSTATECHANGE") == 2
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == DONE

        findEventCount("MAIN_ONERROR") == 1

        cleanup:
        clearAllButton.click()
    }

    def """Verify that API error (i.e. JSON error message from underlying API) inside a paywalled call triggers the
    same events as a regular call. And then if paywalled related error occurred is PAYWALL_ERROR event triggered."""(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Request payment"
        makeAPIErrorRequestButton.click()

        then: "Verify that invoice panel is shown and populated"
        invoicePanel.displayed
        invoiceDescription == "Some description"

        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_INVOICE") == 1
        findEventText("PAYWALL_INVOICE")[0] =~ '"type":"invoice"'
        findEventCount("MAIN_ONSTATECHANGE") == 1
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        when: "Simulate invoice payment"
        payinvoiceButton.click()
        then: "Verify that invoice panel is hidden"
        !invoicePanel.displayed
        and: "Verify that SETTLED and on ready state change/load events have been triggered"
        findEventCount("PAYWALL_SETTLED") == 1
        findEventText("PAYWALL_SETTLED")[0] =~ '"settled":true'
        findEventCount("MAIN_ONSTATECHANGE") == 5
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[2] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[3] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[4] == DONE

        findEventCount("MAIN_ONLOADSTART") == 1
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ '"status":500,"error":"Internal Server Error"'


        when: "call reuse button that will trigger an paywall settlement token error in this case"
        reUsePaywallReq1Button.click()

        then: "Verify that invoice panel is not shown"
        !invoicePanel.displayed

        and: "Verify that Invoice and on ready state change event have been triggered"
        findEventCount("PAYWALL_PAYWALL_ERROR") == 1
        findEventText("PAYWALL_PAYWALL_ERROR")[0] =~ 'Invalid Request: Error request data doesn\'t match data in settlement token.'

        cleanup:
        clearAll()
    }

    def """Verify that API error (i.e. JSON error message from underlying API) inside a non-paywalled call triggers the
    same events as a regular call."""(){
        when: "Go to Test Paywall JS Page"
        to TestPaywallJSPage
        then:
        at TestPaywallJSPage
        when: "Request payment"
        makeAPIErrorNoPaywallRequestButton.click()

        then: "Verify that invoice panel is shown and populated"
        !invoicePanel.displayed

        and: "Verify that on ready state change onload events have been triggered"
        findEventCount("PAYWALL_INVOICE") == 0
        findEventCount("PAYWALL_SETTLED") == 0
        findEventCount("MAIN_ONSTATECHANGE") == 4
        findEventText("MAIN_ONSTATECHANGE")[0] == OPENED
        findEventText("MAIN_ONSTATECHANGE")[1] == HEADERS_RECEIVED
        findEventText("MAIN_ONSTATECHANGE")[2] == LOADING
        findEventText("MAIN_ONSTATECHANGE")[3] == DONE

        findEventCount("MAIN_ONLOADSTART") == 0 // Special case that onload start is never called for non-paywalled requests
        findEventCount("MAIN_PROGRESS") == 1
        findEventCount("MAIN_ONLOADEND") == 1
        findEventCount("MAIN_ONLOAD") == 1
        findEventText("MAIN_ONLOAD")[0] =~ '"status":500,"error":"Internal Server Error"'


        cleanup:
        clearAll()
    }
}
