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
package org.lightningj.paywall.lightninghandler;

import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext;

import javax.json.JsonException;
import javax.json.JsonObject;

/**
 * Generic interface for the context of
 * latest known state of a lightning handler after restart.
 *
 * Created by Philip Vendil on 2018-12-15.
 */
public interface LightningHandlerContext {

    /**
     * Factory method to parse lightning context json data into value objects.
     * @param jsonObject the json data to parse. reades the type field from the object
     *                   and creates an instance of the related object.
     * @return a new LightningHandlerContext from the json data.
     * @throws JsonException if json data couldn't be parsed.
     */
    static LightningHandlerContext parseContext(JsonObject jsonObject) throws JsonException{
       if(!jsonObject.containsKey("type")){
           throw new JsonException("Error parsing LightningHandlerContext json data, no type field specified.");
       }
       String type =jsonObject.getString("type");
       if(type == null){

       }
       if(type.equals(LNDLightningHandlerContext.CONTEXT_TYPE)){
           return new LNDLightningHandlerContext(jsonObject);
       }
       throw new JsonException("Error parsing LightningHandlerContext json data, invalid type field '" + type + "' specified.");
    }
}
