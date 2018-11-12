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

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * PreImageData is a value object containing the secret preimage value used
 * in lightning invoices and it's hash value.
 *
 * Created by philip on 2018-10-29.
 */
public class PreImageData extends JSONParsable{


    /**
     * The secret pre image value for a given invoice or payment.
     */
    private  byte[] preImage;
    /**
     * Hash value of given pre image hash.
     */
    private byte[] preImageHash;

    /**
     * Empty Constructor
     */
    public PreImageData(){}

    /**
     * Json Parsable constructor.
     * @param jsonObject the json data to parse.
     * @throws JsonException if problem occurred parsing.
     */
    public PreImageData(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Default constructor
     * @param preImage  The secret pre image value for a given invoice or payment.
     * @param preImageHash Hash value of given pre image hash.
     */
    public PreImageData(byte[] preImage, byte[] preImageHash){
        this.preImage = preImage;
        this.preImageHash = preImageHash;
    }
    /**
     *
     * @return The secret pre image value for a given invoice or payment.
     */
    public byte[] getPreImage() {
        return preImage;
    }

    /**
     *
     * @param preImage The secret pre image value for a given invoice or payment.
     */
    public void setPreImage(byte[] preImage) {
        this.preImage = preImage;
    }

    /**
     *
     * @return  Hash value of given pre image hash.
     */
    public byte[] getPreImageHash() {
        return preImageHash;
    }

    /**
     *
     * @param preImageHash  Hash value of given pre image hash.
     */
    public void setPreImageHash(byte[] preImageHash) {
        this.preImageHash = preImageHash;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"preImage", HexUtils.encodeHexString(preImage));
        addNotRequired(jsonObjectBuilder,"preImageHash", HexUtils.encodeHexString(preImageHash));
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param o the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject o) throws JsonException {
        preImage = getByteArrayFromHexIfSet(o,"preImage");
        preImageHash = getByteArrayFromHex(o,"preImageHash",true);
    }
}
