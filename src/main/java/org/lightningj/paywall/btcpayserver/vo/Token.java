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

    String token;
    Instant expireDate;
    BTCPayServerFacade facade;

    /**
     * Default constructor for a BTC Pay Server Token
     *
     * @param token the token value
     * @param expireDate the expire instant of this token.
     * @param facade the facade this token is valid for.
     */
    public Token(String token, Instant expireDate, BTCPayServerFacade facade) {
        this.token = token;
        this.expireDate = expireDate;
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
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"token", token);
        if(expireDate != null){
          jsonObjectBuilder.add("expireDate", expireDate.toEpochMilli());
        }
        if(facade != null){
            jsonObjectBuilder.add("facade", facade.name());
        }
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
            token = getStringIfSet(jsonObject, "token");
            if (jsonObject.containsKey("expireDate") && !jsonObject.isNull("expireDate")) {
                expireDate = Instant.ofEpochMilli(jsonObject.getJsonNumber("expireDate").longValueExact());
            }
            if (jsonObject.containsKey("facade") && !jsonObject.isNull("facade")) {
                facade = BTCPayServerFacade.valueOf(jsonObject.getString("facade").toUpperCase());
            }
        }catch(Exception e){
            throw new JsonException("Problem parsing json: " + e.getMessage(),e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token that = (Token) o;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        return facade != null ? facade.equals(that.facade) : that.facade == null;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        return result;
    }


}
