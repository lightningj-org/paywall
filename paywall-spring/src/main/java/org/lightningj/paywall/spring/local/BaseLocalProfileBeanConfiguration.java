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

import org.lightningj.paywall.keymgmt.KeyManager;
import org.lightningj.paywall.keymgmt.SymmetricKeyManager;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory;
import org.lightningj.paywall.paymentflow.PaymentFlowManager;
import org.lightningj.paywall.requestpolicy.RequestPolicyFactory;
import org.lightningj.paywall.spring.CommonBeanConfiguration;
import org.lightningj.paywall.spring.PaywallProperties;
import org.lightningj.paywall.spring.SpringDefaultFileKeyManager;
import org.lightningj.paywall.spring.SpringLNDLightningHandler;
import org.lightningj.paywall.tokengenerator.SymmetricKeyTokenGenerator;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Class containing most configuration definitions except LightningHandler. Full configuration
 * profile for paywall_local profile is defined in LocalProfileBeanConfiguration
 *
 */
public abstract class BaseLocalProfileBeanConfiguration extends CommonBeanConfiguration {

    @Autowired
    PaywallProperties paywallProperties;


    @Bean({"tokenGenerator"})
    public TokenGenerator getTokenGenerator(){
        return new SymmetricKeyTokenGenerator((SymmetricKeyManager) getKeyManager());
    }

    @Bean("orderRequestGeneratorFactory")
    public OrderRequestGeneratorFactory getOrderRequestGeneratorFactory(){
        return new OrderRequestGeneratorFactory();
    }


    @Bean("requestPolicyFactory")
    public RequestPolicyFactory getRequestPolicyFactory(){
        return new RequestPolicyFactory();
    }

    @Bean("paymentFlowManager")
    public PaymentFlowManager getPaymentFlowManager(){
        return new SpringLocalPaymentFlowManager();
    }

    public abstract KeyManager getKeyManager();
}
