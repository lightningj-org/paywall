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
 * Amount specified in crypto currency where value is the amount in its
 * smallest demination. For example for BTC should the value be specified
 * in satoshis and not whole Bitcoin.
 *
 * It is also possible to specify the amount in sub unit using the optional
 * magnitude field. For example to specify amount in millisatoshis set magnitude
 * to MILLI.
 *
 * Created by Philip Vendil on 2018-11-07.
 */
public class CryptoAmount extends Amount {

    protected long value;
    protected String currencyCode;
    protected Magnetude magnetude = Magnetude.NONE;

    /**
     * Empty Constructor
     */
    public CryptoAmount(){
        super(AmountType.CRYTOCURRENCY);
    }

    /**
     * Default Constructor, without magnitude
     *
     * @param value the amount in smallest unit for the cryptocurrency, for
     *              example set in satoshis for BTC.
     * @param currencyCode the currency code, for example "BTC".
     */
    public CryptoAmount(long value, String currencyCode){
        this(value,currencyCode,Magnetude.NONE);
    }

    /**
     * Default Constructor
     *
     * @param value the amount in smallest unit for the cryptocurrency, for
     *              example set in satoshis for BTC.
     * @param currencyCode the currency code, for example "BTC".
     * @param magnetude If sub units to the value it is possible to specify a magnitude.
     *                  for example set to MILLI for millisatoshis for BTC.
     */
    public CryptoAmount(long value, String currencyCode, Magnetude magnetude){
        super(AmountType.CRYTOCURRENCY);
        this.value = value;
        this.currencyCode = currencyCode;
        this.magnetude = magnetude;
    }

    /**
     *
     * @return the amount in smallest unit for the cryptocurrency, for
     *              example set in satoshis for BTC.
     */
    public long getValue() {
        return value;
    }

    /**
     *
     * @param value the amount in smallest unit for the cryptocurrency, for
     *              example set in satoshis for BTC.
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     *
     * @return the currency code, for example "BTC".
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     *
     * @param currencyCode the currency code, for example "BTC".
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     *
     * @return If sub units to the value it is possible to specify a magnitude.
     *                  for example set to MILLI for millisatoshis for BTC.
     */
    public Magnetude getMagnetude() {
        return magnetude;
    }

    /**
     *
     * @param magnetude If sub units to the value it is possible to specify a magnitude.
     *                  for example set to MILLI for millisatoshis for BTC.
     */
    public void setMagnetude(Magnetude magnetude) {
        this.magnetude = magnetude;
    }

    /**
     * JSON Parseable constructor
     */
    public CryptoAmount(JsonObject jsonObject) throws JsonException{
        super(AmountType.CRYTOCURRENCY);
        parseJson(jsonObject);
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
        add(jsonObjectBuilder,"value",value);
        add(jsonObjectBuilder,"currencyCode",currencyCode);
        if(magnetude != null) {
            add(jsonObjectBuilder, "magnetude", magnetude.name());
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
        value = getLong(jsonObject,"value",true);
        currencyCode = getString(jsonObject,"currencyCode", true);

        String magintudeVal = getStringIfSet(jsonObject, "magnetude");
        if(magintudeVal != null){
            try {
                magnetude = Magnetude.valueOf(magintudeVal);
            }catch(Exception e){
                throw new JsonException("Error parsing JSON data, Invalid value " + magintudeVal + " for json key magnetude.");
            }
        }

    }
}
