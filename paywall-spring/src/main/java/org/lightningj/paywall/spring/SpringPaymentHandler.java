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
package org.lightningj.paywall.spring;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.paymenthandler.BasePaymentHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.logging.Logger;

import static org.lightningj.paywall.util.SettingUtils.checkLongWithDefault;

/**
 * A Spring bean enhanced base Payment Handler the implementing application
 * should inherit.
 *
 * @author philip 2019-02-09
 */
public abstract class SpringPaymentHandler extends BasePaymentHandler {

    static Logger log = Logger.getLogger(SpringPaymentHandler.class.getName());

    @Autowired
    LightningHandler lightningHandler;

    @Autowired
    PaywallProperties paywallProperties;

    @PostConstruct
    @Override
    public void init() throws InternalErrorException {
        super.init();
    }

    /**
     * Method that should returned the used LightningHandler.
     *
     * @return the related LightningHandler.
     */
    @Override
    protected LightningHandler getLightningHandler() {
        return lightningHandler;
    }

    /**
     * @return the default validity for generated invoices if no expire date have
     * been set explicit in PaymentData.
     */
    @Override
    protected Duration getDefaultInvoiceValidity() {
        try {
            long durationInSec = checkLongWithDefault(paywallProperties.getInvoiceDefaultValidity(), PaywallProperties.INVOICE_DEFAULT_VALIDITY, PaywallProperties.DEFAULT_INVOICE_DEFAULT_VALIDITY);
            return Duration.ofSeconds(durationInSec);
        }catch (InternalErrorException e){
            log.severe("Error parsing application properties, setting " + PaywallProperties.INVOICE_DEFAULT_VALIDITY + " should be an integer, not " + paywallProperties.getInvoiceDefaultValidity() + ", using default value: " + PaywallProperties.DEFAULT_INVOICE_DEFAULT_VALIDITY);
        }
        return Duration.ofSeconds(PaywallProperties.DEFAULT_INVOICE_DEFAULT_VALIDITY);
    }

    /**
     * @return the default validity for generated settlements if no valid until date have
     * been set explicit in PaymentData.
     */
    @Override
    protected Duration getDefaultSettlementValidity() {
        try {
            long durationInSec = checkLongWithDefault(paywallProperties.getSettlementDefaultValidity(), PaywallProperties.SETTLEMENT_DEFAULT_VALIDITY, PaywallProperties.DEFAULT_SETTLEMENT_DEFAULT_VALIDITY);
            return Duration.ofSeconds(durationInSec);
        }catch (InternalErrorException e){
            log.severe("Error parsing application properties, setting " + PaywallProperties.SETTLEMENT_DEFAULT_VALIDITY + " should be an integer, not " + paywallProperties.getSettlementDefaultValidity() + ", using default value: " + PaywallProperties.DEFAULT_SETTLEMENT_DEFAULT_VALIDITY);
        }
        return Duration.ofSeconds(PaywallProperties.DEFAULT_SETTLEMENT_DEFAULT_VALIDITY);
    }

}
