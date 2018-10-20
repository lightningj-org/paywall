/*
 ************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
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
public class TestDefaultFileKeyManager extends DefaultFileKeyManager {

    private String keyStorePath;
    private String protectPassphrase;
    private String trustStorePath;

    public TestDefaultFileKeyManager(String keyStorePath, String trustStorePath, String protectPassphrase){
        this.keyStorePath=keyStorePath;
        this.protectPassphrase = protectPassphrase;
        this.trustStorePath = trustStorePath;
    }

    @Override
    protected String getKeyStorePath() throws InternalErrorException {
        return keyStorePath;
    }

    @Override
    protected String getProtectPassphrase() throws InternalErrorException {
        return protectPassphrase;
    }

    @Override
    protected String getAsymTrustStorePath() throws InternalErrorException {
        return trustStorePath;
    }

    public KeyPair getKeyPairField(){
        return asymKeyPair;
    }

    public void setKeyPairField(KeyPair keyPair){
        this.asymKeyPair =keyPair;
    }

    public void forwardClock(Duration duration){
        this.clock = Clock.offset(clock,duration);
    }

    public Key getSecretKey(){
        return secretKey;
    }

    public void setSecretKey(Key secretKey){
        this.secretKey=secretKey;
    }
}
