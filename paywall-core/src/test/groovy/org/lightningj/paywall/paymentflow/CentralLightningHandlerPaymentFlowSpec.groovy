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
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.annotations.PaymentRequired
import org.lightningj.paywall.currencyconverter.CurrencyConverter
import org.lightningj.paywall.currencyconverter.SameCryptoCurrencyConverter
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager
import org.lightningj.paywall.keymgmt.Context
import org.lightningj.paywall.keymgmt.DummyKeyManager
import org.lightningj.paywall.keymgmt.KeySerializationHelper
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.lightninghandler.LightningHandlerContext
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory
import org.lightningj.paywall.paymenthandler.PaymentHandler
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory
import org.lightningj.paywall.tokengenerator.AsymmetricKeyTokenGenerator
import org.lightningj.paywall.tokengenerator.KeyIdByFileRecipientRepository
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
import org.lightningj.paywall.vo.PreImageOrder
import org.lightningj.paywall.vo.Settlement
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.web.CachableHttpServletRequest
import org.lightningj.paywall.web.HTTPConstants
import spock.lang.Specification

import javax.servlet.http.Cookie
import java.security.PublicKey
import java.time.Clock
import java.time.Duration
import java.time.Instant

import static org.lightningj.paywall.paymentflow.LocalPaymentFlowSpec.findAnnotation

/**
 * Unit tests for CentralLightningHandlerPaymentFlow.
 *
 * Created by Philip Vendil on 2019-01-06.
 */
class CentralLightningHandlerPaymentFlowSpec extends Specification {

    TestPaymentFlowManager filterFlowManager
    TestPaymentFlowManager centralFlowManager

    TokenGenerator filterTokenGenerator
    TokenGenerator centralTokenGenerator
    Duration tokenNotBeforeDuration = Duration.ofMinutes(-15)
    LightningHandler lightningHandler = Mock(LightningHandler)
    PaymentHandler paymentHandler = Mock(PaymentHandler)
    CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
    RequestPolicyFactory requestPolicyFactory = new RequestPolicyFactory()
    CurrencyConverter currencyConverter = new SameCryptoCurrencyConverter()
    OrderRequestGeneratorFactory orderRequestGeneratorFactory = new OrderRequestGeneratorFactory()
    Clock clock = Mock(Clock)
    Instant currentTime = Instant.ofEpochMilli(1546606166000L)
    String centralSystemKeyId
    String filterSystemKeyId

    NodeInfo nodeInfo = new NodeInfo("asdfa123123@123.123.123.123")

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup(){
        clock.instant() >> { currentTime }
        clock.millis() >> { currentTime.toEpochMilli()}


        AsymmetricKeyManager filterKeyManager =  new TestKeyManager()
        KeyIdByFileRecipientRepository filterRecipientDirectory = new KeyIdByFileRecipientRepository(filterKeyManager)
        filterTokenGenerator = new AsymmetricKeyTokenGenerator(filterKeyManager, filterRecipientDirectory)

        AsymmetricKeyManager centralKeyManager = new TestKeyManager()
        KeyIdByFileRecipientRepository centralRecipientDirectory = new KeyIdByFileRecipientRepository(filterKeyManager)
        centralTokenGenerator = new AsymmetricKeyTokenGenerator(centralKeyManager,centralRecipientDirectory)
        centralSystemKeyId = setupTrustBetweehTokenGenerators(filterKeyManager,centralKeyManager)
        filterSystemKeyId = KeySerializationHelper.genKeyId(filterKeyManager.getPublicKey(null).encoded)

        filterFlowManager = new TestPaymentFlowManager(PaymentFlowMode.CENTRAL_LIGHTNING_HANDLER,filterTokenGenerator,
                tokenNotBeforeDuration, requestPolicyFactory,null, paymentHandler,
                null,orderRequestGeneratorFactory,centralSystemKeyId, false)

        centralFlowManager = new TestPaymentFlowManager(PaymentFlowMode.CENTRAL_LIGHTNING_HANDLER,centralTokenGenerator,
                tokenNotBeforeDuration, null,lightningHandler, null,
                currencyConverter,null,centralSystemKeyId, false)
    }

    def "Verify a successful payment flow for non pay per request flow."(){
        // On pay-walled resource filter.
        when: "First simulate call to filter that have detected a pay-walled resource and not settlement token exists"
        PaymentRequired paymentRequired = findAnnotation("paywalledMethod")
        PaymentFlow paymentFlow = filterFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        Instant orderExpireDate = inFuture(Duration.ofMinutes(60))
        Instant invoiceExpireDate = inFuture(Duration.ofMinutes(120))
        then: "Verify that correct local payment flow was returned"
        paymentFlow instanceof CentralLightningHandlerPaymentFlow
        verifyCentralPaymentFlow(paymentFlow, "filter","notoken")
        paymentFlow.orderRequest.articleId == paymentRequired.articleId()
        paymentFlow.orderRequest.units == paymentRequired.units()

        and: "Verify that payment is required"
        paymentFlow.isPaymentRequired()

        when: "Verify that correct payment required data is returned"
        InvoiceResult filterRequestPaymentResult = paymentFlow.requestPayment()
        then:
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        1 * paymentHandler.createOrder(_,_) >> {
            byte[] preImageHash, OrderRequest orderRequest ->
                assert orderRequest.articleId == paymentRequired.articleId()
                return new Order(preImageHash, "Some description",new BTC(10000), orderExpireDate)
        }
        0 * lightningHandler.generateInvoice(_,_)

        filterRequestPaymentResult.invoice == null

        JwtClaims claims = centralTokenGenerator.parseToken(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE, filterRequestPaymentResult.token)
        PreImageOrder preImageOrder = new PreImageOrder(claims)
        preImageOrder.preImageHash != null
        preImageOrder.preImage != null

        // On  newInvoice controller
        when:
        paymentFlow = centralFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.PAYMENT_TOKEN)
        setClock(paymentFlow)
        verifyCentralPaymentFlow(paymentFlow, "central","paymenttokenset")

        then:
        paymentFlow.isPaymentRequired()
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_PAYMENT_REQUEST,filterRequestPaymentResult.token)]

        when:
        InvoiceResult centralRequestPaymentResult = paymentFlow.requestPayment()

        claims = centralTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, centralRequestPaymentResult.token)
        Invoice invoice = new Invoice(claims)
        then:
        centralRequestPaymentResult.invoice.sourceNode == filterSystemKeyId
        1 * lightningHandler.generateInvoice(_,_) >> {
            PreImageData preImageData, ConvertedOrder convertedOrder ->
                return new Invoice(preImageData.preImageHash, "somebolt11invoice",
                        convertedOrder.description,convertedOrder.convertedAmount,nodeInfo,
                        invoiceExpireDate,clock.instant())
        }
        invoice.preImageHash == centralRequestPaymentResult.invoice.preImageHash

        // On checkSettlement on central controller
        when: "Simulate controller to check settlement, not settled yet"
        paymentFlow = centralFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        setClock(paymentFlow)
        verifyCentralPaymentFlow(paymentFlow,"central","invoicetokenset")

        InvoiceResult centralInvoiceResult = paymentFlow.checkSettledInvoice()
        then:
        !centralInvoiceResult
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,centralRequestPaymentResult.token)]
        1 * lightningHandler.lookupInvoice(invoice.preImageHash) >> {
            assert !invoice.settled
            return invoice
        }

        when: "Verify if token is settled then is a settlement token returned"
        paymentFlow = centralFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        setClock(paymentFlow)
        verifyCentralPaymentFlow(paymentFlow,"central","invoicetokenset")

        centralInvoiceResult = paymentFlow.checkSettledInvoice()

        claims = filterTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, centralInvoiceResult.token)
        Invoice tokenInvoice = new Invoice(claims)
        then:
        centralInvoiceResult
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,centralRequestPaymentResult.token)]
        1 * lightningHandler.lookupInvoice(invoice.preImageHash) >> {
            invoice.settled = true
            return invoice
        }
        centralInvoiceResult.invoice.settled
        tokenInvoice.settled
        paymentFlow.@settlement == null


        when: "After redirected to local systems genSettlement controller"
        paymentFlow = filterFlowManager.getPaymentFlowFromToken(request, ExpectedTokenType.INVOICE_TOKEN)
        setClock(paymentFlow)
        verifyCentralPaymentFlow(paymentFlow,"filter","invoicetokenset")

        boolean isSettled = paymentFlow.isSettled()
        SettlementResult settlementResult = paymentFlow.getSettlement()

        then:
        isSettled
        settlementResult
        1 * request.getCookies() >> [new Cookie(HTTPConstants.COOKIE_INVOICE_REQUEST,centralInvoiceResult.token)]
        1 * paymentHandler.registerSettledInvoice(_,false,_,null) >> { Invoice settledInvoice, boolean registerNew,
        OrderRequest orderRequest, LightningHandlerContext context ->
            assert settledInvoice.settled
            assert settledInvoice.preImageHash == centralInvoiceResult.invoice.preImageHash
            return new Settlement(invoice.preImageHash,null,inFuture(Duration.ofMinutes(180)), null,false)
        }
        paymentFlow.@settlement != null


        // Back on pay-walled resource filter.
        when: "Check that valid settlement token in the filter header returns payment required as false."
        paymentFlow = filterFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        verifyCentralPaymentFlow(paymentFlow,"filter","settlementtokenset")
        and:
        boolean isPaymentRequired = paymentFlow.isPaymentRequired()
        then:
        !isPaymentRequired
        1 * request.getMethod() >> "POST"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

        when: "Check that is is possible to run multiple times until it is expired"
        paymentFlow = filterFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
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
        paymentFlow = filterFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        setClock(paymentFlow)
        and:
        isPaymentRequired = paymentFlow.isPaymentRequired()
        then:
        !isPaymentRequired
        def e = thrown IllegalArgumentException
        e.message == "Error request data doesn't match data in settlement token."
        1 * request.getMethod() >> "GET"
        1 * request.getRequestURL() >> new StringBuffer("http://test1/test")
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

        when: "Check that token exception is thrown if settlement token expired."
        currentTime = inFuture(Duration.ofMinutes(200))
        filterFlowManager.getPaymentFlowByAnnotation(paymentRequired,request)
        then:
        e = thrown TokenException
        e.reason == TokenException.Reason.EXPIRED
        e.message == "JWT Token have expired."
        2 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> settlementResult.token

    }


    private void verifyCentralPaymentFlow(CentralLightningHandlerPaymentFlow paymentFlow, String location, String state){

        assert paymentFlow.request == request
        assert paymentFlow.tokenGenerator != null
        assert paymentFlow.orderRequest != null
        if(location == "filter"){
            assert paymentFlow.requestPolicyFactory != null
            assert paymentFlow.lightningHandler == null
            assert paymentFlow.paymentHandler != null
            assert paymentFlow.currencyConverter == null
        }
        if(location == "central"){
            assert paymentFlow.requestPolicyFactory == null
            assert paymentFlow.lightningHandler != null
            assert paymentFlow.paymentHandler == null
            assert paymentFlow.currencyConverter != null
        }

        if(state == "notoken") {
            assert location == "filter"
            assert paymentFlow.@paymentRequired != null
            assert paymentFlow.tokenClaims == null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.SETTLEMENT_TOKEN
        }
        if(state == "paymenttokenset") {
            assert location == "central"
            assert paymentFlow.@paymentRequired == null
            assert paymentFlow.tokenClaims != null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.PAYMENT_TOKEN
            assert paymentFlow.order != null
            assert paymentFlow.invoice == null
            assert paymentFlow.preImageHash != null
            assert paymentFlow.requestData != null
            assert paymentFlow.@settlement == null
        }
        if(state == "settlementtokenset"){
            assert location == "filter"
            assert paymentFlow.@paymentRequired != null
            assert paymentFlow.tokenClaims != null
            assert paymentFlow.expectedTokenType == ExpectedTokenType.SETTLEMENT_TOKEN
            assert paymentFlow.invoice == null
            assert paymentFlow.preImageHash != null
            assert paymentFlow.requestData == null
            assert paymentFlow.@settlement != null
        }
    }

    private void setClock(PaymentFlow flow){
        flow.clock = clock
        filterTokenGenerator.clock = clock
        centralTokenGenerator.clock = clock
    }

    private inFuture(Duration duration){
        return clock.instant().plus(duration)
    }

    private String setupTrustBetweehTokenGenerators(TestKeyManager filterKeyManager, TestKeyManager centralKeyManager){
        String centralSystemKeyId = KeySerializationHelper.genKeyId(centralKeyManager.getPublicKey(null).encoded)
        String filterKeyId = KeySerializationHelper.genKeyId(filterKeyManager.getPublicKey(null).encoded)

        Map<String, PublicKey> keys = [(centralSystemKeyId) : centralKeyManager.getPublicKey(null),
                                       (filterKeyId) : filterKeyManager.getPublicKey(null)]

        filterKeyManager.trustedKeys = keys
        centralKeyManager.trustedKeys = keys

        return centralSystemKeyId
    }

    static class TestKeyManager extends DummyKeyManager{

        Map<String, PublicKey> trustedKeys = null;

        /**
         * Own keys are trusted in dummy setup.
         *
         */
        @Override
        Map<String, PublicKey> getTrustedKeys(Context context) throws UnsupportedOperationException, InternalErrorException {
            return trustedKeys
        }

        /**
         * Own keys are trusted in dummy setup.
         *
         */
        @Override
        Map<String, PublicKey> getReceipients(Context context) throws UnsupportedOperationException, InternalErrorException {
            return trustedKeys
        }
    }
}
