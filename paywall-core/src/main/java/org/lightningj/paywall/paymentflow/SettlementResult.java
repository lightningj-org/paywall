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
package org.lightningj.paywall.paymentflow;

import org.lightningj.paywall.vo.Settlement;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object returned by getSettlement call in a payment flow.
 *
 * Created by Philip Vendil on 2018-12-29.
 */
public class SettlementResult extends Result{

    private Settlement settlement;

    /**
     * Empty Constructor
     */
    public SettlementResult(){
    }

    /**
     * Default Constructor
     *
     * @param settlement the related settlement to the payment.
     * @param token the JWT token related to the result.
     */
    public SettlementResult(Settlement settlement, String token) {
        super(token);
        this.settlement = settlement;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public SettlementResult(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the related invoice to the payment.
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /**
     *
     * @param settlement the related settlement to the payment.
     */
    public void setSettlement(Settlement settlement){
        this.settlement = settlement;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"settlement",settlement);
        super.convertToJson(jsonObjectBuilder);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        if(jsonObject.containsKey("settlement")) {
            settlement = new Settlement(getJsonObject(jsonObject, "settlement", true));
        }
        super.parseJson(jsonObject);
    }
}
