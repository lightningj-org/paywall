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

import org.lightningj.paywall.vo.Invoice;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object returned by requestPayment call in a payment flow.
 *
 * Created by Philip Vendil on 2018-12-29.
 */
public class RequestPaymentResult extends Result{

    private Invoice invoice;

    /**
     * Empty Constructor
     */
    public RequestPaymentResult(){
    }

    /**
     * Default Constructor
     *
     * @param invoice the related invoice to the payment, null if no invoice have been generated yet
     *                during the payment flow.
     * @param token the JWT token related to the result.
     */
    public RequestPaymentResult(Invoice invoice, String token) {
        super(token);
        this.invoice = invoice;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public RequestPaymentResult(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the related invoice to the payment, null if no invoice have been generated yet
               during the payment flow.
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     *
     * @param invoice the related invoice to the payment, null if no invoice have been generated yet
     *                during the payment flow.
     */
    public void setInvoice(Invoice invoice){
        this.invoice = invoice;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"invoice",invoice);
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
        if(jsonObject.containsKey("invoice")) {
            invoice = new Invoice(getJsonObject(jsonObject, "invoice", true));
        }
        super.parseJson(jsonObject);
    }
}
