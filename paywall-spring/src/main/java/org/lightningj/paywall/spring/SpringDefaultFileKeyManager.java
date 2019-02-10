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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.keymgmt.DefaultFileKeyManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Security;

import static org.lightningj.paywall.spring.PaywallProperties.*;
import static org.lightningj.paywall.util.SettingUtils.checkRequiredString;

/**
 * Spring implementation of Default File KeyManager.
 */
public class SpringDefaultFileKeyManager extends DefaultFileKeyManager {

    static {
        if(Security.getProvider("BC") == null){
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Autowired
    PaywallProperties paywallProperties;

    /**
     * Returns the path of directory where trusted public key files are stored.
     *
     * @return the path to the directory where trusted public key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the trust store path.
     */
    @Override
    protected String getAsymTrustStorePath() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getKeymgrAsymTruststorePath(),KEYMGR_ASYMTRUSTSTOREPATH);
    }

    /**
     * Returns the path of directory where key files are stored.
     *
     * @return the path to the directory where key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the key store path.
     */
    @Override
    protected String getKeyStorePath() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getKeymgrKeystorePath(),KEYMGR_KEYSTOREPATH);
    }

    /**
     * Method to retrieve the configured pass phrase used to protect generated keys.
     *
     * @return the configured protect pass phrase or null if no passphrase is configured.
     * @throws InternalErrorException if internal error occurred retrieving configuration.
     */
    @Override
    protected String getProtectPassphrase() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getKeymgrPassword(),KEYMGR_PASSWORD);
    }
}
