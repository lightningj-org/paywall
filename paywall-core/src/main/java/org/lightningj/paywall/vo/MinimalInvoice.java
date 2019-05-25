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
package org.lightningj.paywall.vo;

import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.paymenthandler.Payment;
import org.lightningj.paywall.tokengenerator.JWTClaim;
import org.lightningj.paywall.util.Base58;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Minimal Invoice used inside JWT tokens that is intended to have as small size as possible
 * and used in payment flows where full invoice data isn't needed to be transferred between nodes.
 *
 * Created by Philip Vendil on 2019-04-25
 */
public class MinimalInvoice extends JWTClaim implements Payment {

    public static final String CLAIM_NAME = "min_inv";

    protected byte[] preImageHash;

    /**
     * Empty Constructor
     */
    public MinimalInvoice(){}

    /**
     * Default constructor, from existing invoice
     *
     * @param invoice the invoice to create a minimal invoice from.
     */
    public MinimalInvoice(Invoice invoice) {
        this.preImageHash = invoice.getPreImageHash();
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public MinimalInvoice(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Parse from JWTClaims constructor
     *
     * @param jwtClaims the JWT Tokens Claim set to extract data from.
     */
    public MinimalInvoice(JwtClaims jwtClaims) {
        super(jwtClaims);
    }

    /**
     *
     * @return the generated preImageHash from PreImageData which acts as an unique id for the payment.
     */
    public byte[] getPreImageHash() {
        return preImageHash;
    }

    /**
     *
     * @param preImageHash the generated preImageHash from PreImageData which acts as an unique id for the payment.
     */
    public void setPreImageHash(byte[] preImageHash) {
        this.preImageHash = preImageHash;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addB58(jsonObjectBuilder,"preImageHash", Base58.encodeToString(preImageHash));
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        preImageHash = getByteArrayFromB58(jsonObject,"preImageHash",true);
    }

    @Override
    public String getClaimName() {
        return CLAIM_NAME;
    }
}
