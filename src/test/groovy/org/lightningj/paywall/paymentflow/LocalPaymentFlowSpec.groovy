/*
 *************************************************************************
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
package org.lightningj.paywall.paymentflow

import org.jose4j.jwt.JwtClaims
import org.lightningj.paywall.AlreadyExecutedException
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.annotations.PaymentRequired
import org.lightningj.paywall.currencyconverter.CurrencyConverter
import org.lightningj.paywall.currencyconverter.SameCryptoCurrencyConverter
import org.lightningj.paywall.keymgmt.DummyKeyManagerInstance
import org.lightningj.paywall.keymgmt.SymmetricKeyManager
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory
import org.lightningj.paywall.paymenthandler.PaymentHandler
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory
import org.lightningj.paywall.requestpolicy.RequestPolicyType
import org.lightningj.paywall.tokengenerator.SymmetricKeyTokenGenerator
import org.lightningj.paywall.tokengenerator.TokenContext
import org.lightningj.paywall.tokengenerator.TokenException
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.ConvertedOrder
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.NodeInfo
import org.lightningj.paywall.vo.Order
import org.lightningj.paywall.vo.OrderRequest
import org.lightningj.paywall.vo.PreImageData
import org.lightningj.paywall.vo.Settlement
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.web.CachableHttpServletRequest
import org.lightningj.paywall.web.HTTPConstants
import spock.lang.Specification

import javax.servlet.http.Cookie
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for LocalPaymentFlow.
 *
 * Created by Philip Vendil on 2019-01-01.
 */
class LocalPaymentFlowSpec extends Specification {

    TestPaymentFlowManager localFlowManager

    TokenGenerator tokenGenerator
    Duration tokenNotBeforeDuration = Duration.ofMinutes(-15)
    LightningHandler lightningHandler = Mock(LightningHandler)
    PaymentHandler paymentHandler = Mock(PaymentHandler)
    CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
    RequestPolicyFactory requestPolicyFactory = new RequestPolicyFactory()
    CurrencyConverter currencyConverter = new SameCryptoCurrencyConverter()
    OrderRequestGeneratorFactory orderRequestGeneratorFactory = new OrderRequestGeneratorFactory()
    Clock clock = Mock(Clock)
    Instant currentTime = Instant.ofEpochMilli(1546606166000L)

    NodeInfo nodeInfo = new NodeInfo("asdfa123123@123.123.123.123")

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup(){
        clock.instant() >> { currentTime }
        clock.millis() >> { currentTime.toEpochMilli()}
        SymmetricKeyManager keyManager =  DummyKeyManagerInstance.commonInstance
        tokenGenerator = new SymmetricKeyTokenGenerator(keyManager)

        localFlowManager = new TestPaymentFlowManager(PaymentFlowMode.LOCAL,tokenGenerator,
                tokenNotBeforeDuration, requestPolicyFactory,lightningHandler, paymentHandler,
                currencyConverter,orderRequestGeneratorFactory,null)

    }

    def "Verify a successful payment flow for non pay per request flow."(){
        // On pay-walled resource filter.
        when: "First simulate call to filter that have detected a pay-walled resource and not settlement token exists"
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        Instant orderExpireDate = inFuture(Duration.ofMinutes(60))
        Instant invoiceExpireDate = inFuture(Duration.ofMinutes(120))
        then: "Verify that correct local payment flow was returned"
        paymentFlow instanceof LocalPaymentFlow
        verifyLocalPaymentFlow(paymentFlow, "notoken")
        paymentFlow.orderRequest.articleId == paymentRequired.articleId()
        paymentFlow.orderRequest.units == paymentRequired.units()

        and: "Verify that payment is required"
        paymentFlow.isPaymentRequired()

        when: "Verify that correct payment required data is returned"
        InvoiceResult requestPaymentResult = paymentFlow.requestPayment()
        then:
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
            return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                invoiceExpireDate,clock.instant())
        }
        requestPaymentResult.invoice.preImageHash.length > 0
        requestPaymentResult.invoice.bolt11Invoice == "somebolt11invoice"
        JwtClaims claims = tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, requestPaymentResult.token)
        Invoice invoice = new Invoice(claims)
        invoice.preImageHash == requestPaymentResult.invoice.preImageHash

        // On controller
        when: "Simulate controller to check settlement, not settled yet"
        paymentFlow = localFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        verifyLocalPaymentFlow(paymentFlow, "invoicetokenset")
        boolean isSettled = paymentFlow.isSettled()
        then:
        !isSettled
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,requestPaymentResult.token)]
        1 * paymentHandler.checkSettlement(invoice.preImageHash,false) >> null
        paymentFlow.@settlement == null

        when: "Verify that token exception is thrown if invoice token have expired"
        // move clock 180 minutes forward
        currentTime = inFuture(Duration.ofMinutes(180))
        paymentFlow = localFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        setClock(paymentFlow)

        then: "Check that payment flow is invalid"
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,requestPaymentResult.token)]
        0 * paymentHandler.checkSettlement(invoice.preImageHash,false) >> null
        def e = thrown TokenException
        e.reason == TokenException.Reason.EXPIRED
        e.message == "JWT Token have expired."

        when: "Generate new invoice"
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        orderExpireDate = inFuture(Duration.ofMinutes(60))
        invoiceExpireDate = inFuture(Duration.ofMinutes(120))

        then:
        paymentFlow.isPaymentRequired()

        when:
        requestPaymentResult = paymentFlow.requestPayment()
        claims = tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, requestPaymentResult.token)
        invoice = new Invoice(claims)
        then:
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
                return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice2",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                        invoiceExpireDate,clock.instant())
        }

        when: "Verify if token is settled then is a settlement token returned"
        paymentFlow = localFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        verifyLocalPaymentFlow(paymentFlow, "invoicetokenset")
        isSettled = paymentFlow.isSettled()

        then:
        isSettled
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,requestPaymentResult.token)]
        1 * paymentHandler.checkSettlement(invoice.preImageHash,false) >> new Settlement(invoice.preImageHash,null,inFuture(Duration.ofMinutes(180)), null,false)
        paymentFlow.@settlement != null

        when:
        SettlementResult settlementResult = paymentFlow.getSettlement()
        claims = tokenGenerator.parseToken(TokenContext.CONTEXT_SETTLEMENT_TOKEN_TYPE, settlementResult.token)
        Settlement settlement = new Settlement(claims)
        then:
        settlementResult.settlement == paymentFlow.@settlement
        settlement.preImageHash == invoice.preImageHash

        // Back on pay-walled resource filter.
        when: "Check that valid settlement token in the filter header returns payment required as false."
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        verifyLocalPaymentFlow(paymentFlow, "settlementtokenset")
        and:
        boolean isPaymentRequired = paymentFlow.isPaymentRequired()
        then:
        !isPaymentRequired
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

        when: "Check that is is possible to run multiple times until it is expired"
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        currentTime = inFuture(Duration.ofMinutes(10))
        setClock(paymentFlow)
        and:
        isPaymentRequired = paymentFlow.isPaymentRequired()
        boolean isPayPerRequest = paymentFlow.isPayPerRequest()
        then:
        !isPaymentRequired
        !isPayPerRequest
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

        when: "Check that other request data to the filter generates IllegalArgumentException"
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        and:
        isPaymentRequired = paymentFlow.isPaymentRequired()
        then:
        !isPaymentRequired
        e = thrown IllegalArgumentException
        e.message == "Error request data doesn't match data in settlement token."
        1 * request.getMethod() >> "GET"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

        when: "Check that token exception is thrown if settlement token expired."
        currentTime = inFuture(Duration.ofMinutes(200))
        localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        then:
        e = thrown TokenException
        e.reason == TokenException.Reason.EXPIRED
        e.message == "JWT Token have expired."
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

    }

    def "Verify a successful payment flow for a pay per request flow."(){
        // On pay-walled resource filter.
        when: "First simulate call to filter that have detected a pay-walled resource and not settlement token exists"
        PaymentRequired paymentRequired = findAnnotation("payPerRequestMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        Instant orderExpireDate = inFuture(Duration.ofMinutes(60))
        Instant invoiceExpireDate = inFuture(Duration.ofMinutes(120))
        then: "Verify that correct local payment flow was returned"
        paymentFlow instanceof LocalPaymentFlow
        verifyLocalPaymentFlow(paymentFlow, "notoken")
        paymentFlow.orderRequest.articleId == paymentRequired.articleId()
        paymentFlow.orderRequest.units == paymentRequired.units()
        paymentFlow.orderRequest.payPerRequest

        and: "Verify that payment is required"
        paymentFlow.isPaymentRequired()

        when: "Verify that correct payment required data is returned"
        InvoiceResult requestPaymentResult = paymentFlow.requestPayment()
        then:
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
                return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                        invoiceExpireDate,clock.instant())
        }
        requestPaymentResult.invoice.preImageHash.length > 0
        requestPaymentResult.invoice.bolt11Invoice == "somebolt11invoice"
        JwtClaims claims = tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, requestPaymentResult.token)
        Invoice invoice = new Invoice(claims)
        invoice.preImageHash == requestPaymentResult.invoice.preImageHash
        OrderRequest or = new OrderRequest(claims)
        or.payPerRequest

        // On controller
        when: "Generate new invoice"
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        orderExpireDate = inFuture(Duration.ofMinutes(60))
        invoiceExpireDate = inFuture(Duration.ofMinutes(120))

        then:
        paymentFlow.isPaymentRequired()

        when:
        requestPaymentResult = paymentFlow.requestPayment()
        claims = tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, requestPaymentResult.token)
        invoice = new Invoice(claims)
        then:
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
                return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice2",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                        invoiceExpireDate,clock.instant())
        }

        when: "Verify if token is settled then is a settlement token returned"
        paymentFlow = localFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        verifyLocalPaymentFlow(paymentFlow, "invoicetokenset")
        boolean isSettled = paymentFlow.isSettled()

        then:
        isSettled
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,requestPaymentResult.token)]
        1 * paymentHandler.checkSettlement(invoice.preImageHash,false) >> new Settlement(invoice.preImageHash,null,inFuture(Duration.ofMinutes(180)), null,true)
        paymentFlow.@settlement != null

        when:
        SettlementResult settlementResult = paymentFlow.getSettlement()
        claims = tokenGenerator.parseToken(TokenContext.CONTEXT_SETTLEMENT_TOKEN_TYPE, settlementResult.token)
        Settlement settlement = new Settlement(claims)
        then:
        settlementResult.settlement == paymentFlow.@settlement
        settlement.preImageHash == invoice.preImageHash

        // Back on pay-walled resource filter.
        when: "Check that valid settlement token in the filter header returns payment required as false."
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        verifyLocalPaymentFlow(paymentFlow, "settlementtokenset")
        and:
        boolean isPaymentRequired = paymentFlow.isPaymentRequired()
        boolean isPayPerRequest = paymentFlow.isPayPerRequest()
        then:
        !isPaymentRequired
        isPayPerRequest
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token
        1 * paymentHandler.checkSettlement(settlement.preImageHash,false)
        when: "After execution is markAsExecuted called"
        paymentFlow.markAsExecuted()
        then:
        1 * paymentHandler.markAsExecuted(settlement.preImageHash)

        when: "Check that it is not possible to run multiple times."
        paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        and:
        paymentFlow.isPaymentRequired()
        then:
        thrown AlreadyExecutedException
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token
        1 * paymentHandler.checkSettlement(settlement.preImageHash,false) >> { throw new AlreadyExecutedException(settlement.preImageHash,"Somemessage")}

    }

    def "Verify that markAsExecuted throws IllegalArgumentException if settlement is null"(){
        setup:
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        expect:
        paymentFlow.@settlement == null
        when:
        paymentFlow.markAsExecuted()
        then:
        def e = thrown IllegalArgumentException
        e.message == "Internal error marking payment flow as executed, no settlement found."
    }

    def "Verify that getTokenIssuer return null if no JWT token existed in payment flow"(){
        setup:
        Instant orderExpireDate = inFuture(Duration.ofMinutes(300))
        Instant invoiceExpireDate = inFuture(Duration.ofMinutes(300))
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        expect:
        paymentFlow.getTokenIssuer() == null

        when: "Generate invoice for following test"
        InvoiceResult result = paymentFlow.requestPayment()
        then:
        result != null

        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
                return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                        invoiceExpireDate,clock.instant())
        }

        when:
        paymentFlow = localFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        setClock(paymentFlow)
        then:
        paymentFlow.getTokenIssuer() != null
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,result.token)]
    }


    def "Verify getNotBeforeDate return null if notBeforeDuration is null"(){
        setup:
        localFlowManager = new TestPaymentFlowManager(PaymentFlowMode.LOCAL,tokenGenerator,
                null, requestPolicyFactory,lightningHandler, paymentHandler,
                currencyConverter,orderRequestGeneratorFactory,null)
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        expect:
        paymentFlow.getNotBeforeDate() == null
    }

    def "Verify getNotBeforeDate returned is before before current time if notBeforeDuration is negative"(){
        setup:
        localFlowManager = new TestPaymentFlowManager(PaymentFlowMode.LOCAL,tokenGenerator,
                Duration.ofMinutes(-15), requestPolicyFactory,lightningHandler, paymentHandler,
                currencyConverter,orderRequestGeneratorFactory,null)
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        expect:
        paymentFlow.getNotBeforeDate().isBefore(clock.instant())
    }

    def "Verify getNotBeforeDate returned is after current time if notBeforeDuration is positive"(){
        setup:
        localFlowManager = new TestPaymentFlowManager(PaymentFlowMode.LOCAL,tokenGenerator,
                Duration.ofMinutes(15), requestPolicyFactory,lightningHandler, paymentHandler,
                currencyConverter,orderRequestGeneratorFactory,null)
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        expect:
        paymentFlow.getNotBeforeDate().isAfter(clock.instant())
    }

    def "Verify that checkSettledInvoice throws InternalErrorException since operation isn't supported"(){
        setup:
        localFlowManager = new TestPaymentFlowManager(PaymentFlowMode.LOCAL,tokenGenerator,
                Duration.ofMinutes(15), requestPolicyFactory,lightningHandler, paymentHandler,
                currencyConverter,orderRequestGeneratorFactory,null)
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = localFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        when:
        paymentFlow.checkSettledInvoice()
        then:
        thrown InternalErrorException
    }

    private void verifyLocalPaymentFlow(LocalPaymentFlow paymentFlow, String state){

        assert paymentFlow.request == request
        assert paymentFlow.orderRequest != null
        assert paymentFlow.requestPolicyFactory != null
        assert paymentFlow.lightningHandler != null
        assert paymentFlow.paymentHandler != null
        assert paymentFlow.tokenGenerator != null
        assert paymentFlow.currencyConverter != null
        if(state == "notoken") {
            assert paymentFlow.@paymentRequired != null
            assert paymentFlow.tokenClaims == null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.SETTLEMENT_TOKEN
        }
        if(state == "invoicetokenset") {
            assert paymentFlow.@paymentRequired == null
            assert paymentFlow.tokenClaims != null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.INVOICE_TOKEN
            assert paymentFlow.invoice != null
            assert paymentFlow.preImageHash != null
            assert paymentFlow.requestData != null
            assert paymentFlow.@settlement == null
        }
        if(state == "settlementtokenset"){
            assert paymentFlow.@paymentRequired != null
            assert paymentFlow.tokenClaims != null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.SETTLEMENT_TOKEN
            assert paymentFlow.invoice == null
            assert paymentFlow.preImageHash != null
            assert paymentFlow.requestData == null
            assert paymentFlow.@settlement != null
        }
    }

    static findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    private void setClock(LocalPaymentFlow flow){
        flow.clock = clock
        tokenGenerator.clock = clock
    }

    private inFuture(Duration duration){
        return clock.instant().plus(duration)
    }

    static class AnnotationTest{

        @PaymentRequired(articleId = "123466",  units = 2, requestPolicy = RequestPolicyType.URL_AND_METHOD)
        void paywalledMethod(){}

        @PaymentRequired(articleId = "123467",  units = 1, requestPolicy = RequestPolicyType.URL_AND_METHOD, payPerRequest = true)
        void payPerRequestMethod(){}

    }

}
