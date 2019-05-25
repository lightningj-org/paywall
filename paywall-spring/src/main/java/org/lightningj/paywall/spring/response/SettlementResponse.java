/*
 * ***********************************************************************
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
package org.lightningj.paywall.spring.response;

import org.lightningj.paywall.JSONParsable;
import org.lightningj.paywall.paymentflow.SettlementResult;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.Settlement;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * Value object used to return the current status of a settlement from
 * the CheckSettlementController
 *
 * @author philip 2019-02-13
 */
@XmlRootElement(name = "SettlementResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SettlementResponseType", propOrder = {
        "preImageHash",
        "token",
        "settlementValidUntil",
        "settlementValidFrom",
        "payPerRequest",
        "settled"
})
public class SettlementResponse extends Response {

    public static final String TYPE = "settlement";

    @XmlTransient
    private String type=TYPE;

    @XmlElement()
    private String preImageHash;
    @XmlElement()
    private String token;
    @XmlElement()
    private Date settlementValidUntil;
    @XmlElement()
    private Date settlementValidFrom;
    @XmlElement()
    private Boolean payPerRequest;
    @XmlElement(required = true)
    private boolean settled;

    /**
     * Empty Constructor
     */
    public SettlementResponse(){
        settled = false;
    }

    /**
     * Constructor for settled invoices, of settlement is null is
     * only settled field set to false.
     * @param settlementResult A settlement result object, null if not settled.
     */
    public SettlementResponse(SettlementResult settlementResult){
        if(settlementResult != null && settlementResult.getSettlement() != null){
            Settlement settlement = settlementResult.getSettlement();
            preImageHash = Base58.encodeToString(settlement.getPreImageHash());
            token = settlementResult.getToken();
            if(settlement.getValidUntil() != null) {
                settlementValidUntil = new Date(settlement.getValidUntil().toEpochMilli());
            }
            if(settlement.getValidFrom() != null) {
                settlementValidFrom = new Date(settlement.getValidFrom().toEpochMilli());
            }
            payPerRequest = settlement.isPayPerRequest();
            settled = true;
        }
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public SettlementResponse(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return related pre image hash hex encoded
     */
    public String getPreImageHash() {
        return preImageHash;
    }

    /**
     *
     * @param preImageHash related pre image hash hex encoded
     */
    public void setPreImageHash(String preImageHash) {
        this.preImageHash = preImageHash;
    }

    /**
     *
     * @return generated settlement JWT Token
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token generated settlement JWT Token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return how long the JWT Token is valid
     */
    public Date getSettlementValidUntil() {
        return settlementValidUntil;
    }

    /**
     *
     * @param settlementValidUntil how long the JWT Token is valid
     */
    public void setSettlementValidUntil(Date settlementValidUntil) {
        this.settlementValidUntil = settlementValidUntil;
    }

    /**
     *
     * @return the valid from date of the JWT Token, null if no valid from date exists.
     */
    public Date getSettlementValidFrom() {
        return settlementValidFrom;
    }

    /**
     *
     * @param settlementValidFrom the valid from date of the JWT Token, null if no valid from date exists.
     */
    public void setSettlementValidFrom(Date settlementValidFrom) {
        this.settlementValidFrom= settlementValidFrom;
    }

    /**
     *
     * @return true if related payment is for one request only.
     */
    public Boolean getPayPerRequest() {
        return payPerRequest;
    }

    /**
     *
     * @param payPerRequest if related payment is for one request only.
     */
    public void setPayPerRequest(Boolean payPerRequest) {
        this.payPerRequest = payPerRequest;
    }

    /**
     *
     * @return true if payment is settled and settlement is generated.
     */
    public boolean isSettled() {
        return settled;
    }

    /**
     *
     * @param settled true if payment is settled and settlement is generated.
     */
    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    /**
     *
     * @return the type of response returned in JSON responses only.
     */
    public String getType(){
        return type;
    }

    /**
     *
     * @param type the type of response returned in JSON responses only.
     */
    public void setType(String type){
        this.type= type;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        super.convertToJson(jsonObjectBuilder);
        addNotRequired(jsonObjectBuilder,"preImageHash",preImageHash);
        addNotRequired(jsonObjectBuilder,"token",token);
        addNotRequired(jsonObjectBuilder,"settlementValidUntil",settlementValidUntil);
        addNotRequired(jsonObjectBuilder,"settlementValidFrom",settlementValidFrom);
        addNotRequired(jsonObjectBuilder,"payPerRequest",payPerRequest);
        add(jsonObjectBuilder,"settled",settled);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        super.parseJson(jsonObject);
        preImageHash = getStringIfSet(jsonObject,"preImageHash");
        token = getStringIfSet(jsonObject,"token");
        settlementValidUntil = getDateIfSet(jsonObject,"settlementValidUntil");
        settlementValidFrom =  getDateIfSet(jsonObject,"settlementValidFrom");
        payPerRequest = getBooleanIfSet(jsonObject,"payPerRequest");
        settled = getBoolean(jsonObject,"settled",true);
    }

}
