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
package org.lightningj.paywall.tokengenerator;

import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.keymgmt.RecipientKeyManager;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Implementation that looks up recipient keys stored in local given where it is assumed that the keyId is used as subject.
 *
 * Created by Philip Vendil on 2018-11-21.
 */
public class KeyIdByFileRecipientRepository implements RecipientRepository {

    private RecipientKeyManager recipientKeyManager;

    public KeyIdByFileRecipientRepository(RecipientKeyManager recipientKeyManager){
        this.recipientKeyManager = recipientKeyManager;
    }

    /**
     * Method to fetch a public key for a given recipients subject.
     *
     * @param subject the subject of the recipient.
     * @return the key of the recipient used to encrypt data transferred to it.
     * @throws TokenException         if subject's key couldn't be found.
     * @throws IOException            if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred retrieving the public key.
     */
    @Override
    public JsonWebKey findRecipientKey(TokenContext context, String subject) throws TokenException, IOException, InternalErrorException {
        if(subject == null){
            throw new TokenException("Error finding recipient key for subject null.");
        }
        PublicKey key = recipientKeyManager.getReceipients(context).get(subject);
        if(key == null){
            throw new TokenException("Error couldn't find any recipient key to encrypt token to having key id (subject): " + subject);
        }
        if(key instanceof RSAPublicKey){
            JsonWebKey jwk = new RsaJsonWebKey((RSAPublicKey) key);
            jwk.setKeyId(subject);
            return jwk;
        }
        if(key instanceof ECPublicKey){
            JsonWebKey jwk = new EllipticCurveJsonWebKey((ECPublicKey) key);
            jwk.setKeyId(subject);
            return jwk;
        }
        throw new TokenException("Error finding public key to encrypt token with key id " + subject + ", unsupport key type: " + key.getClass().getSimpleName());
    }
}
