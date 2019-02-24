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

import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.currencyconverter.SameCryptoCurrencyConverter;
import org.lightningj.paywall.qrcode.DefaultQRCodeGenerator;
import org.lightningj.paywall.qrcode.QRCodeGenerator;
import org.springframework.context.annotation.Bean;

/**
 * Class containing configuration for beans in common for all configuration
 * profiles.
 */
public class CommonBeanConfiguration {

    @Bean({"currencyConverter"})
    public CurrencyConverter getCurrencyConverter(){
        return new SameCryptoCurrencyConverter();
    }


    @Bean("paywallExceptionHandler")
    public PaywallExceptionHandler getPaywallExceptionHandler(){
        return new SpringPaywallExceptionHandler();
    }

    @Bean("qrCodeGenerator")
    public QRCodeGenerator getQRCodeGenerator(){
        return new DefaultQRCodeGenerator();
    }
}
