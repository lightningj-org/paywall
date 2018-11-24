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
import org.jose4j.jwk.*;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager;
import org.lightningj.paywall.keymgmt.KeySerializationHelper;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lightningj.paywall.keymgmt.Context.KeyUsage.SIGN;
import static org.lightningj.paywall.tokengenerator.TokenContext.*;

/**
 * Token Manager to generate token using asymmetric key manager.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public class AsymmetricKeyTokenGenerator extends BaseTokenGenerator{

    AsymmetricKeyManager keyManager;
    RecipientRepository recipientRepository;

    protected Clock clock = Clock.systemDefaultZone();
    private static final long CACHE_TIME = 5 * 60 * 1000; // 5 Min

    long cacheExpireDate = 0;
    Map<TokenContext,JsonWebKeySet> trustedSigningPublicKeys = null;

    /**
     * Default constructor.
     * @param keyManager the KeyManager storing the keys with the generated tokens
     * @param recipientRepository an implementation of a recipient repository to lookup public key of encrypted
     *                            recipients.
     */
    public AsymmetricKeyTokenGenerator(AsymmetricKeyManager keyManager, RecipientRepository recipientRepository){
        this.keyManager = keyManager;
        this.recipientRepository = recipientRepository;
    }

    /**
     * Method to set the used RSA_USING_SHA256 algorithm and the private key to sign the data with.
     * @param context the related token context.
     * @param jws the signature to populate used algorithm and key for.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred setting the private key.
     */
    @Override
    protected void populateJWSSignatureAlgAndKey(TokenContext context, JsonWebSignature jws) throws IOException, InternalErrorException {
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKeyIdHeaderValue(KeySerializationHelper.genKeyId(keyManager.getPublicKey(context).getEncoded()));
        jws.setKey(keyManager.getPrivateKey(context));
    }

    /**
     * Populates the JWS Signature with a whitelisting of asymmetric key algorithms and the finding the related public key.
     * @param context the related token context.
     * @param jws the JsonWebSignature to populate.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal error occurred accessing the asymmetric key.
     */
    @Override
    protected void populateJWSVerifyAlgAndKey(TokenContext context, JsonWebSignature jws) throws TokenException, JoseException, IOException, InternalErrorException {
        jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                AlgorithmIdentifiers.RSA_USING_SHA256,
                AlgorithmIdentifiers.RSA_USING_SHA384,
                AlgorithmIdentifiers.RSA_USING_SHA512,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA256,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA384,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA512,
                AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
                AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384,
                AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512));
        VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
        JsonWebKey jwk = jwkSelector.select(jws, getTrustedKeysAsJWTKeys(context).getJsonWebKeys());
        if(jwk == null){
            throw new TokenException("Error verifying token signature, signature key is not trusted.");
        }
        jws.setKey(jwk.getKey());
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
        return KeySerializationHelper.genKeyId(keyManager.getPublicKey(context).getEncoded());
    }

    /**
     * Help method to retrieve the set of trusted public keys as a JsonWebKeySet.
     * @param context related context.
     * @return A list of trusted public keys as a JsonWebKeySet
     * @throws InternalErrorException if internal error occurred retriveing the public keys.
     */
    JsonWebKeySet getTrustedKeysAsJWTKeys(TokenContext context) throws  InternalErrorException{
        if(hasCacheExpired()){
            rebuildCache();
        }
        return trustedSigningPublicKeys.get(context);
    }

    /**
     * Method to set the encryption algorithm and key used to encrypt the token.
     *
     * @param context the related token context.
     * @param recipientSubject the subject name of the recipient for the generated token, could be null if not applicable.
     * @param jwe     the encryption object to populate used algorithm and key for.
     * @throws TokenException if no related encryption key could be found.
     * @throws IOException            if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWE properties.
     */
    @Override
    protected void populateJWEEncryptionAlgAndKey(TokenContext context, String recipientSubject, JsonWebEncryption jwe) throws TokenException, IOException, InternalErrorException {
        JsonWebKey jwk = recipientRepository.findRecipientKey(context,recipientSubject);
        jwe.setKey(jwk.getKey());
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA_OAEP);
        jwe.setKeyIdHeaderValue(jwk.getKeyId());
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512);
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
        jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, KeyManagementAlgorithmIdentifiers.RSA_OAEP));
        jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512));
        jwe.setKey(keyManager.getPrivateKey(context));
    }

    public synchronized void rebuildCache() throws InternalErrorException{
        // Calculate trustedSigningPublicKeys
        trustedSigningPublicKeys = new ConcurrentHashMap<>();
        // for each context.
        populateJsonWebKeySet(new TokenContext(CONTEXT_INVOICE_TOKEN_TYPE,SIGN), trustedSigningPublicKeys, keyManager.getTrustedKeys(new TokenContext(CONTEXT_INVOICE_TOKEN_TYPE,SIGN)));
        populateJsonWebKeySet(new TokenContext(CONTEXT_PAYMENT_TOKEN_TYPE,SIGN), trustedSigningPublicKeys, keyManager.getTrustedKeys(new TokenContext(CONTEXT_PAYMENT_TOKEN_TYPE,SIGN)));
        populateJsonWebKeySet(new TokenContext(CONTEXT_SETTLEMENT_TOKEN_TYPE,SIGN), trustedSigningPublicKeys, keyManager.getTrustedKeys(new TokenContext(CONTEXT_SETTLEMENT_TOKEN_TYPE,SIGN)));
        cacheExpireDate = clock.millis() + CACHE_TIME;

    }

    private void populateJsonWebKeySet(TokenContext context, Map<TokenContext,JsonWebKeySet> keyCache, Map<String,PublicKey> keys) throws InternalErrorException{
        JsonWebKeySet retval = new JsonWebKeySet();
        for(String keyId : keys.keySet()){
            PublicKey publicKey = keys.get(keyId);
            JsonWebKey jsonWebKey;
            if(publicKey instanceof RSAPublicKey){
                jsonWebKey = new RsaJsonWebKey((RSAPublicKey) publicKey);
            }else{
                if(publicKey instanceof ECPublicKey){
                    jsonWebKey = new EllipticCurveJsonWebKey((ECPublicKey) publicKey);
                }else{
                    throw new InternalErrorException("problem creating JSON Web Key set. Invalid asymmetric key type: " + publicKey.getClass().getSimpleName());
                }
            }
            jsonWebKey.setKeyId(keyId);
            retval.addJsonWebKey(jsonWebKey);
        }
        keyCache.put(context,retval);
    }

    private boolean hasCacheExpired(){
        if(trustedSigningPublicKeys == null){
            return true;
        }
        return cacheExpireDate < clock.millis();
    }

}
