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

import org.lightningj.paywall.InternalErrorException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * Interface for a KeyManager in charge of maintaining recipient keys.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public interface RecipientKeyManager extends KeyManager{

    /**
     * Retrieves a list of public keys that should be used included in encrypted envelopes of generated
     * tokens.
     * @param context related context.
     * @return a map of keyId to public keys that should be recipients of encrypted messages.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred retrieving the public keys.
     */
    Map<String,PublicKey> getReceipients(Context context) throws UnsupportedOperationException, InternalErrorException;

}
