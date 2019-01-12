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
package org.lightningj.paywall.btcpayserver.vo;

import org.lightningj.paywall.JSONParsable;
import org.lightningj.paywall.btcpayserver.BTCPayServerFacade;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.time.temporal.TemporalField;

/**
 * Value object class representing one serializable Token. Has the following
 * values:
 *
 * token : the server generated token value (Required)
 * expireDate : the date the token expires (Optional)
 * facade : the facade of the token, merchant, pos ...
 *
 * Created by Philip Vendil on 2018-10-13.
 */
public class Token extends JSONParsable {

    String id;
    String token;
    Instant expireDate;
    BTCPayServerFacade facade;
    String pairingCode;
    String label;

    /**
     * Default constructor for a BTC Pay Server Token
     *
     * @param id the SIN value of the public key
     * @param token the token value
     * @param expireDate the expire instant of this token.
     * @param facade the facade this token is valid for.
     */
    public Token(String id, String token, Instant expireDate, BTCPayServerFacade facade) {
        this.id = id;
        this.token = token;
        this.expireDate = expireDate;
        this.facade = facade;
    }

    /**
     * Constructor when creating a Token for fetching from BTC Pay SErver
     *
     * @param id the SIN value of the public key
     * @param label name of this requesting application.
     * @param facade the facade this token is valid for.
     */
    public Token(String id, String label, BTCPayServerFacade facade) {
        this.id = id;
        this.label = label;
        this.facade = facade;
    }

    public Token(JsonObject jsonObject){
        super(jsonObject);
    }

    /**
     * Empty Constructor
     */
    public Token(){}

    /**
     * Gets the BTC Pay Server token.
     * @return the BTC Pay Server token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the BTC Pay Server token.
     * @param token the BTC Pay Server token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return the date this token expires.
     */
    public Instant getExpireDate() {
        return expireDate;
    }

    /**
     *
     * @param expireDate the date this token expires.
     */
    public void setExpireDate(Instant expireDate) {
        this.expireDate = expireDate;
    }

    /**
     *
     * @return the facade that is used, merchant, pos etc.
     */
    public BTCPayServerFacade getFacade() {
        return facade;
    }

    /**
     *
     * @param facade the facade that is used, merchant, pos etc.
     */
    public void setFacade(BTCPayServerFacade facade) {
        this.facade = facade;
    }

    /**
     * The id of the token, i.e the public key SIN
     */
    public String getId() {
        return id;
    }

    /**
     * The id of the token, i.e the public key SIN
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The pairing code retrieved from BTC Pay Server
     */
    public String getPairingCode() {
        return pairingCode;
    }

    /**
     * The pairing code retrieved from BTC Pay Server
     */
    public void setPairingCode(String pairingCode) {
        this.pairingCode = pairingCode;
    }

    /**
     * Name of this requesting application.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Name of this requesting application.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"id", id);
        addNotRequired(jsonObjectBuilder,"token", token);
        if(expireDate != null){
          jsonObjectBuilder.add("expireDate", expireDate.toEpochMilli());
        }
        if(facade != null){
            jsonObjectBuilder.add("facade", facade.toString());
        }
        addNotRequired(jsonObjectBuilder,"pairingCode", pairingCode);
        addNotRequired(jsonObjectBuilder,"label", label);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        try {
            id = getStringIfSet(jsonObject, "id");
            token = getStringIfSet(jsonObject, "token");
            if (jsonObject.containsKey("expireDate") && !jsonObject.isNull("expireDate")) {
                expireDate = Instant.ofEpochMilli(jsonObject.getJsonNumber("expireDate").longValueExact());
            }
            if (jsonObject.containsKey("facade") && !jsonObject.isNull("facade")) {
                facade = BTCPayServerFacade.valueOf(jsonObject.getString("facade").toUpperCase());
            }
            pairingCode = getStringIfSet(jsonObject, "pairingCode");
            label = getStringIfSet(jsonObject, "label");
        }catch(Exception e){
            throw new JsonException("Problem parsing json: " + e.getMessage(),e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token1 = (Token) o;

        if (id != null ? !id.equals(token1.id) : token1.id != null) return false;
        if (token != null ? !token.equals(token1.token) : token1.token != null) return false;
        return facade == token1.facade;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (facade != null ? facade.hashCode() : 0);
        return result;
    }
}
