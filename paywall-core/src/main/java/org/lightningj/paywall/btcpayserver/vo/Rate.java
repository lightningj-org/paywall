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

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object representing one BTC Pay Server JSON rate.
 *
 * Doesn't implement the full API just the necessary parts to integrate with paywall-core requirements.
 *
 * Created by philip on 2018-10-17.
 */
public class Rate extends JSONParsable{

    /**
     * ISO 4217 3-character currency code
     */
    String code;

    /**
     * English currency name
     */
    String name;

    /**
     *Currency units per BTC
     */
    Double rate;

    /**
     * Empty Constructor
     */
    public Rate(){}

    /**
     * Json Parsable constructor.
     * @param jsonObject the json data to parse.
     * @throws JsonException if problem occurred parsing.
     */
    public Rate(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * See property comment
     * @return the rate's code field.
     */
    public String getCode() {
        return code;
    }

    /**
     * See property comment
     * @param code the rate's code field.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * See property comment
     * @return the rate's name field.
     */
    public String getName() {
        return name;
    }

    /**
     * See property comment
     * @param name the rate's name field.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * See property comment
     * @return the rate's rate field.
     */
    public Double getRate() {
        return rate;
    }

    /**
     * See property comment
     * @param rate the rate's rate field.
     */
    public void setRate(Double rate) {
        this.rate = rate;
    }


    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"code", code);
        addNotRequired(jsonObjectBuilder,"name", name);
        addNotRequired(jsonObjectBuilder,"rate", rate);
    }

    @Override
    public void parseJson(JsonObject o) throws JsonException {
        code = getStringIfSet(o,"code");
        name = getStringIfSet(o,"name");
        rate = getDoubleIfSet(o,"rate");
    }
}
