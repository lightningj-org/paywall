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
import org.lightningj.paywall.util.Base64Utils;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.Instant;

/**
 * Value object class containing information about a payment that have been
 * settled through the lightning network.
 *
 * Created by Philip Vendil on 2018-11-12.
 */
public class Settlement extends JWTClaim  implements Payment {

    public static final String CLAIM_NAME = "settlement";

    protected byte[] preImageHash;
    protected Invoice invoice;
    protected Instant validUntil;
    protected Instant validFrom;
    protected boolean payPerRequest;

    /**
     * Empty Constructor
     */
    public Settlement(){}

    /**
     * Default Constructor
     * @param preImageHash the generated preImageHash from PreImageData and used as primary key for the payment request.
     * @param invoice the related invoice. (Optional)
     * @param validUntil the time the payment is valid and the requested call can used.
     * @param validFrom the time the payment is valid from. (Optional)
     * @param payPerRequest if the settlement is one time only, i.e only valid for one request.
     */
    public Settlement(byte[] preImageHash, Invoice invoice, Instant validUntil, Instant validFrom, boolean payPerRequest) {
        this.preImageHash = preImageHash;
        this.invoice = invoice;
        this.validUntil = validUntil;
        this.validFrom = validFrom;
        this.payPerRequest = payPerRequest;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public Settlement(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Parse from JWTClaims constructor
     *
     * @param jwtClaims the JWT Tokens Claim set to extract data from.
     */
    public Settlement(JwtClaims jwtClaims) {
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
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     *
     * @param invoice the related invoice. (Optional)
     */
    public void setInvoice(Invoice invoice) {
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
     *
     * @return if the settlement is one time only, i.e only valid for one request.
     */
    public boolean isPayPerRequest() {
        return payPerRequest;
    }

    /**
     *
     * @param payPerRequest if the settlement is one time only, i.e only valid for one request.
     */
    public void setPayPerRequest(boolean payPerRequest) {
        this.payPerRequest = payPerRequest;
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
        add(jsonObjectBuilder,"payPerRequest",payPerRequest);
    }

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
            this.invoice = new Invoice(getJsonObject(jsonObject, "invoice", true));
        }
        this.validUntil = Instant.ofEpochMilli(getLong(jsonObject,"validUntil", true));
        if(jsonObject.containsKey("validFrom") && !jsonObject.isNull("validFrom")) {
            validFrom = Instant.ofEpochMilli(getLong(jsonObject,"validFrom", true));
        }
        payPerRequest = getBoolean(jsonObject,"payPerRequest",true);
    }

    @Override
    public String getClaimName() {
        return CLAIM_NAME;
    }
}
