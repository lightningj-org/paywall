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
package org.lightningj.paywall.btcpayserver;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.btcpayserver.vo.Invoice;
import org.lightningj.paywall.btcpayserver.vo.Token;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/**
 * Class parsing JSON responses from BTC Pay Server into value objects.
 *
 *
 * Created by philip on 2018-10-17.
 */
public class BTCPayServerResponseParser {

    /**
     * Default constructor
     */
    public BTCPayServerResponseParser(){
    }

    /**
     * Method to parse and convert a BTC Pay Server JSON invoice response into a value object
     * invoice.
     * @param jsonResponse the jsonResponse as a string.
     * @return the related invoice value object.
     * @throws JsonException if problems occurred parsing the json data.
     */
    public Invoice parseInvoice(byte[] jsonResponse) throws JsonException {
        if(jsonResponse == null){
            return null;
        }
        return new Invoice(toJsonObject(jsonResponse).getJsonObject("data"));
    }

    /**
     * Method to parse and convert a BTC Pay Server JSON invoice response into a value object
     * invoice.
     * @param jsonResponse the jsonResponse as a string.
     * @return the related invoice value object.
     * @throws JsonException if problems occurred parsing the json data.
     */
    public Token parseToken(byte[] jsonResponse) throws JsonException {
        if(jsonResponse == null){
            return null;
        }
        return new Token(toJsonObject(jsonResponse).getJsonArray("data").getJsonObject(0));
    }

    protected JsonObject toJsonObject(byte[] jsonResponse) throws JsonException {
        try {
            System.out.print(new String(jsonResponse));
          JsonReader reader = Json.createReader(new StringReader(new String(jsonResponse, "UTF-8")));
           return reader.readObject();
        }catch (UnsupportedEncodingException e){
            throw  new JsonException(e.getMessage(),e);
        }
    }
    // TODO Token Test

    // TODO Rate

}
