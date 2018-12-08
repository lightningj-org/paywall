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

import org.jose4j.json.JsonUtil;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.keymgmt.Context;
import org.lightningj.paywall.util.DigestUtils;
import org.lightningj.paywall.vo.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;

/**
 * Default abstract base class containing help method for implementing
 * TokenGenerators.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public abstract class BaseTokenGenerator implements TokenGenerator{

    public static final int PREIMAGE_LENGTH = 32;

    public static final long ALLOWED_CLOCK_SKEW =  5 * 60 * 1000; // 5 minutes.

    SecureRandom secureRandom = null;
    Clock clock = Clock.systemDefaultZone();
    /**
     * Method that should generate a random pre image data used to
     * create invoice.
     *
     * @return a newly created unique PreImageData
     * @throws InternalErrorException if internal errors occurred generating
     *                                the pre image data.
     */
    @Override
    public PreImageData genPreImageData() throws InternalErrorException {
        byte[] preImage = new byte[PREIMAGE_LENGTH];
        getSecureRandom().nextBytes(preImage);
        byte[] preImageHash = DigestUtils.sha256(preImage);
        return new PreImageData(preImage,preImageHash);
    }

    /**
     * Help method to generate a JWT token containing a payment data claim.
     *
     * @param orderData the payment data to include in the token.
     * @param requestData optional request data that could be set if workflow requires it.
     * @param expireDate the expire date of the token in the future.
     * @param notBefore an optional not before data, indicating when the token starts to become valid. Use null not to set.
     * @param recipientSubject the subject (usually keyId) of the recipient, required if asymmetrical keys are used, otherwise it
     *                        can be null.
     * @return the generate JWT/JWS/JWE token with a payment data claim.
     * @throws TokenException if problems occurred looking up the recipient public key.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred processing the token.
     */
    public String generatePaymentToken(OrderData orderData, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException{
        return generateToken(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE,expireDate, notBefore, true, recipientSubject, orderData, requestData);
    }

    /**
     * Help method to generate a JWT token containing a invoice data claim.
     *
     * @param invoiceData the invoice data to include in the token.
     * @param requestData optional request data that could be set if workflow requires it.
     * @param expireDate the expire date of the token in the future.
     * @param notBefore an optional not before data, indicating when the token starts to become valid. Use null not to set.
     * @param recipientSubject the subject (usually keyId) of the recipient, required if asymmetrical keys are used, otherwise it
     *                        can be null.
     * @return the generate JWT/JWS/JWE token with a invoice data claim.
     * @throws TokenException if problems occurred looking up the recipient public key.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred processing the token.
     */
    public String generateInvoiceToken(InvoiceData invoiceData, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException{
        return generateToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,expireDate, notBefore, true, recipientSubject, invoiceData, requestData);
    }

    /**
     * Help method to generate a JWT token containing a settlement data claim.
     *
     * @param settlementData the settlement data to include in the token.
     * @param requestData optional request data that could be set if workflow requires it.
     * @param expireDate the expire date of the token in the future.
     * @param notBefore an optional not before data, indicating when the token starts to become valid. Use null not to set.
     * @param recipientSubject the subject (usually keyId) of the recipient, required if asymmetrical keys are used, otherwise it
     *                        can be null.
     * @return the generate JWT/JWS/JWE token with a settlement data claim.
     * @throws TokenException if problems occurred looking up the recipient public key.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred processing the token.
     */
    public String generateSettlementToken(SettlementData settlementData, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException{
        return generateToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,expireDate, notBefore, true, recipientSubject, settlementData, requestData);
    }


    // TODO
    // Write tests for base token.

    /**
     * General method to generate JWT token that is JWS signed and optionally JWE encrypted.
     *
     * @param tokenContextType the type the token context
     * @param expireDate the expire date of the token in the future.
     * @param notBefore an optional not before data, indicating when the token starts to become valid. Use null not to set.
     * @param encrypt true if JWE encrypted token should be generated.
     * @param recipientSubject the subject (usually keyId) of the recipient, required if asymmetrical keys are used, otherwise it
     *                        can be null.
     * @param claim a list of claims to include in the token.
     * @return the generate JWT/JWS/JWE token.
     * @throws TokenException if problems occurred looking up the recipient public key.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred processing the token.
     */
    public String generateToken(String tokenContextType,Instant expireDate, Instant notBefore, boolean encrypt, String recipientSubject, JWTClaim ... claim) throws TokenException, IOException, InternalErrorException{
        try {
            TokenContext signContext = new TokenContext(tokenContextType, Context.KeyUsage.SIGN);
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setIssuer(getIssuerName(signContext));
            if(recipientSubject != null){
                jwtClaims.setSubject(recipientSubject);
            }
            jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(expireDate.toEpochMilli()));
            if(notBefore != null) {
                jwtClaims.setNotBefore(NumericDate.fromMilliseconds(notBefore.toEpochMilli()));
            }
            for (JWTClaim c : claim) {
                if(c != null) {
                    jwtClaims.setClaim(c.getClaimName(), JsonUtil.parseJson(c.toJsonAsString(false)));
                }
            }
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(jwtClaims.toJson());
            populateJWSSignatureAlgAndKey(signContext,jws);

            String token = jws.getCompactSerialization();
            if(encrypt){
                JsonWebEncryption jsonWebEncryption = new JsonWebEncryption();
                jsonWebEncryption.setPlaintext(token);
                populateJWEEncryptionAlgAndKey(new TokenContext(tokenContextType, Context.KeyUsage.ENC),recipientSubject,jsonWebEncryption);
                token = jsonWebEncryption.getCompactSerialization();
            }

            return token;
        }catch(JoseException e){
            throw new InternalErrorException("Internal error generate JWT token: " + e.getMessage(),e);
        }
    }

    /**
     * Method to parse, verify signature and decrypt (if encrypted) a token.
     *
     * @param tokenContextType the type of token context used when parsing a token.
     * @param jwtToken The JWT token string data to parse.
     * @return a JwtClaims from the JWT token.
     * @throws TokenException if problems occurred parsing, verifying or decrypting the token.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problems occurred processing the token.
     */
    public JwtClaims parseToken(String tokenContextType, String jwtToken) throws TokenException, IOException, InternalErrorException{
        if(jwtToken == null){
            throw new TokenException("Couldn't verify null JWT token.");
        }
        try {
            if(isEncryptedToken(jwtToken)){
                jwtToken = decryptTokenData(new TokenContext(tokenContextType, Context.KeyUsage.ENC),jwtToken);
            }
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwtToken);
            populateJWSVerifyAlgAndKey(new TokenContext(tokenContextType, Context.KeyUsage.SIGN),jws);
            if(!jws.verifySignature()){
                throw new TokenException("Invalid signature for token.");
            }
            JwtClaims jwtClaims = JwtClaims.parse(jws.getPayload());
            checkExpireDate(jwtClaims);
            checkNotBefore(jwtClaims);
            return jwtClaims;
        }catch(JoseException e){
            throw new TokenException("Invalid token received when parsing JWS Signature: " + e.getMessage(),e);
        }catch(InvalidJwtException e){
            throw new TokenException("Invalid token received when parsing JWS Claims: " + e.getMessage(),e);
        }
    }

    /**
     * Help method used to check if a signed JWT token have expired.
     * @param jwtClaims the JWT claims set.
     * @throws TokenException it no expire date found or has expired.
     */
    void checkExpireDate(JwtClaims jwtClaims) throws TokenException {
        try {
            NumericDate numericDate = jwtClaims.getExpirationTime();
            if(numericDate == null){
                throw new TokenException("Couldn't verify token, couldn't retrieve expire date from JWT claims.");
            }
            if(clock.millis() > (numericDate.getValueInMillis() + ALLOWED_CLOCK_SKEW)){
                throw new TokenException("JWT Token have expired.");
            }
        } catch (MalformedClaimException e) {
            throw new TokenException("Couldn't verify token, couldn't retrieve expire date from JWT claims.");
        }
    }

    /**
     * Help method used to check if a signed JWT token is valid yet.
     * @param jwtClaims the JWT claims set.
     * @throws TokenException it no not before date found or not yet valid.
     */
    void checkNotBefore(JwtClaims jwtClaims) throws TokenException {
        try {
            NumericDate numericDate = jwtClaims.getNotBefore();
            if(numericDate != null) {
                if (clock.millis() < (numericDate.getValueInMillis() - ALLOWED_CLOCK_SKEW)) {
                    throw new TokenException("JWT Token not yet valid.");
                }
            }
        } catch (MalformedClaimException e) {
            throw new TokenException("Couldn't verify token, couldn't retrieve not before date from JWT claims.");
        }
    }

    /**
     * Method to set the signature algorithm and key used to sign the token.
     *
     * @param context the related token context.
     * @param jws the signature to populate used algorithm and key for.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWS properties.
     */
    protected abstract void populateJWSSignatureAlgAndKey(TokenContext context, JsonWebSignature jws) throws IOException, InternalErrorException;

    /**
     * Method to set algorithm whitelisting and verification key before verification of the JWS signature.
     *
     * Important, the implementation must check that the verification key is trusted.
     *
     * @param context the related token context.
     * @param jws the signature to check signing algorithm and signing key for.
     * @throws TokenException if no trusted signing key could be found.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWS properties.
     */
    protected abstract void populateJWSVerifyAlgAndKey(TokenContext context, JsonWebSignature jws) throws TokenException, JoseException, IOException, InternalErrorException;


    /**
     * Method to set the encryption algorithm and key used to encrypt the token.
     *
     * @param context the related token context.
     * @param recipientSubject the subject name of the recipient for the generated token, could be null if not applicable.
     * @param jwe the encryption object to populate used algorithm and key for.
     * @throws TokenException if no related encryption key could be found.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWE properties.
     */
    protected abstract void populateJWEEncryptionAlgAndKey(TokenContext context, String recipientSubject, JsonWebEncryption jwe) throws TokenException,IOException, InternalErrorException;

    /**
     * Method to set the encryption algorithm and key used to deencrypt the token.
     *
     * @param context the related token context.
     * @param jwe the encryption object to populate used algorithm and key for.
     * @throws IOException if communication problems occurred with underlying systems.
     * @throws InternalErrorException if internal problem occurred setting the JWE properties.
     */
    protected abstract void populateJWEDecryptionAlgAndKey(TokenContext context, JsonWebEncryption jwe) throws IOException, InternalErrorException;

    /**
     * Method to retrieve the issuer claim in generated JWT tokens.
     *
     * @param context the related token context.
     * @return should return the issuer name set in the JWT token. Usually the signers key id.
     * @throws InternalErrorException if internal problems occurred retrieving the issuer name for the given context.
     */
    protected abstract String getIssuerName(TokenContext context) throws InternalErrorException;

    protected SecureRandom getSecureRandom() throws InternalErrorException{
        if(secureRandom == null) {
            try {
                secureRandom = new SecureRandom();
            } catch (Exception e) {
                throw new InternalErrorException("Internal error generating SecureRandom for TokenGenerator, message: " + e.getMessage(), e);
            }
        }
        return secureRandom;
    }

    private boolean isEncryptedToken(String jwtToken){
        return jwtToken.split("\\.").length == 5;
    }

    /**
     * Help method to decrypt JWE Token data.
     */
    private String decryptTokenData(TokenContext context, String encryptedJWEToken) throws TokenException, IOException, InternalErrorException{
        try {
            JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setCompactSerialization(encryptedJWEToken);
            populateJWEDecryptionAlgAndKey(context, jwe);
            return jwe.getPlaintextString();
        }catch(Exception e){
            if(e instanceof IOException){
                throw (IOException) e;
            }
            if(e instanceof InternalErrorException){
                throw (InternalErrorException) e;
            }
            throw new TokenException("Unable to decrypt token: " + e.getMessage(),e);
        }
    }
}
