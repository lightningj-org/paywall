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

import org.lightningj.paywall.currencyconverter.CurrencyConverter
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory
import org.lightningj.paywall.paymenthandler.PaymentHandler
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.vo.OrderRequest

import java.time.Duration

/**
 * Test implementation of a PaymentFlowManager used in unit tests.
 */
class TestPaymentFlowManager extends BasePaymentFlowManager{

    PaymentFlowMode paymentFlowMode
    TokenGenerator tokenGenerator
    Duration tokenNotBeforeDuration
    RequestPolicyFactory requestPolicyFactory
    LightningHandler lightningHandler
    PaymentHandler paymentHandler
    CurrencyConverter currencyConverter
    OrderRequestGeneratorFactory orderRequestGeneratorFactory
    String centralSystemRecipientId

    List getPaymentFlowModeCalls = []

    TestPaymentFlowManager(PaymentFlowMode paymentFlowMode, TokenGenerator tokenGenerator, Duration tokenNotBeforeDuration, RequestPolicyFactory requestPolicyFactory, LightningHandler lightningHandler, PaymentHandler paymentHandler, CurrencyConverter currencyConverter, OrderRequestGeneratorFactory orderRequestGeneratorFactory, String centralSystemRecipientId) {
        this.paymentFlowMode = paymentFlowMode
        this.tokenGenerator = tokenGenerator
        this.tokenNotBeforeDuration = tokenNotBeforeDuration
        this.requestPolicyFactory = requestPolicyFactory
        this.lightningHandler = lightningHandler
        this.paymentHandler = paymentHandler
        this.currencyConverter = currencyConverter
        this.orderRequestGeneratorFactory = orderRequestGeneratorFactory
        this.centralSystemRecipientId = centralSystemRecipientId
    }

    @Override
    protected TokenGenerator getTokenGenerator() {
        return tokenGenerator
    }


    @Override
    protected PaymentFlowMode getPaymentFlowMode(OrderRequest orderRequest) {
        getPaymentFlowModeCalls << orderRequest
        return paymentFlowMode
    }

    @Override
    protected Duration getTokenNotBeforeDuration() {
        return tokenNotBeforeDuration
    }

    @Override
    protected RequestPolicyFactory getRequestPolicyFactory() {
        return requestPolicyFactory
    }

    @Override
    protected LightningHandler getLightningHandler() {
        return lightningHandler
    }

    @Override
    protected PaymentHandler getPaymentHandler() {
        return paymentHandler
    }

    @Override
    protected CurrencyConverter getCurrencyConverter() {
        return currencyConverter
    }

    @Override
    protected OrderRequestGeneratorFactory getOrderRequestGeneratorFactory() {
        return orderRequestGeneratorFactory
    }

    @Override
    protected String getCentralSystemRecipientId() {
        return centralSystemRecipientId
    }
}
