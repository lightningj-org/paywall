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
import org.lightningj.paywall.tokengenerator.JWTClaim;
import org.lightningj.paywall.util.Base58;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

/**
 * Value object class containing information about a request that might require payment.
 * Contains significant request data, which is a hash of the parts of a request that makes it uniquely
 * identifiable.
 *
 * Created by Philip Vendil on 2018-11-23.
 */
public class RequestData extends JWTClaim {

    protected Clock clock = Clock.systemDefaultZone();
    public static final String CLAIM_NAME = "request";

    protected byte[] significantData;
    protected Instant requestDate;

    /**
     * Empty Constructor
     */
    public RequestData(){}

    /**
     * Default Constructor
     * @param significantData significant request data, which is a hash of the parts of a request that makes it uniquely
     * identifiable.
     * @param requestDate the date the request was generated.
     */
    public RequestData(byte[] significantData, Instant requestDate) {
        this.significantData = significantData;
        this.requestDate = requestDate;
    }

    /**
     * Constructor setting request data to current instant.
     * @param significantData significant request data, which is a hash of the parts of a request that makes it uniquely
     * identifiable.
     */
    public RequestData(byte[] significantData) {
        this.significantData = significantData;
        this.requestDate = clock.instant();
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public RequestData(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Parse from JWTClaims constructor
     *
     * @param jwtClaims the JWT Tokens Claim set to extract data from.
     */
    public RequestData(JwtClaims jwtClaims) {
        super(jwtClaims);
    }

    /**
     *
     * @return significant request data, which is a hash of the parts of a request that makes it uniquely
     */
    public byte[] getSignificantData() {
        return significantData;
    }

    /**
     *
     * @param significantData significant request data, which is a hash of the parts of a request that makes it uniquely
     */
    public void setSignificantData(byte[] significantData) {
        this.significantData = significantData;
    }

    /**
     *
     * @return the date the request was generated.
     */
    public Instant getRequestDate() {
        return requestDate;
    }

    /**
     *
     * @param requestDate the date the request was generated.
     */
    public void setRequestDate(Instant requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addB58(jsonObjectBuilder,"significantData", Base58.encodeToString(significantData));
        add(jsonObjectBuilder,"requestDate",requestDate);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        this.significantData = getByteArrayFromB58(jsonObject,"significantData",true);
        this.requestDate = Instant.ofEpochMilli(getLong(jsonObject,"requestDate", true));
    }

    @Override
    public String getClaimName() {
        return CLAIM_NAME;
    }

    /**
     * Equals checks significantData only.
     * @param o other object
     * @return true if significantData is equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestData that = (RequestData) o;

        return Arrays.equals(significantData, that.significantData);
    }

    /**
     * @return hashcode of significantData only.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(significantData);
    }
}
