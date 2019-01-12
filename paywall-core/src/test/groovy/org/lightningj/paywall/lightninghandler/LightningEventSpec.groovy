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
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.InvoiceSpec
import spock.lang.Specification

import javax.json.JsonException

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for LightningEvent.
 *
 * Created by Philip Vendil on 2018-11-28.
 */
class LightningEventSpec extends Specification {

    Invoice invoiceData = InvoiceSpec.genFullInvoiceData()

    def "Verify constructors and getter and setters"(){
        when:
        def le1 = new LightningEvent()
        then:
        le1.getType() == null
        le1.getInvoice() == null
        when:
        le1.setType(LightningEventType.ADDED)
        le1.setInvoice(invoiceData)
        then:
        le1.getType() == LightningEventType.ADDED
        le1.getInvoice() != null

        when:
        def le2 = new LightningEvent(LightningEventType.ADDED,invoiceData, new LNDLightningHandlerContext(123,321))
        then:
        le2.getType() == LightningEventType.ADDED
        le2.getInvoice() != null
        le2.getContext().addIndex == 123
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new LightningEvent(LightningEventType.ADDED,invoiceData,new LNDLightningHandlerContext(123,321)).toJsonAsString(false) == """{"type":"ADDED","invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"context":{"type":"lnd","addIndex":123,"settleIndex":321}}"""
        when:
        new LightningEvent(null,invoiceData, null).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key type is null."
        when:
        new LightningEvent(LightningEventType.ADDED,null, null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key invoice is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        LightningEvent d = new LightningEvent(toJsonObject("""{"type":"ADDED","invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"context":{"type":"lnd","addIndex":123,"settleIndex":321}}"""))
        then:
        d.type == LightningEventType.ADDED
        d.invoice != null
        d.context.addIndex == 123
        when:
        new LightningEvent(toJsonObject("""{"type":"INVALID","invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON, invalid lightning event type INVALID."
        when:
        new LightningEvent(toJsonObject("""{"invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key type is required."
        when:
        new LightningEvent(toJsonObject("""{"type":"ADDED"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key invoice is required."
    }
}
