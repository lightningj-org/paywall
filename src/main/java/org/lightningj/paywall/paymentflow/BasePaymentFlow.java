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
import org.jose4j.jwt.MalformedClaimException;
import org.lightningj.paywall.AlreadyExecutedException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.requestpolicy.RequestPolicy;
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.vo.*;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Base class containing common functionality between payment flows.
 *
 * @see LocalPaymentFlow
 * @see CentralLightningHandlerPaymentFlow
 *
 * Created by Philip Vendil on 2019-01-01.
 */
public abstract class BasePaymentFlow implements PaymentFlow{

   protected Clock clock = Clock.systemDefaultZone();

   protected PaymentRequired paymentRequired;

   protected CachableHttpServletRequest request;
   protected OrderRequest orderRequest;

   protected JwtClaims tokenClaims;
   protected ExpectedTokenType expectedTokenType;

   private RequestPolicyFactory requestPolicyFactory;
   private LightningHandler lightningHandler;
   private PaymentHandler paymentHandler;
   private TokenGenerator tokenGenerator;
   private CurrencyConverter currencyConverter;
   private Duration notBeforeDuration;

   private boolean checkRequestData = false;
   protected PreImageOrder order = null;
   protected Invoice invoice = null;
   protected Settlement settlement = null;
   protected byte[] preImageHash = null;
   protected RequestData requestData = null;

    /**
     * Default constructor initializing the common parts of a PaymentFlow.
     *
     * @param paymentRequired the annotation signaling the requested resource requires payment.
     *                        Can be null for some nodes in a distributed setuo.
     * @param request the related HTTP Request in this phase of the payment flow.
     * @param orderRequest the orderRequest calculated either from paymentRequired annotation
     *                     of extracted from JWT token depending on state in the payment flow.
     * @param requestPolicyFactory the used RequestPolicyFactory. Might be null for nodes in a distributed setup.
     * @param lightningHandler the used LightningHandler. Might be null for nodes in a distributed setup.
     * @param paymentHandler the used PaymentHandler. Might be null for nodes in a distributed setup.
     * @param tokenGenerator the user TokenGenerator, should never be null.
     * @param currencyConverter the used CurrencyConverter. Might be null for nodes in a distributed setup.
     * @param tokenClaims all claims parsed from the related JWT token. Null in no related token exists in current state.
     * @param expectedTokenType the expected type of JWT token expected in this state of the payment flow.
     * @param notBeforeDuration the duration for the not before field in generated
     *                          JWT tokens. This can be positive if it should be valid in the future, or negative
     *                          to support skewed clocked between systems. Use null if no not before date should
     *                          be set in generated JWT tokens.
     */
   public BasePaymentFlow(PaymentRequired paymentRequired,
                          CachableHttpServletRequest request,
                          OrderRequest orderRequest,
                          RequestPolicyFactory requestPolicyFactory,
                          LightningHandler lightningHandler,
                          PaymentHandler paymentHandler,
                          TokenGenerator tokenGenerator,
                          CurrencyConverter currencyConverter,
                          JwtClaims tokenClaims,
                          ExpectedTokenType expectedTokenType,
                          Duration notBeforeDuration){
       this.paymentRequired = paymentRequired;
       this.request = request;
       this.orderRequest = orderRequest;

       this.requestPolicyFactory = requestPolicyFactory;
       this.lightningHandler = lightningHandler;
       this.paymentHandler = paymentHandler;
       this.tokenGenerator = tokenGenerator;
       this.currencyConverter = currencyConverter;

       this.tokenClaims = tokenClaims;
       this.expectedTokenType = expectedTokenType;

       this.notBeforeDuration = notBeforeDuration;

       if(tokenClaims != null && tokenClaims.hasClaim(Settlement.CLAIM_NAME)){
           checkRequestData = true;
           settlement = new Settlement(tokenClaims);
           preImageHash = settlement.getPreImageHash();
       }

       switch (expectedTokenType){
           case PAYMENT_TOKEN:
               order = new PreImageOrder(tokenClaims);
               preImageHash = order.getPreImageHash();
               requestData = new RequestData(tokenClaims);
               break;
           case INVOICE_TOKEN:
               invoice = new Invoice(tokenClaims);
               preImageHash = invoice.getPreImageHash();
               requestData = new RequestData(tokenClaims);
               break;
       }
   }

    /**
     *
     * @return the used RequestPolicyFactory. Might be null for nodes in a distributed setup.
     */
    protected RequestPolicyFactory getRequestPolicyFactory() {
        return requestPolicyFactory;
    }

    /**
     *
     * @return the used LightningHandler. Might be null for nodes in a distributed setup.
     */
    protected LightningHandler getLightningHandler() {
        return lightningHandler;
    }

    /**
     *
     * @return the used PaymentHandler. Might be null for nodes in a distributed setup.
     */
    protected PaymentHandler getPaymentHandler() {
        return paymentHandler;
    }

    /**
     *
     * @return the user TokenGenerator, should never be null.
     */
    protected TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    /**
     *
     * @return the used CurrencyConverter. Might be null for nodes in a distributed setup.
     */
    protected CurrencyConverter getCurrencyConverter() {
        return currencyConverter;
    }

    /**
     *
     * @return the timestamp that should be used in the notBefore claim of the JWT token.
     * Returns null if no notBefore claim should be set in token.
     */
    protected Instant getNotBeforeDate(){
       if(notBeforeDuration == null){
           return null;
       }
       return clock.instant().plus(notBeforeDuration);
    }

    /**
     *
     * @return the reciepient id of encrypted JWT tokens for the payment flow source node
     * (the node that started the flow with a PaymentRequired annotation.)
     */
    protected abstract String getSourceNode();

    /**
     * Method that should determine if the current state of payment flow. Should
     * for instance return false if valid settlement is included in the request.
     *
     * @return true if payment is currently required in for related HTTP request.
     */
    @Override
    public boolean isPaymentRequired() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException {
        if(checkRequestData){
            if(settlement.isPayPerRequest()){
                // An AlreadyExecutedException is thrown if settlement is already executed.
                getPaymentHandler().checkSettlement(settlement.getPreImageHash(),false);
            }

            RequestPolicy requestPolicy = getRequestPolicyFactory().getRequestPolicy(paymentRequired);
            RequestData currentRequestData = requestPolicy.significantRequestDataDigest(request);

            RequestData originalRequestData = new RequestData(tokenClaims);
            if(Arrays.equals(currentRequestData.getSignificantData(), originalRequestData.getSignificantData())){
                return false;
            }
            throw new IllegalArgumentException("Error request data doesn't match data in settlement token.");
        }
        return true;

    }

    /**
     * Method that should indicate if related payment flow is for one request only or valid
     * over a given period.
     *
     * @return true if related payment is for one request only.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    @Override
    public boolean isPayPerRequest() throws InternalErrorException {
        if(paymentRequired != null){
            return paymentRequired.payPerRequest();
        }
        if(settlement != null){
            return settlement.isPayPerRequest();
        }
        throw new InternalErrorException("Error determining if payment is pay per request, neither PaymentRequired annotation or settlement token found");
    }

    /**
     * Method that should be called by a filter or equivalent after successful execution of
     * payed request and the related payment flow is payPerRequest.
     *
     * This method calls the used payment handle to mark the preImageHash as executed.
     *
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    @Override
    public void markAsExecuted() throws IllegalArgumentException, IOException, InternalErrorException {
       if(settlement == null){
           throw new IllegalArgumentException("Internal error marking payment flow as executed, no settlement found.");
       }
        getPaymentHandler().markAsExecuted(settlement.getPreImageHash());
    }

    /**
     * Method to check if related payment is settled by the end user.
     *
     * @return true if settled.
     *
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    @Override
    public boolean isSettled() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException {
        if(settlement == null) {
            settlement = getPaymentHandler().checkSettlement(preImageHash, false);
        }
        return settlement != null;
    }

    /**
     * Method to retrieve a settlement and generate a settlement token.
     *
     * @return a value object containing the settlement and the related settlement token.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws TokenException if problem occurred generating the settlement token.
     */
    @Override
    public SettlementResult getSettlement() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException, TokenException {
        assert orderRequest != null;
        assert requestData != null;
        if(settlement == null){
            settlement = getPaymentHandler().checkSettlement(preImageHash, false);
        }
        String token = getTokenGenerator().generateSettlementToken(orderRequest,settlement,requestData,settlement.getValidUntil(),settlement.getValidFrom(), getSourceNode());
        return new SettlementResult(settlement,token);
    }

    /**
     * Help method to retrieve the issuer claim from a JWT token.
     *
     * @return the issuer claim from token or null if no issuer found in JWT Claim.
     * @throws TokenException if problems occurred parsing issuer from JWT claims.
     */
    protected String getTokenIssuer() throws TokenException{
        try{
            String retval = null;
            if(tokenClaims != null){
                retval = tokenClaims.getIssuer();
            }
            return retval;
        }catch (MalformedClaimException e){
            throw new TokenException("Invalid JWT token, no issuer found in token: " + e.getMessage(),e, TokenException.Reason.INVALID);
        }
    }

}
