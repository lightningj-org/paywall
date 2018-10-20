/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.btcpayserver;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.btcpayserver.vo.Invoice;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

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
     * @throws InternalErrorException if problems occurred parsing the json data.
     */
    public Invoice parseInvoice(String jsonResponse) throws JsonException {
        if(jsonResponse == null){
            return null;
        }
        JsonReader reader = Json.createReader(new StringReader(jsonResponse));
        JsonObject response = reader.readObject();
        return new Invoice(response.getJsonObject("data"));
    }

    // TODO Token

    // TODO Rate

}
