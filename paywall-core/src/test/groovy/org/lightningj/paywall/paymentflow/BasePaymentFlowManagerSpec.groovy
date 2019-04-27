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
import org.lightningj.paywall.tokengenerator.TokenContext
import org.lightningj.paywall.tokengenerator.TokenException
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.web.CachableHttpServletRequest
import org.lightningj.paywall.web.HTTPConstants
import spock.lang.Specification

import javax.servlet.http.Cookie

/**
 * Unit tests for BasePaymentFlowManager.
 *
 * Created by Philip Vendil on 2019-01-01.
 */
class BasePaymentFlowManagerSpec extends Specification {

    BasePaymentFlowManager flowManager = new TestPaymentFlowManager(null,null,null,null,null
            ,null,null,null,null)

    // getPaymentFlowByAnnotation is tested in the local and central flows

    // getPaymentFlowFromToken is tested in the local and central flows

    def "Verify that lookupPaymentFlow throws InternalErrorException if mode is CENTRAL_PAYMENT_HANDLER"(){
        setup:
        flowManager = new TestPaymentFlowManager(PaymentFlowMode.CENTRAL_PAYMENT_HANDLER,null,null,null,null
        ,null,null,null,null)
        when:
        flowManager.lookupPaymentFlow(null,null,null,null,null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Unsupported payment flow mode: CENTRAL_PAYMENT_HANDLER"
    }

    def "Verify that hasSettlementToken returns true if HEADER_PAYMENT header exists"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> "sometoken"
        expect:
        flowManager.hasSettlementToken(request)
    }

    def "Verity that hasSettlementToken return false if HEADER_PAYMENT header exists doesn't exist"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> null
        expect:
        !flowManager.hasSettlementToken(request)
    }

    def "Verify that getAndVerifyTokenClaims find the correct token value if expectedTokenType is SETTLEMENT_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> "sometoken"
        TokenGenerator tokenGenerator = Mock(TokenGenerator)
        1 * tokenGenerator.parseToken(TokenContext.CONTEXT_SETTLEMENT_TOKEN_TYPE, "sometoken") >> new JwtClaims()
        flowManager = new TestPaymentFlowManager(null,tokenGenerator,null,null,null
                ,null,null,null,null)
        expect:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.SETTLEMENT_TOKEN) instanceof JwtClaims
    }

    def "Verify that getAndVerifyTokenClaims find the correct token value if expectedTokenType is INVOICE_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getCookies() >> {[new Cookie("somename1", "somevalue1"),
                                      new Cookie((String) HTTPConstants.COOKIE_INVOICE_REQUEST, "sometoken"),
                                      new Cookie("somename2", "somevalue2")] as Cookie[]}
        TokenGenerator tokenGenerator = Mock(TokenGenerator)
        1 * tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, "sometoken") >> new JwtClaims()
        flowManager = new TestPaymentFlowManager(null,tokenGenerator,null,null,null
                ,null,null,null,null)
        expect:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.INVOICE_TOKEN) instanceof JwtClaims
    }

    def "Verify that getAndVerifyTokenClaims find the correct token value if expectedTokenType is PAYMENT_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getCookies() >> {[new Cookie("somename1", "somevalue1"),
                                      new Cookie((String) HTTPConstants.COOKIE_PAYMENT_REQUEST, "sometoken"),
                                      new Cookie("somename2", "somevalue2")] as Cookie[]}
        TokenGenerator tokenGenerator = Mock(TokenGenerator)
        1 * tokenGenerator.parseToken(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE, "sometoken") >> new JwtClaims()
        flowManager = new TestPaymentFlowManager(null,tokenGenerator,null,null,null
                ,null,null,null,null)
        expect:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.PAYMENT_TOKEN) instanceof JwtClaims
    }

    def "Verify that getAndVerifyTokenClaims throws TokenException if no token found for expectedTokenType is PAYMENT_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getCookies() >> {[new Cookie("somename1", "somevalue1"),
                                      new Cookie("somename2", "somevalue2")] as Cookie[]}
        when:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.PAYMENT_TOKEN)
        then:
        def e = thrown(TokenException)
        e.message == "No related JWT token found for payment flow."
        e.reason == TokenException.Reason.NOT_FOUND
    }

    def "Verify that getAndVerifyTokenClaims throws TokenException if no token found for expectedTokenType is INVOICE_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getCookies() >> {[new Cookie("somename1", "somevalue1"),
                                      new Cookie("somename2", "somevalue2")] as Cookie[]}
        when:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.INVOICE_TOKEN)
        then:
        def e = thrown(TokenException)
        e.message == "No related JWT token found for payment flow."
        e.reason == TokenException.Reason.NOT_FOUND
    }

    def "Verify that getAndVerifyTokenClaims throws TokenException if no token found for expectedTokenType is SETTLEMENT_TOKEN"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getHeader(HTTPConstants.HEADER_PAYMENT) >> null
        when:
        flowManager.getAndVerifyTokenClaims(request,ExpectedTokenType.SETTLEMENT_TOKEN)
        then:
        def e = thrown(TokenException)
        e.message == "No related JWT token found for payment flow."
        e.reason == TokenException.Reason.NOT_FOUND
    }

    def "Verify that findToken check parameter first and returns it's value if not null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST) >> "SomeValue"
        0 * request.getHeader(_)
        0 * request.getCookies()
        expect:
        flowManager.findToken(request,HTTPConstants.PARAMETER_INVOICE_REQUEST, HTTPConstants.HEADER_INVOICE_REQUEST, HTTPConstants.COOKIE_INVOICE_REQUEST) == "SomeValue"
    }

    def "Verify that findToken check header if parameter was null and returns it's value if not null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST) >> null
        1 * request.getHeader(HTTPConstants.HEADER_INVOICE_REQUEST)  >> "SomeValue"
        0 * request.getCookies()
        expect:
        flowManager.findToken(request,HTTPConstants.PARAMETER_INVOICE_REQUEST, HTTPConstants.HEADER_INVOICE_REQUEST, HTTPConstants.COOKIE_INVOICE_REQUEST) == "SomeValue"
    }

    def "Verify that findToken check cookie if parameter and header was null and returns it's value if not null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        1 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST) >> null
        1 * request.getHeader(HTTPConstants.HEADER_INVOICE_REQUEST)  >> null
        1 * request.getCookies()  >> {[new Cookie("somename1", "somevalue1"),
                                       new Cookie((String) HTTPConstants.COOKIE_INVOICE_REQUEST, "sometoken"),
                                       new Cookie("somename2", "somevalue2")] as Cookie[]}
        expect:
        flowManager.findToken(request,HTTPConstants.PARAMETER_INVOICE_REQUEST, HTTPConstants.HEADER_INVOICE_REQUEST, HTTPConstants.COOKIE_INVOICE_REQUEST) == "sometoken"
    }


    def "Verify that findToken doesn't check parameters of parameterName is null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        0 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST)
        1 * request.getHeader(HTTPConstants.HEADER_INVOICE_REQUEST)  >> "SomeValue"
        0 * request.getCookies()
        expect:
        flowManager.findToken(request,null, HTTPConstants.HEADER_INVOICE_REQUEST, HTTPConstants.COOKIE_INVOICE_REQUEST) == "SomeValue"
    }

    def "Verify that findToken doesn't check header of headerName is null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        0 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST)
        0 * request.getHeader(HTTPConstants.HEADER_INVOICE_REQUEST)
        1 * request.getCookies()  >> {[new Cookie("somename1", "somevalue1"),
                                       new Cookie((String) HTTPConstants.COOKIE_INVOICE_REQUEST, "sometoken"),
                                       new Cookie("somename2", "somevalue2")] as Cookie[]}
        expect:
        flowManager.findToken(request,null, null, HTTPConstants.COOKIE_INVOICE_REQUEST) == "sometoken"
    }

    def "Verify that findToken doesn't check cookie of cookieName is null"(){
        setup:
        CachableHttpServletRequest request = Mock(CachableHttpServletRequest)
        0 * request.getParameter(HTTPConstants.PARAMETER_INVOICE_REQUEST)
        0 * request.getHeader(HTTPConstants.HEADER_INVOICE_REQUEST)
        0 * request.getCookies()
        expect:
        flowManager.findToken(request,null, null, null) == null
    }
}
