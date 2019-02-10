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
package org.lightningj.paywall.spring.local;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory;
import org.lightningj.paywall.paymentflow.BasePaymentFlowManager;
import org.lightningj.paywall.paymentflow.PaymentFlowMode;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory;
import org.lightningj.paywall.spring.PaywallProperties;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.vo.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.logging.Logger;

import static org.lightningj.paywall.util.SettingUtils.checkBooleanWithDefault;
import static org.lightningj.paywall.util.SettingUtils.checkRequiredLong;

/**
 * A Spring implementation of local mode payment flow manager. Manages
 * spring properties and dependency injections.
 *
 *
 */
public class SpringLocalPaymentFlowManager extends BasePaymentFlowManager {

    static Logger log = Logger.getLogger(SpringLocalPaymentFlowManager.class.getName());

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    PaywallProperties paywallProperties;

    @Autowired
    LightningHandler lightningHandler;

    @Autowired
    CurrencyConverter currencyConverter;

    @Autowired
    OrderRequestGeneratorFactory orderRequestGeneratorFactory;

    @Autowired
    RequestPolicyFactory requestPolicyFactory;

    @Autowired
    PaymentHandler paymentHandler;

    /**
     * Method that must be implemented and return the TokenGenerator used.
     *
     * @return the token generator used.
     */
    @Override
    protected TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    /**
     * Method that should return mode of payment flow in relation.
     *
     * @param orderRequest the related order request.
     * @return the mode that should be used for the given order request.
     * @see PaymentFlowMode
     */
    @Override
    protected PaymentFlowMode getPaymentFlowMode(OrderRequest orderRequest) {
        return PaymentFlowMode.LOCAL;
    }

    /**
     * @return the duration for the not before field in generated
     * JWT tokens. This can be positive if it should be valid in the future, or negative
     * to support skewed clocked between systems. Use null if no not before date should
     * be set in generated JWT tokens.
     */
    @Override
    protected Duration getTokenNotBeforeDuration() {
        try {
            if(paywallProperties.getJwtTokenNotBefore() != null){
               long durationInSec = checkRequiredLong(paywallProperties.getJwtTokenNotBefore(), PaywallProperties.JWT_TOKEN_NOTBEFORE);
               return Duration.ofSeconds(durationInSec);
            }
        }catch (InternalErrorException e){
            log.severe("Error parsing application properties, setting " + PaywallProperties.JWT_TOKEN_NOTBEFORE + " should be an integer value if set, not " + paywallProperties.getJwtTokenNotBefore() + ", disabling not before duration in JWT tokens.");
        }
        return null;
    }

    /**
     * @return true if settled invoices are presented before any order is created should
     * be registered as new payments automatically when register them as settled.
     */
    @Override
    protected boolean getRegisterNewInvoices() {
        try {
            return checkBooleanWithDefault(paywallProperties.getInvoiceRegisterNew(), PaywallProperties.INVOICE_REGISTER_NEW, PaywallProperties.DEFAULT_INVOICE_REGISTER_NEW);
        }catch (InternalErrorException e){
            log.severe("Error parsing application properties, setting " + PaywallProperties.INVOICE_REGISTER_NEW + " should be true or false, not " + paywallProperties.getInvoiceRegisterNew() + ", using default value: " + PaywallProperties.DEFAULT_INVOICE_REGISTER_NEW);
        }
        return PaywallProperties.DEFAULT_INVOICE_REGISTER_NEW;
    }

    /**
     * @return the PaymentHandler used.  Not all implementations
     * * need to override this method, usually on nodes containing pay-walled resources.
     */
    @Override
    protected RequestPolicyFactory getRequestPolicyFactory() {
        return requestPolicyFactory;
    }

    /**
     * @return the PaymentHandler used.  Not all implementations
     * need to override this method, usually on nodes containing generating lightning invoices.
     */
    @Override
    protected LightningHandler getLightningHandler() {
        return lightningHandler;
    }

    /**
     * @return the PaymentHandler used.  Not all implementations
     * need to override this method, usually on nodes nodes containing pay-walled resources
     * except if PaymentFlowMode.CENTRAL_PAYMENT_HANDLER then should this be implemented
     * on same node as lightning handler.
     */
    @Override
    protected PaymentHandler getPaymentHandler() {
        return paymentHandler;
    }

    /**
     * @return the CurrencyConverter used.  Not all implementations
     * need to override this method, usually on nodes containing lightning handler.
     */
    @Override
    protected CurrencyConverter getCurrencyConverter() {
        return currencyConverter;
    }

    /**
     * @return the OrderRequestGeneratorFactory used. Not all implementations
     * need to override this method, usually on nodes containing pay-walled resources.
     */
    @Override
    protected OrderRequestGeneratorFactory getOrderRequestGeneratorFactory() {
        return orderRequestGeneratorFactory;
    }
}
