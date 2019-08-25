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
package org.lightningj.paywall.springboot2

import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.springboot2.paymenthandler.*
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.Base58
import org.lightningj.paywall.web.HTTPConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.Duration
import java.time.temporal.ChronoUnit

import static org.lightningj.paywall.web.HTTPConstants.HEADER_PAYWALL_MESSAGE
import static org.lightningj.paywall.web.HTTPConstants.HEADER_PAYWALL_MESSAGE_VALUE

/**
 * Functional test of PaywallInterceptor and LocalPaymentFlow. It runs
 * defined test scienarios against running web service with a mocked LightningHandler.
 *
 * @author philip 2019-04-12
 *
 */
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/test_application.properties")
class LocalPaymentFlowIntegrationSpec extends Specification {

    @LocalServerPort
    int randomServerPort

    @Autowired
    ArticleDataRepository articleDataRepository

    @Autowired
    DemoMinimalPaymentDataRepository demoPaymentDataRepository

    @Autowired
    DemoPaymentHandler demoPaymentHandler

    @Autowired
    LightningHandler lightningHandler

    @Autowired
    TokenGenerator tokenGenerator

    @Autowired
    PaywallProperties paywallProperties


    @Shared RESTClient restClient

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup() {
        restClient = new RESTClient( "http://localhost:${randomServerPort}" )

        restClient.handler.failure = {
            resp, data ->
                resp.data = data
                return resp
        }

        if (articleDataRepository.findByArticleId("abc123") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abc123"
            articleData.price = 10
            articleDataRepository.save(articleData)
        }
        if (articleDataRepository.findByArticleId("abcPayPerRequest") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abcPayPerRequest"
            articleData.price = 15
            articleDataRepository.save(articleData)
        }

    }

    def "Verify successful local payment flow with Json and pay per request set to false."(){
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/poc1' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        resp.headers[HEADER_PAYWALL_MESSAGE].value == HEADER_PAYWALL_MESSAGE_VALUE
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        when:

        // Check if payment have been settled.
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        resp.headers[HEADER_PAYWALL_MESSAGE].value == HEADER_PAYWALL_MESSAGE_VALUE
        verifyCheckSettlementJsonResponse(resp.data, invoice)

        when:
        // check again and check that same result is returned.
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"

        verifyCheckSettlementJsonResponse(resp.data, invoice)

        when: // Verify that QR Code is successfully generated
        resp = get(uri: invoice.qrLink)

        then:
        resp.status == 200
        resp.contentType == "image/png"
        resp.data != null

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        then: // Verify that payment data have been updated
        verifyPaymentData(invoice,
                demoPaymentHandler.findPaymentData(Base58.decode(invoice.preImageHash)),
                [expectSettled: true])

        when:
        // Check settlement
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        resp.headers[HEADER_PAYWALL_MESSAGE].value == HEADER_PAYWALL_MESSAGE_VALUE
        def settlement = resp.data
        verifyCheckSettlementJsonResponse(settlement, invoice, [expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/poc1', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "PocService1, Poc1!"
        resp.headers[HEADER_PAYWALL_MESSAGE] == null
        when:
        // Verify that it is possible to use the token multiple times until if not pay per request.
        resp = get(uri: '/poc1', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "PocService1, Poc1!"

    }

    def "Verify successful local payment flow with xml"(){
        when: // First call end-point to receive invoice, and check xml response

        def resp = get(uri: '/poc1' , headers: ["Accept":"application/xml"])
        then:
        resp.status == 402
        resp.contentType == "application/xml"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyXmlInvoiceResponse(invoice)
        when:

        // Check if payment have been settled.
        resp = get(uri: invoice.checkSettlementLink.toString(), headers: ["Accept":"application/xml"])

        then:
        resp.status == 200
        resp.contentType == "application/xml"
        verifyCheckSettlementXMLResponse(resp.data, invoice)

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash.toString()))

        and:
        // Check settlement
        resp = get(uri: invoice.checkSettlementLink.toString(), headers: ["Accept":"application/xml"] )

        then:
        resp.status == 200
        resp.contentType == "application/xml"
        def settlement = resp.data
        verifyCheckSettlementXMLResponse(settlement, invoice, [expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/poc1', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token,
                                           "Accept":"application/xml" ])
        then:
        resp.status == 200
        resp.contentType == "application/xml"
        resp.data.id != null
        resp.data.content == "PocService1, Poc1!"
    }

    def "Verify successful local payment flow with Json and pay per request set to true."(){
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.

        def resp = get(uri: '/poc1PayPerRequest' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice, [payPerRequest: true, expectedValue: 15])
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,
                demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)),
                [expectPayPerRequest: true, expectExecuted: false])
        when:

        // Check if payment have been settled.
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        verifyCheckSettlementJsonResponse(resp.data, invoice, [expectPayPerRequest: true])

        when:
        // check again and check that same result is returned.
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        verifyCheckSettlementJsonResponse(resp.data, invoice, [expectPayPerRequest: true])

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        then: // Verify that payment data have been updated
        verifyPaymentData(invoice,
                demoPaymentHandler.findPaymentData(Base58.decode(invoice.preImageHash)),
        [expectSettled: true, expectPayPerRequest: true])

        when:
        // Check settlement
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        def settlement = resp.data
        verifyCheckSettlementJsonResponse(settlement, invoice, [expectSettled: true, expectPayPerRequest: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/poc1PayPerRequest', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "PocService1, Pay Per Request!"

        when:
        // Verify that it is possible to use the token multiple times until if not pay per request.
        resp = get(uri: '/poc1PayPerRequest', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 402
        resp.contentType == "application/json"

    }

    def "Verify that checkSettlementController returns status 401 with reason EXPIRED if invoice has expired."(){
        setup:
        def originalValidity = lightningHandler.invoiceValidity
        // Set expire to -6 minutes to make sure it is expired with accepted clock skew.
        lightningHandler.invoiceValidity = Duration.of(-6, ChronoUnit.MINUTES)
        when:
        def resp = get(uri: '/poc1' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        resp.headers[HEADER_PAYWALL_MESSAGE].value == HEADER_PAYWALL_MESSAGE_VALUE
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        when:

        // Check if invoice have been expired.
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 401
        resp.contentType == "application/json"
        resp.headers[HEADER_PAYWALL_MESSAGE].value == HEADER_PAYWALL_MESSAGE_VALUE
        def error = resp.data
        error.status == "UNAUTHORIZED"
        error.message == "JWT Token Problem: JWT Token have expired."
        error.errors.size() == 1
        error.errors[0] == "JWT Token have expired."
        error.reason == "EXPIRED"
        cleanup:
        lightningHandler.invoiceValidity = originalValidity
    }

    // Change to full data, to verify everything.
    def "Verify that PaymentInterceptor returns status 401 with reason EXPIRED if settlement has expired."(){
        setup:
        def originalValidity = demoPaymentHandler.getDefaultSettlementValidity()
        // Set expire to -6 minutes to make sure it is expired with accepted clock skew.
        demoPaymentHandler.settlementDuration = Duration.of(-6, ChronoUnit.MINUTES)
        when:
        def resp = get(uri: '/poc1' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        then: // Verify that payment data have been updated
        verifyPaymentData(invoice,
                demoPaymentHandler.findPaymentData(Base58.decode(invoice.preImageHash)),
                [expectSettled: true])

        when:
        // Check settlement
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        def settlement = resp.data
        verifyCheckSettlementJsonResponse(settlement, invoice, [expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/poc1', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])

        then:
        resp.status == 401
        resp.contentType == "application/json"
        def error = resp.data
        error.status == "UNAUTHORIZED"
        error.message == "JWT Token Problem: JWT Token have expired."
        error.errors.size() == 1
        error.errors[0] == "JWT Token have expired."
        error.reason == "EXPIRED"
        cleanup:
        demoPaymentHandler.settlementDuration = originalValidity
    }

    def "Verify that PaywallInterceptor returns 400 with message BAD_REQUEST if request body doesn't match"(){
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/poc1' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        and:
        // Check settlement
        resp = get(uri: invoice.checkSettlementLink )

        then:
        resp.status == 200
        resp.contentType == "application/json"
        def settlement = resp.data

        when:
        // Verify that altered request returns.
        resp = get(uri: '/poc1?param=value', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 400
        resp.contentType == "application/json"
        def error = resp.data
        error.status == "BAD_REQUEST"
        error.message == "Invalid Request: Error request data doesn't match data in settlement token."
        error.errors.size() == 1
        error.errors[0] == "Error request data doesn't match data in settlement token."
        error.reason == null
    }


    def "Verify that PaymentInterceptor returns status 500 with error message if internal error occurred."(){
        setup:
        lightningHandler.simulateInternalError("Some Internal Error")
        when:
        def resp = get(uri: '/poc1' )
        then:
        resp.status == 500
        resp.contentType == "application/json"
        def error = resp.data
        error.status == "INTERNAL_SERVER_ERROR"
        error.message == "Internal Server Error"
        error.errors.size() == 1
        error.errors[0] == "Internal Server Error"
        error.reason == null

    }

    private def get(Map m){
        URIBuilder urlBuilder = new URIBuilder(m.uri)
        return restClient.get(path: urlBuilder.path, query: urlBuilder.query, headers: m.headers)
    }

    private void verifyJsonInvoiceResponse(Map jsonData, Map expectedData = [:]){
        assert jsonData.type == "invoice"
        assert jsonData.preImageHash != null
        assert jsonData.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert jsonData.description == "Some description"
        assert jsonData.invoiceAmount.value == (expectedData.expectedValue != null ? expectedData.expectedValue : 10)
        assert jsonData.invoiceAmount.currencyCode == "BTC"
        assert jsonData.invoiceAmount.magnetude == "NONE"
        assert jsonData.nodeInfo.publicKeyInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa"
        assert jsonData.nodeInfo.nodeAddress == "10.10.10.10"
        assert jsonData.nodeInfo.nodePort == 5735
        assert jsonData.nodeInfo.network == "UNKNOWN"
        assert jsonData.nodeInfo.connectString == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        assert jsonData.token != null
        assert jsonData.invoiceDate != null
        assert jsonData.invoiceExpireDate != null
        assert jsonData.payPerRequest == (expectedData.payPerRequest != null ? expectedData.payPerRequest : false)
        assert jsonData.requestPolicyType == "WITH_BODY"
        assert jsonData.checkSettlementLink == "/paywall/api/checkSettlement?pwir=" + URLEncoder.encode(jsonData.token, "UTF-8")
        assert jsonData.qrLink == "/paywall/genqrcode?d=" + URLEncoder.encode(jsonData.bolt11Invoice,"UTF-8")
    }

    private void verifyCheckSettlementJsonResponse(Map jsonData, Map invoice, Map expectedData = [:]){
        assert jsonData.type == "settlement"
        boolean expectSettled =  expectedData.expectSettled != null ? expectedData.expectSettled : false
        assert jsonData.settled == expectSettled
        assert jsonData.status == "OK"
        if(expectSettled){
            assert jsonData.preImageHash == invoice.preImageHash
            assert jsonData.token != null
            assert jsonData.settlementValidUntil != null
            assert jsonData.payPerRequest == (expectedData.expectPayPerRequest != null ? expectedData.expectPayPerRequest : false)
        }else{
            assert jsonData.preImageHash == null
            assert jsonData.token == null
            assert jsonData.settlementValidUntil == null
            assert jsonData.payPerRequest == null
        }
        assert jsonData.settlementValidFrom == null
    }

    private void verifyXmlInvoiceResponse(NodeChild xmlData, Map expectedData = [:]){
        assert xmlData.preImageHash != null
        assert xmlData.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert xmlData.description == "Some description"
        assert xmlData.invoiceAmount.value == 10
        assert xmlData.invoiceAmount.currencyCode == "BTC"
        assert xmlData.invoiceAmount.magnetude == "NONE"
        assert xmlData.nodeInfo.publicKeyInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa"
        assert xmlData.nodeInfo.nodeAddress == "10.10.10.10"
        assert xmlData.nodeInfo.nodePort == 5735
        assert xmlData.nodeInfo.connectString == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        assert xmlData.token != null
        assert xmlData.invoiceDate != null
        assert xmlData.invoiceExpireDate != null
        assert xmlData.payPerRequest == expectedData.payPerRequest != null ? expectedData.payPerRequest.toString() : false
        assert xmlData.requestPolicyType == "WITH_BODY"
        assert xmlData.checkSettlementLink == "/paywall/api/checkSettlement?pwir=" + URLEncoder.encode(xmlData.token.toString(), "UTF-8")
        assert xmlData.qrLink == "/paywall/genqrcode?d=" + URLEncoder.encode(xmlData.bolt11Invoice.toString(),"UTF-8")
    }

    private void verifyCheckSettlementXMLResponse(NodeChild xmlData, NodeChild invoice, Map expectedData = [:]){
        boolean expectSettled = expectedData.expectSettled != null ? expectedData.expectSettled : false
        assert xmlData.settled == expectSettled
        assert xmlData.status == "OK"
        if(expectSettled){
            assert xmlData.preImageHash.toString() == invoice.preImageHash.toString()
            assert xmlData.token != null
            assert xmlData.settlementValidUntil != null
        }
    }

    private void verifyPaymentData(Map jsonData, DemoFullPaymentData data, Map expectedData = [:]){
        assert data.id > 0
        assert data.preImageHash == Base58.decode(jsonData.preImageHash)
        assert data.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert data.description == "Some description"
        assert data.orderAmount.value > 0
        assert data.orderAmount.value == jsonData.invoiceAmount.value
        assert data.invoiceAmount.value == data.orderAmount.value
        assert data.settled == (expectedData.expectSettled != null ? expectedData.expectSettled : false)
        assert data.invoiceDate != null
        assert data.invoiceExpireDate != null
        if(data.settled){
            assert data.settledAmount.value == data.orderAmount.value
            assert data.settlementDate != null
        }
        assert data.payPerRequest == (expectedData.expectPayPerRequest!= null ? expectedData.expectPayPerRequest : false)
        assert data.executed == (expectedData.expectExecuted!= null ? expectedData.expectExecuted : false)
    }

}
