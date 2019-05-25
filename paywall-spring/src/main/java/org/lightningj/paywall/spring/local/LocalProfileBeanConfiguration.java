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
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.spring.SpringDefaultFileKeyManager;
import org.lightningj.paywall.spring.SpringLNDLightningHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Default Spring Bean Configuration for running paywall in local mode.
 * This configuration can be overrriden by specifying own Configuration and setting property 'paywall.custombeans.enable=true'
 *
 */
@Configuration
@Profile({"paywall_local"})
@Conditional(DefaultLocalBeanCondition.class)
public class LocalProfileBeanConfiguration extends BaseLocalProfileBeanConfiguration {

    @Bean("lightningHandler")
    public LightningHandler getLightningHandler(){
        return new SpringLNDLightningHandler();
    }

    @Bean("keyManager")
    public KeyManager getKeyManager(){
        return new SpringDefaultFileKeyManager();
    }

}
