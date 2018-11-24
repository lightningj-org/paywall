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
package org.lightningj.paywall.keymgmt;

import org.lightningj.paywall.InternalErrorException;

import java.security.Key;
import java.security.KeyPair;
import java.time.Clock;
import java.time.Duration;

/**
 * Test implementation of a SymmetricFileKeyManager
 * Created by Philip Vendil on 2018-09-19.
 */
public class TestDefaultRecipientKeyManager extends DefaultRecipientKeyManager {

    private String recipientStorePath;
    private AsymmetricKeyManager keyManager;

    public TestDefaultRecipientKeyManager(String recipientStorePath,AsymmetricKeyManager keyManager){
        this.recipientStorePath=recipientStorePath;
        this.keyManager = keyManager;
    }

    public void forwardClock(Duration duration){
        this.clock = Clock.offset(clock,duration);
    }

    /**
     * Returns the path of directory where recipients public key files are stored used to encrypt
     * messages to.
     *
     * @return the path to the directory where recipients public key files are stored used to encrypt
     * messages to. Or null if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the recipients store path.
     */
    @Override
    protected String getAsymRecipientsStorePath() throws InternalErrorException {
        return recipientStorePath;
    }

    /**
     * @return Returns the related asymmetric key manager.
     */
    @Override
    protected AsymmetricKeyManager getAsymmetricKeyManager() {
        return keyManager;
    }

    /**
     * Method to return the Security Provider to use in given context.
     *
     * @param context the related context
     * @return the provider to use.
     */
    @Override
    public String getProvider(Context context) {
        return "BC";
    }
}
