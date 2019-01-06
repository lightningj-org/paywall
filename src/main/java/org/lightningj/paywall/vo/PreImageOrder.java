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

import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.paymenthandler.Payment;
import org.lightningj.paywall.tokengenerator.JWTClaim;
import org.lightningj.paywall.util.Base64Utils;
import org.lightningj.paywall.vo.amount.Amount;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.Instant;

/**
 * Value object extending the standard by including the secret preImage
 * value that should be used for payment.
 *
 * @see Order
 * Created by Philip Vendil on 2018-12-30.
 */
public class PreImageOrder extends Order implements Payment {

    protected byte[] preImage;

    /**
     * Empty Constructor
     */
    public PreImageOrder(){}

    /**
     * Default Constructor.
     *
     * @param preImage the generated secret preImage from PreImageData. (Required)
     * @param order the related order to include preImage to. (Required)
     */
    public PreImageOrder(byte[] preImage, Order order) {
        this.preImage = preImage;
        this.preImageHash = order.getPreImageHash();
        this.description = order.getDescription();
        this.orderAmount = order.getOrderAmount();
        this.expireDate = order.getExpireDate();
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public PreImageOrder(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Parse from JWTClaims constructor
     *
     * @param jwtClaims the JWT Tokens Claim set to extract data from.
     */
    public PreImageOrder(JwtClaims jwtClaims) {
        super(jwtClaims);
    }

    /**
     *
     * @return the generated secret preImage from PreImageData.
     */
    public byte[] getPreImage() {
        return preImage;
    }

    /**
     *
     * @param preImage the generated secret preImage from PreImageData.
     */
    public void setPreImage(byte[] preImage) {
        this.preImage = preImage;
    }

    /**
     * Help method to convert the order to a pre image data.
     * @return a PreImageData representation of the pre image pair.
     */
    public PreImageData toPreImageData(){
        return new PreImageData(preImage,preImageHash);
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
        add(jsonObjectBuilder,"preImage", Base64Utils.encodeBase64String(preImage));
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
        preImage = getByteArrayFromB64(jsonObject,"preImage", true);
    }

}
