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

import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.vo.*;

import java.io.IOException;
import java.time.Instant;

/**
 * Interface for Token Generator in charge of generating
 * different types of Tokens throughout the systems such
 * as pre image data, JSON Web Tokens etc.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public interface TokenGenerator {

    /**
     * Method that should generate a random pre image data used to
     * create invoice.
     * @return a newly created unique PreImageData
     * @throws InternalErrorException if internal errors occurred generating
     * the pre image data.
     */
    PreImageData genPreImageData() throws InternalErrorException;


    /**
     * Help method to generate a JWT token containing a payment data claim.
     *
     * @param orderRequest the order request derived from the payment required annotation.
     * @param order the payment data to include in the token.
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
    String generatePaymentToken(OrderRequest orderRequest, Order order, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException;

    /**
     * Help method to generate a JWT token containing a invoice data claim.
     *
     * @param orderRequest the order request derived from the payment required annotation.
     * @param invoice the invoice data to include in the token.
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
    String generateInvoiceToken(OrderRequest orderRequest, Invoice invoice, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException;

    /**
     * Help method to generate a JWT token containing a settlement data claim.
     *
     * @param orderRequest the order request derived from the payment required annotation.
     * @param settlement the settlement data to include in the token.
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
    String generateSettlementToken(OrderRequest orderRequest, Settlement settlement, RequestData requestData, Instant expireDate, Instant notBefore, String recipientSubject) throws TokenException, IOException, InternalErrorException;

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
    String  generateToken(String tokenContextType,Instant expireDate, Instant notBefore, boolean encrypt, String recipientSubject, JWTClaim ... claim) throws TokenException, IOException, InternalErrorException;

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
    JwtClaims parseToken(String tokenContextType, String jwtToken) throws TokenException, IOException, InternalErrorException;

    /**
     * Method to retrieve the issuer claim in generated JWT tokens.
     *
     * @param tokenContextType the related token context.
     * @return should return the issuer name set in the JWT token. Usually the signers key id.
     * @throws InternalErrorException if internal problems occurred retrieving the issuer name for the given context.
     */
    String getIssuerName(String tokenContextType) throws InternalErrorException;
}
