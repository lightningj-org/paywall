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
package org.lightningj.paywall.vo.amount;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Amount specifying a FIAT currency with an value and a currency code.
 * The amount is specified as an double and will be converted into
 * a crypto currency amount before LightningHandler is called.
 *
 * Created by Philip Vendil on 2018-11-07.
 */
public class FiatAmount extends Amount {

    private double value;
    private String currencyCode;

    /**
     * Empty Constructor
     */
    public FiatAmount(){
        super(AmountType.FIAT);
    }

    /**
     * Default Constructor with a value and a currencyCode.
     *
     * @param value the value of fiat currency as a double.
     * @param currencyCode the related currency code. Make sure it is
     *                     supported by the used currency converter.
     */
    public FiatAmount(double value, String currencyCode){
        super(AmountType.FIAT);
        this.value = value;
        this.currencyCode = currencyCode;
    }

    /**
     * JSON Parseable constructor
     * @param jsonObject the jsonObject to parse.
     * @throws JsonException if problems occurred parsing the json data.
     */
    public FiatAmount(JsonObject jsonObject) throws JsonException{
        super(AmountType.FIAT);
        parseJson(jsonObject);
    }

    /**
     *
     * @return the value of fiat currency as a double.
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @param value the value of fiat currency as a double.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     *
     * @return the related currency code. Make sure it is
     *                     supported by the used currency converter.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     *
     * @param currencyCode the related currency code. Make sure it is
     *                     supported by the used currency converter.
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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
        add(jsonObjectBuilder,"value", value);
        add(jsonObjectBuilder,"currencyCode", currencyCode);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        value = getDouble(jsonObject,"value", true);
        currencyCode = getString(jsonObject, "currencyCode", true);
    }
}
