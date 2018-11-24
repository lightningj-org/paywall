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

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.keymgmt.KeySerializationHelper;
import org.lightningj.paywall.keymgmt.SymmetricKeyManager;

import java.io.IOException;

import static org.jose4j.jwa.AlgorithmConstraints.ConstraintType.*;

/**
 * Token Manager to generate token using symmetric key manager.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public class SymmetricKeyTokenGenerator extends BaseTokenGenerator{

    SymmetricKeyManager keyManager;

    public SymmetricKeyTokenGenerator(SymmetricKeyManager keyManager){
        this.keyManager = keyManager;
    }

    /**
     * Populates the JWS Signature with HMAC_SHA256 and the used symmetric key.
     * @param jws the JsonWebSignature to populate.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal error occurred accessing the symmetric key.
     */
    @Override
    protected void populateJWSSignatureAlgAndKey(TokenContext context, JsonWebSignature jws) throws IOException, InternalErrorException {
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(new HmacKey(keyManager.getSymmetricKey(context).getEncoded()));
    }

    /**
     * Populates the JWS Signature with HMAC_SHA256 whitelisting and the used symmetric key.
     * @param context the related token context.
     * @param jws the JsonWebSignature to populate.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal error occurred accessing the symmetric key.
     */
    @Override
    protected void populateJWSVerifyAlgAndKey(TokenContext context, JsonWebSignature jws) throws IOException, InternalErrorException {
        jws.setAlgorithmConstraints(new AlgorithmConstraints(WHITELIST,
                AlgorithmIdentifiers.HMAC_SHA256,
                AlgorithmIdentifiers.HMAC_SHA384,
                AlgorithmIdentifiers.HMAC_SHA512));
        jws.setKey(new HmacKey(keyManager.getSymmetricKey(context).getEncoded()));
        // No trust check is necessary since there is the same key used by signer and verifier.
    }

    /**
     * Method to set the encryption algorithm and key used to encrypt the token.
     *
     * @param context the related token context.
     * @param recipientSubject not used for symmetric token generator.
     * @param jwe     the encryption object to populate used algorithm and key for.
     * @throws IOException            if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWE properties.
     */
    @Override
    protected void populateJWEEncryptionAlgAndKey(TokenContext context, String recipientSubject, JsonWebEncryption jwe) throws IOException, InternalErrorException {
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwe.setKey(keyManager.getSymmetricKey(context));
    }

    /**
     * Method to set the encryption algorithm and key used to deencrypt the token.
     *
     * @param context the related token context.
     * @param jwe     the encryption object to populate used algorithm and key for.
     * @throws IOException            if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWE properties.
     */
    @Override
    protected void populateJWEDecryptionAlgAndKey(TokenContext context, JsonWebEncryption jwe) throws IOException, InternalErrorException {
        AlgorithmConstraints algConstraints = new AlgorithmConstraints(WHITELIST, KeyManagementAlgorithmIdentifiers.DIRECT);
        jwe.setAlgorithmConstraints(algConstraints);
        AlgorithmConstraints encConstraints = new AlgorithmConstraints(WHITELIST, ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwe.setContentEncryptionAlgorithmConstraints(encConstraints);

        jwe.setKey(keyManager.getSymmetricKey(context));
    }

    /**
     * Method to retrieve the issuer claim in generated JWT tokens.
     *
     * @param context the related token context.
     * @return should return the issuer name set in the JWT token. Usually the signers key id.
     * @throws InternalErrorException if internal problems occurred retrieving the issuer name for the given context.
     */
    @Override
    protected String getIssuerName(TokenContext context) throws InternalErrorException {
        return KeySerializationHelper.genKeyId(keyManager.getSymmetricKey(context).getEncoded());
    }
}
