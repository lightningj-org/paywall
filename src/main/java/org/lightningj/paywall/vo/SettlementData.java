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

import org.lightningj.paywall.JSONParsable;
import org.lightningj.paywall.util.HexUtils;
import org.lightningj.paywall.vo.amount.CryptoAmount;

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
public class SettlementData extends JSONParsable {

    protected byte[] preImageHash;
    protected boolean isSettled;
    protected CryptoAmount settledAmount;
    protected Instant validUntil;
    protected Instant settlementDate;

    /**
     * Empty Constructor
     */
    public SettlementData(){}

    /**
     * Default Constructor
     * @param preImageHash the generated preImageHash from PreImageData and used as primary key for the payment request.
     * @param isSettled if the payment was done successfully.
     * @param settledAmount the amount that was settled.
     * @param validUntil the time the payment is valid and the requested call can used.
     * @param settlementDate the time the payment was settled with the LightningHandler.
     */
    public SettlementData(byte[] preImageHash, boolean isSettled, CryptoAmount settledAmount, Instant validUntil, Instant settlementDate) {
        this.preImageHash = preImageHash;
        this.isSettled = isSettled;
        this.settledAmount = settledAmount;
        this.validUntil = validUntil;
        this.settlementDate = settlementDate;
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
     * @return if the payment was done successfully.
     */
    public boolean isSettled() {
        return isSettled;
    }

    /**
     *
     * @param settled if the payment was done successfully.
     */
    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    /**
     *
     * @return the amount that was settled.
     */
    public CryptoAmount getSettledAmount() {
        return settledAmount;
    }

    /**
     *
     * @param settledAmount the amount that was settled.
     */
    public void setSettledAmount(CryptoAmount settledAmount) {
        this.settledAmount = settledAmount;
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
     * @return the time the payment was settled with the LightningHandler.
     */
    public Instant getSettlementDate() {
        return settlementDate;
    }

    /**
     *
     * @param settlementDate the time the payment was settled with the LightningHandler.
     */
    public void setSettlementDate(Instant settlementDate) {
        this.settlementDate = settlementDate;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        add(jsonObjectBuilder,"preImageHash", HexUtils.encodeHexString(preImageHash));
        add(jsonObjectBuilder,"isSettled",isSettled);
        add(jsonObjectBuilder,"settledAmount",settledAmount);
        add(jsonObjectBuilder,"validUntil",validUntil);
        add(jsonObjectBuilder,"settlementDate",settlementDate);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        this.preImageHash = getByteArrayFromHex(jsonObject,"preImageHash",true);
        this.isSettled = getBoolean(jsonObject,"isSettled", true);
        this.settledAmount = new CryptoAmount(getJsonObject(jsonObject, "settledAmount", true));
        this.validUntil = Instant.ofEpochMilli(getLong(jsonObject,"validUntil", true));
        this.settlementDate = Instant.ofEpochMilli(getLong(jsonObject,"settlementDate", true));
    }
}
