/*
 ************************************************************************
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

import org.jose4j.jwk.JsonWebKeySet;
import org.lightningj.paywall.InternalErrorException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

/**
 * Base interface for a KeyManager in charge of maintaining asymmetric keys.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public interface AsymmetricKeyManager extends KeyManager{

    /**
     * Returns the public key that should be used for asymmetric operations for the given context.
     *
     * @param context related context.
     * @return related public key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred parsing/generating public key.
     */
    PublicKey getPublicKey(Context context) throws UnsupportedOperationException, InternalErrorException;

    /**
     * Returns the private key that should be used for asymmetric operations for the given context.
     *
     * @param context related context.
     * @return related private key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred parsing/generating private key.
     */
    PrivateKey getPrivateKey(Context context) throws UnsupportedOperationException, InternalErrorException;

    /**
     * Method retrieve a list of trusted public keys used to verify signatures..
     *
     * @param context related context.
     * @return A map of keyId of trusted public keys.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred retrieving the public keys.
     */
    Map<String,PublicKey> getTrustedKeys(Context context) throws UnsupportedOperationException, InternalErrorException;


}
