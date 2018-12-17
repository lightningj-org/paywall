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
import org.lightningj.paywall.annotations.PaymentRequired;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderRequest contains values generated from related PaymentRequired annotation
 * and is used as a basis for creating a payment flow in PaymentHandler.
 *
 * Created by Philip Vendil on 2018-12-07.
 */
public class OrderRequest extends JSONParsable {

    String articleId;
    int units;
    List<PaymentOption> paymentOptions;
    boolean payPerRequest;

    /**
     * Empty Constructor
     */
    public OrderRequest(){
    }

    /**
     * Default constructor.
     *
     * @param articleId the article id derived from PaymentRequired notation and OrderRequestGenerator.
     * @param calculatedUnits the calculated units for paymentRequiredAnnotation unit calculator.
     * @param paymentOptions optional payment options which can be custom values defined between
     * PaymentRequired annotation and used PaymentHandler.
     * @param payPerRequest true if payment only should be valid for one payment, set by annotation, but
     *                      PaymentHandler might override this setting. Important: If used must PaymentData implementation
     *                      implement PerRequestPaymentData.
     */
    public OrderRequest(String articleId, int calculatedUnits, List<PaymentOption> paymentOptions, boolean payPerRequest){
        this.articleId = articleId;
        this.units = calculatedUnits;
        this.paymentOptions = paymentOptions;
        this.payPerRequest = payPerRequest;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public OrderRequest(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the article id derived from PaymentRequired notation and OrderRequestGenerator.
     */
    public String getArticleId() {
        return articleId;
    }

    /**
     *
     * @param articleId the article id derived from PaymentRequired notation and OrderRequestGenerator.
     */
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    /**
     *
     * @return the number of units ordered.
     */
    public int getUnits() {
        return units;
    }

    /**
     *
     * @param units the number of units ordered.
     */
    public void setUnits(int units) {
        this.units = units;
    }

    /**
     *
     * @return optional payment options which can be custom values defined between
     * PaymentRequired annotation and used PaymentHandler.
     */
    public List<PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    /**
     *
     * @param paymentOptions optional payment options which can be custom values defined between
     * PaymentRequired annotation and used PaymentHandler.
     */
    public void setPaymentOptions(List<PaymentOption> paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    /**
     *
     * @return true if payment only should be valid for one payment, set by annotation, but
     *                      PaymentHandler might override this setting. Important: If used must PaymentData implementation
     *                      implement PerRequestPaymentData.
     */
    public boolean isPayPerRequest() {
        return payPerRequest;
    }

    /**
     *
     * @param payPerRequest true if payment only should be valid for one payment, set by annotation, but
     *                      PaymentHandler might override this setting. Important: If used must PaymentData implementation
     *                      implement PerRequestPaymentData.
     */
    public void setPayPerRequest(boolean payPerRequest) {
        this.payPerRequest = payPerRequest;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"articleId",articleId);
        addNotRequired(jsonObjectBuilder,"units",units);
        addNotRequired(jsonObjectBuilder, "paymentOptions",paymentOptions);
        add(jsonObjectBuilder, "payPerRequest",payPerRequest);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        articleId = getStringIfSet(jsonObject, "articleId");
        Integer unitsInt = getIntIfSet(jsonObject, "units");
        if(unitsInt != null) {
            units =unitsInt;
        }
        JsonArray jsonArray = getJsonArrayIfSet(jsonObject,"paymentOptions");
        if(jsonArray != null) {
            try {
                paymentOptions = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    paymentOptions.add(new PaymentOption(jsonArray.getJsonObject(i)));
                }
            } catch (JsonException e) {
                throw e;
            } catch (Exception e) {
                throw new JsonException("Error parsing json paymentOptions field on OrderRequest : " + e.getMessage(), e);
            }
        }
        if(jsonObject.containsKey("payPerRequest")){
            payPerRequest = getBoolean(jsonObject, "payPerRequest", payPerRequest);
        }
    }
}
