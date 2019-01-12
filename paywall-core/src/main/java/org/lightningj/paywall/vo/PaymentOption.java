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

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object representation of an annotated payment option in @PaymentRequired
 * annotation.
 *
 * Created by Philip on 2018-12-07.
 */
public class PaymentOption extends JSONParsable {

    protected String option;
    protected String value;

    /**
     * Empty Constructor
     */
    public PaymentOption(){
    }

    /**
     * Constructor converting an annotated payment option into a value object.
     */
    public PaymentOption(org.lightningj.paywall.annotations.vo.PaymentOption paymentOption){
        this.option = paymentOption.option();
        this.value = paymentOption.value();
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public PaymentOption(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the name of the option set. Values should be supported
     * by the configured payment handler.
     */
    public String getOption() {
        return option;
    }

    /**
     *
     * @param option the name of the option set. Values should be supported
     * by the configured payment handler.
     */
    public void setOption(String option) {
        this.option = option;
    }

    /**
     *
     * @return the options value. Values should be supported
     * by the configured payment handler.
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value the options value. Values should be supported
     * by the configured payment handler.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"option",option);
        addNotRequired(jsonObjectBuilder,"value",value);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        option = getStringIfSet(jsonObject,"option");
        value = getStringIfSet(jsonObject,"value");
    }
}
