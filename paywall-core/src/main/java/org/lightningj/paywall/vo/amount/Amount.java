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

import org.lightningj.paywall.JSONParsable;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Abstract class for a given amount of currency used in a payment.
 *
 * Can either be a FIAT or CrytoCurrency amount.
 *
 * @see FiatAmount
 * @see CryptoAmount
 *
 * Created by Philip Vendil on 2018-11-07.
 */
public abstract class Amount extends JSONParsable{

    protected AmountType type;

    protected Amount(AmountType type){
        this.type = type;
    }

    /**
     *
     * @return if amount is a FIAT or cryptocurrency.
     */
    public AmountType getType() {
        return type;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        add(jsonObjectBuilder,"type", type.name());
    }

    /**
     * Help method to parse a JSON amount depending on its type field, fiat and cryptoamount.
     * @param jsonObject the json object to convert to an amount.
     * @return either a FiatAmount or CryptoAmount.
     * @throws JsonException if problems occurred parsing the json.
     */
    public static Amount parseAmountObject(JsonObject jsonObject) throws JsonException{
        String typeValue;
        if(jsonObject.containsKey("type") && !jsonObject.isNull("type")) {
            typeValue = jsonObject.getString("type");
        }else{
            throw new JsonException("Error parsing JSON, no type field set in amount.");
        }
        try{
            AmountType amountType = AmountType.valueOf(typeValue.toUpperCase());
            if(amountType == AmountType.CRYTOCURRENCY){
                return new CryptoAmount(jsonObject);
            }
            return new FiatAmount(jsonObject);
        }catch (Exception e){
            if(e instanceof JsonException){
                throw (JsonException) e;
            }
            throw new JsonException("Error parsing JSON, invalid amount type " + typeValue + ".");
        }
    }
}
