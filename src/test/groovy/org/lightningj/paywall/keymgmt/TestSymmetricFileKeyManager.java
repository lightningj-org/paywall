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

/**
 * Test implementation of a SymmetricFileKeyManager
 * Created by Philip Vendil on 2018-09-19.
 */
public class TestSymmetricFileKeyManager extends SymmetricFileKeyManager {

    private String keyStorePath;
    private String protectPassphrase;

    public TestSymmetricFileKeyManager(String keyStorePath, String protectPassphrase){
        this.keyStorePath=keyStorePath;
        this.protectPassphrase = protectPassphrase;
    }

    @Override
    protected String getKeyStorePath() throws InternalErrorException {
        return keyStorePath;
    }

    @Override
    protected String getProtectPassphrase() throws InternalErrorException {
        return protectPassphrase;
    }

    public Key getSecretKey(){
        return secretKey;
    }

    public void setSecretKey(Key secretKey){
        this.secretKey=secretKey;
    }
}
