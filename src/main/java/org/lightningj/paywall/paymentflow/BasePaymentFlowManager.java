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
package org.lightningj.paywall.paymentflow;

import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGenerator;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.web.CachableHttpServletRequest;
import org.lightningj.paywall.web.HTTPConstants;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.time.Duration;

/**
 * Base class for all PaymentFlowManager implementations that is in charge of creating payment flows
 * for a given PaymentRequired annotation and managing all related sub-components such as TokenGenerator
 * and LightningHandler etc.
 *
 * <p>
 *     There are numerous empty methods that should be implemented for different modes and states
 *     of payment flows.
 * </p>
 * Created by Philip Vendil on 2018-12-20.
 */
public abstract class BasePaymentFlowManager implements PaymentFlowManager{

    /**
     * Method to create a new instance of related mode of PaymentFlow initialized with
     * current state in the payment flow. Usually when initiating a new payment flow or
     * checking if a current payment flow is settled.
     *
     * @param paymentRequired the related annotation for the resource needing payment.
     * @param request the HTTP request to parse JWT token from.
     * @return a new instance of related payment flow.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    public PaymentFlow getPaymentFlowByAnnotation(PaymentRequired paymentRequired, CachableHttpServletRequest request) throws InternalErrorException, IOException, TokenException {
        assert getOrderRequestGeneratorFactory() != null : "Internal error, configured OrderRequestGeneratorFactory cannot be null in payment flow.";

        OrderRequestGenerator orderRequestGenerator = getOrderRequestGeneratorFactory().getGenerator(paymentRequired);
        OrderRequest orderRequest = orderRequestGenerator.generate(paymentRequired,request);

        JwtClaims claims = null;
        if(hasSettlementToken(request)){
            claims = getAndVerifyTokenClaims(request,ExpectedTokenType.SETTLEMENT_TOKEN);
        }

        return lookupPaymentFlow(paymentRequired,request,orderRequest,claims, ExpectedTokenType.SETTLEMENT_TOKEN);
    }

    /**
     * Method to create a new instance of related mode of PaymentFlow initialized with
     * current state in the payment flow.
     * <p>
     *     This method should be called in states that doesn't have access to the
     *     PaymentRequired annotation, such as status controllers and similar.
     * </p>
     * @param request the HTTP request to parse JWT token from.
     * @param expectedTokenType the expected type of JWT token.
     * @return a new instance of related payment flow.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    public PaymentFlow getPaymentFlowFromToken(CachableHttpServletRequest request, ExpectedTokenType expectedTokenType) throws IllegalArgumentException, InternalErrorException, IOException, TokenException {

        JwtClaims claims = getAndVerifyTokenClaims(request,expectedTokenType);
        OrderRequest orderRequest = new OrderRequest(claims);

        return lookupPaymentFlow(null,request,orderRequest,claims,expectedTokenType);
    }

    /**
     *
     * @return the PaymentHandler used.  Not all implementations
     *      * need to override this method, usually on nodes containing pay-walled resources.
     */
    protected RequestPolicyFactory getRequestPolicyFactory(){
        return null;
    }

    /**
     *
     * @return the PaymentHandler used.  Not all implementations
     * need to override this method, usually on nodes containing generating lightning invoices.
     */
    protected LightningHandler getLightningHandler() {
        return null;
    }

    /**
     *
     * @return the PaymentHandler used.  Not all implementations
     * need to override this method, usually on nodes nodes containing pay-walled resources
     * except if PaymentFlowMode.CENTRAL_PAYMENT_HANDLER then should this be implemented
     * on same node as lightning handler.
     */
    protected PaymentHandler getPaymentHandler() {
        return null;
    }

    /**
     *
     * @return the CurrencyConverter used.  Not all implementations
     * need to override this method, usually on nodes containing lightning handler.
     */
    protected CurrencyConverter getCurrencyConverter() {
        return null;
    }

    /**
     *
     * @return the OrderRequestGeneratorFactory used. Not all implementations
     * need to override this method, usually on nodes containing pay-walled resources.
     */
    protected OrderRequestGeneratorFactory getOrderRequestGeneratorFactory(){
        return null;
    }

    /**
     * Method that must be implemented and return the TokenGenerator used.
     * @return the token generator used.
     */
    protected abstract TokenGenerator getTokenGenerator();

    /**
     * Method that should return mode of payment flow in relation.
     *
     * @see PaymentFlowMode
     *
     * @param orderRequest the related order request.
     * @return the mode that should be used for the given order request.
     */
    protected abstract PaymentFlowMode getPaymentFlowMode(OrderRequest orderRequest);

    /**
     * Method that should be overridden in distributed setups.
     *
     * @return the recipient id of the decryption key of the central system to encrypt
     * JWT tokens to.
     */
    protected String getCentralSystemRecipientId(){
        return null;
    }

    /**
     * @return the duration for the not before field in generated
     * JWT tokens. This can be positive if it should be valid in the future, or negative
     * to support skewed clocked between systems. Use null if no not before date should
     * be set in generated JWT tokens.
     */
    protected abstract Duration getTokenNotBeforeDuration();

    /**
     * @return true if settled invoices are presented before any order is created should
     * be registered as new payments automatically when register them as settled.
     */
    protected abstract boolean getRegisterNewInvoices();

    /**
     * Help method creating a new instance of configured payment flow (depending on PaymentFlowMode).
     *
     *
     * @param paymentRequired the annotation signaling the requested resource requires payment.
     *                        Can be null for some nodes in a distributed setup.
     * @param request the related HTTP Request in this phase of the payment flow.
     * @param orderRequest the orderRequest calculated either from paymentRequired annotation
     *                     of extracted from JWT token depending on state in the payment flow.
     * @param tokenClaims all claims parsed from the related JWT token. Null in no related token exists in current state.
     * @param expectedTokenType the expected type of JWT token expected in this state of the payment flow.
     * @return the configure payment flow for given system.
     * @throws InternalErrorException if internal errors occurred creating the payment flow.
     */
    protected PaymentFlow lookupPaymentFlow(PaymentRequired paymentRequired,CachableHttpServletRequest request, OrderRequest orderRequest,
                                            JwtClaims tokenClaims, ExpectedTokenType expectedTokenType) throws InternalErrorException{
        PaymentFlowMode mode = getPaymentFlowMode(orderRequest);
        switch (mode){
            case LOCAL:
                return new LocalPaymentFlow(paymentRequired,request,
                        orderRequest,getRequestPolicyFactory(),getLightningHandler(),
                        getPaymentHandler(),getTokenGenerator(),getCurrencyConverter(),
                        tokenClaims,expectedTokenType,getTokenNotBeforeDuration());
            case CENTRAL_LIGHTNING_HANDLER:
                return new CentralLightningHandlerPaymentFlow(paymentRequired,request,
                        orderRequest,getRequestPolicyFactory(),getLightningHandler(),
                        getPaymentHandler(),getTokenGenerator(),getCurrencyConverter(),
                        tokenClaims,expectedTokenType,getTokenNotBeforeDuration(),
                        getCentralSystemRecipientId(), getRegisterNewInvoices());
            case CENTRAL_PAYMENT_HANDLER:
                default:
                    throw new InternalErrorException("Unsupported payment flow mode: " + mode);
        }
    }

    /**
     * Help method checking if JWT token is set in HTTP header. Does not actually
     * verify the token nor it's content.
     *
     * @param request the http request to lookup JWT in.
     * @return true if JWT token exists in header.
     */
    protected boolean hasSettlementToken(CachableHttpServletRequest request){
        return request.getHeader(HTTPConstants.HEADER_PAYMENT) != null;
    }

    /**
     * Help method to find and verify JWT token from a HTTP request. For settlement tokens
     * are token stored in HTTP header and for payment and invoice token is stored in a cookie.
     *
     * @param request the http request to parse token from.
     * @param expectedTokenType the expected type token to parse and verify.
     * @return the claims of related token if expected token was found and was valid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the token.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    protected JwtClaims getAndVerifyTokenClaims(CachableHttpServletRequest request, ExpectedTokenType expectedTokenType) throws TokenException, InternalErrorException, IOException {
        String tokenData = null;

        switch (expectedTokenType){
            case SETTLEMENT_TOKEN:
                tokenData = request.getHeader(HTTPConstants.HEADER_PAYMENT);
                break;
            case INVOICE_TOKEN:
                tokenData = findCookie(request, HTTPConstants.COOKIE_INVOICE_REQUEST);
                break;
            case PAYMENT_TOKEN:
                tokenData = findCookie(request, HTTPConstants.COOKIE_PAYMENT_REQUEST);
        }
        if(tokenData == null){
            throw new TokenException("No related JWT token found for payment flow.", TokenException.Reason.NOT_FOUND);
        }
        return getTokenGenerator().parseToken(expectedTokenType.getTokenContext(),tokenData);
    }

    /**
     * Help method to find first cookie with specified name.
     *
     * @param request the http request to parse cookie from
     * @param cookieName the name of the cookie to find.
     * @return the related cookie value or null if no cookie with given name found.
     */
    protected String findCookie(CachableHttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
