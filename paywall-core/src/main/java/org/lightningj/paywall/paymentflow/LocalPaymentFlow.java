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
import org.lightningj.paywall.AlreadyExecutedException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.currencyconverter.InvalidCurrencyException;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.requestpolicy.RequestPolicy;
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.vo.*;
import org.lightningj.paywall.vo.amount.CryptoAmount;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * Payment flow use case where all components exists in the same application as
 * the pay walled resource.
 * <p>
 * Should only be created by a BasePaymentFlowManager implementation.
 *
 * Created by Philip Vendil on 2019-01-01.
 */
public class LocalPaymentFlow extends BasePaymentFlow {

    /**
     * Default constructor initializing the local PaymentFlow.
     *
     * @param paymentRequired the annotation signaling the requested resource requires payment.
     *                        Never null.
     * @param request the related HTTP Request in this phase of the payment flow.
     * @param orderRequest the orderRequest calculated either from paymentRequired annotation
     *                     of extracted from JWT token depending on state in the payment flow.
     * @param requestPolicyFactory the used RequestPolicyFactory. Never null.
     * @param lightningHandler the used LightningHandler. Never null.
     * @param paymentHandler the used PaymentHandler. Never null.
     * @param tokenGenerator the user TokenGenerator, should never be null.
     * @param currencyConverter the used CurrencyConverter.Never null.
     * @param tokenClaims all claims parsed from the related JWT token. Null in no related token exists in current state.
     * @param expectedTokenType the expected type of JWT token expected in this state of the payment flow.
     * @param notBeforeDuration the duration for the not before field in generated
     *                          JWT tokens. This can be positive if it should be valid in the future, or negative
     *                          to support skewed clocked between systems. Use null if no not before date should
     *                          be set in generated JWT tokens.
     */
    public LocalPaymentFlow(PaymentRequired paymentRequired, CachableHttpServletRequest request, OrderRequest orderRequest, RequestPolicyFactory requestPolicyFactory,
                            LightningHandler lightningHandler, PaymentHandler paymentHandler, TokenGenerator tokenGenerator, CurrencyConverter currencyConverter,
                            JwtClaims tokenClaims, ExpectedTokenType expectedTokenType, Duration notBeforeDuration) {
        super(paymentRequired, request, orderRequest,
                requestPolicyFactory, lightningHandler,
                paymentHandler, tokenGenerator, currencyConverter,
                tokenClaims, expectedTokenType, notBeforeDuration);

        assert lightningHandler != null : "Internal error, configured LightningHandler cannot be null in local payment flow";
        assert paymentHandler != null  : "Internal error, configured PaymentHandler cannot be null in local payment flow";
        assert requestPolicyFactory != null  : "Internal error, configured RequestPolicyFactory cannot be null in local payment flow";
        assert tokenGenerator != null  : "Internal error, configured TokenGenerator cannot be null in local payment flow";
        assert currencyConverter != null  : "Internal error, configured CurrencyConverter cannot be null in local payment flow";
    }

    /**
     * Method to create and order and an invoice in local lightning handler..
     *
     * @return a value object containing a payment or invoice JWT Token and optionally and invoice.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws InvalidCurrencyException if problems occurred converting the currency in the order to the one
     * used in the invoice.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    @Override
    public InvoiceResult requestPayment() throws IllegalArgumentException, IOException, InternalErrorException, InvalidCurrencyException, TokenException{
            RequestPolicy requestPolicy = getRequestPolicyFactory().getRequestPolicy(paymentRequired);
            requestData = requestPolicy.significantRequestDataDigest(request);

            PreImageData preImageData = getTokenGenerator().genPreImageData();
            preImageHash = preImageData.getPreImageHash();
            Order order = getPaymentHandler().createOrder(preImageData.getPreImageHash(), orderRequest);

            CryptoAmount convertedAmount = getCurrencyConverter().convert(order.getOrderAmount());
            ConvertedOrder convertedOrder = new ConvertedOrder(order,convertedAmount);

            Invoice invoice = getLightningHandler().generateInvoice(preImageData,convertedOrder);
            MinimalInvoice minimalInvoice = new MinimalInvoice(invoice);
            String invoiceToken = getTokenGenerator().generateInvoiceToken(null,minimalInvoice,requestData,invoice.getExpireDate(), getNotBeforeDate(),null);

            return new InvoiceResult(invoice, invoiceToken);
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
        assert requestData != null;
        if (settlement == null) {
            settlement = getPaymentHandler().checkSettlement(preImageHash, false);
        }

        String token = getTokenGenerator().generateSettlementToken(null,settlement,requestData,settlement.getValidUntil(),settlement.getValidFrom(), getSourceNode());
        return new SettlementResult(settlement,token);
    }

    /**
     * Unsupported operation in local payment flow. Throws InternalErrorException.
     *
     * @return InvoiceResult with invoice token if related token is settled, otherwise null.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    public InvoiceResult checkSettledInvoice() throws InternalErrorException{
        throw new InternalErrorException("Unsupported method checkSettledInvoice in local payment flow.");
    }

    /**
     *
     * @return no source node value needed in local flow setup, returning null.
     */
    @Override
    protected String getSourceNode() {
        return null;
    }

}
