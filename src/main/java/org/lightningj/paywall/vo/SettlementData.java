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
import org.lightningj.paywall.JSONParsable;
import org.lightningj.paywall.tokengenerator.JWTClaim;
import org.lightningj.paywall.util.Base64Utils;
import org.lightningj.paywall.util.HexUtils;
import org.lightningj.paywall.vo.amount.CryptoAmount;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.Instant;
import java.util.Base64;

/**
 * Value object class containing information about a payment that have been
 * settled through the lightning network.
 *
 * Created by Philip Vendil on 2018-11-12.
 */
public class SettlementData extends JWTClaim {

    public static final String CLAIM_NAME = "settlement";

    protected byte[] preImageHash;
    protected InvoiceData invoice;
    protected Instant validUntil;
    protected Instant validFrom;

    /**
     * Empty Constructor
     */
    public SettlementData(){}

    /**
     * Default Constructor
     * @param preImageHash the generated preImageHash from PreImageData and used as primary key for the payment request.
     * @param invoice the related invoice. (Optional)
     * @param validUntil the time the payment is valid and the requested call can used.
     * @param validFrom the time the payment is valid from. (Optional)
     */
    public SettlementData(byte[] preImageHash, InvoiceData invoice, Instant validUntil, Instant validFrom) {
        this.preImageHash = preImageHash;
        this.invoice = invoice;
        this.validUntil = validUntil;
        this.validFrom = validFrom;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public SettlementData(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Parse from JWTClaims constructor
     *
     * @param jwtClaims the JWT Tokens Claim set to extract data from.
     */
    public SettlementData(JwtClaims jwtClaims) {
        super(jwtClaims);
    }

    /**
     *
     * @return the generated preImageHash from PreImageData and used as primary key for the payment request.
     */
    public byte[] getPreImageHash() {
        return preImageHash;
    }

    /**
     *
     * @param preImageHash the generated preImageHash from PreImageData and used as primary key for the payment request.
     */
    public void setPreImageHash(byte[] preImageHash) {
        this.preImageHash = preImageHash;
    }

    /**
     *
     * @return the related invoice. (Optional)
     */
    public InvoiceData getInvoice() {
        return invoice;
    }

    /**
     *
     * @param invoice the related invoice. (Optional)
     */
    public void setInvoice(InvoiceData invoice) {
        this.invoice = invoice;
    }

    /**
     *
     * @return the time the payment is valid and the requested call can used.
     */
    public Instant getValidUntil() {
        return validUntil;
    }

    /**
     *
     * @param validUntil the time the payment is valid and the requested call can used.
     */
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    /**
     *
     * @return the time the payment is valid from. (Optional)
     */
    public Instant getValidFrom() {
        return validFrom;
    }

    /**
     *
     * @param validFrom the time the payment is valid from. (Optional)
     */
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Method that minimizes the amount of data (by removing the related invoice) if
     * the settlement data is only needed to verify if a given preImageHash have been
     * settled or not.
     */
    public void minimizeData(){
        this.invoice = null;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        add(jsonObjectBuilder,"preImageHash", Base64Utils.encodeBase64String(preImageHash));
        addNotRequired(jsonObjectBuilder,"invoice", invoice);
        add(jsonObjectBuilder,"validUntil",validUntil);
        addNotRequired(jsonObjectBuilder,"validFrom",validFrom);
    }

    //
    // Fix test, getter and setter
    //
    // test mimimize data method, (removes invoice)
    // test invoice
    // Add converter methods to LNDHelper

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        this.preImageHash = getByteArrayFromB64(jsonObject,"preImageHash",true);
        if(jsonObject.containsKey("invoice") && !jsonObject.isNull("invoice")) {
            this.invoice = new InvoiceData(getJsonObject(jsonObject, "invoice", true));
        }
        this.validUntil = Instant.ofEpochMilli(getLong(jsonObject,"validUntil", true));
        if(jsonObject.containsKey("validFrom") && !jsonObject.isNull("validFrom")) {
            validFrom = Instant.ofEpochMilli(getLong(jsonObject,"validFrom", true));
        }
    }

    @Override
    public String getClaimName() {
        return CLAIM_NAME;
    }
}
