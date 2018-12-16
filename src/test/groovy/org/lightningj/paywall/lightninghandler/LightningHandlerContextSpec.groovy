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
package org.lightningj.paywall.lightninghandler

import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext
import spock.lang.Specification

import javax.json.Json
import javax.json.JsonException
import javax.json.JsonObject

/**
 * Unit tests for LightningHandlerContext
 *
 * Created by Philip Vendil on 2018-12-15.
 */
class LightningHandlerContextSpec extends Specification {

    def "Verify that a correct LNDLightningHandlerContext is parsed if type is lnd."(){
        setup:
        JsonObject jsonObject = Json.createReader(new StringReader("""{"type":"lnd","addIndex":123,"settleIndex":234}""")).readObject()
        when:
        LNDLightningHandlerContext ctx = LightningHandlerContext.parseContext(jsonObject)
        then:
        ctx.addIndex == 123
        ctx.settleIndex == 234
    }

    def "Verify that parseContext throws JsonException if json data didn't contain any type."(){
        setup:
        JsonObject jsonObject = Json.createReader(new StringReader("""{"addIndex":123,"settleIndex":234}""")).readObject()
        when:
        LightningHandlerContext.parseContext(jsonObject)
        then:
        def e = thrown JsonException
        e.message == "Error parsing LightningHandlerContext json data, no type field specified."
    }

    def "Verify that parseContext throws JsonException if json data contained unsupported type."(){
        setup:
        JsonObject jsonObject = Json.createReader(new StringReader("""{"type":"unsupported","addIndex":123,"settleIndex":234}""")).readObject()
        when:
        LightningHandlerContext.parseContext(jsonObject)
        then:
        def e = thrown JsonException
        e.message == "Error parsing LightningHandlerContext json data, invalid type field 'unsupported' specified."
    }
}
