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
package org.lightningj.paywall.vo

import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.vo.amount.CryptoAmount
import spock.lang.Specification

import javax.json.JsonException
import java.time.Instant

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for InvoiceData.
 *
 * Created by Philip Vendil on 2018-11-12.
 */
class InvoiceDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def id1 = new InvoiceData()
        then:
        id1.getPreImageHash() == null
        id1.getExpireDate() == null
        id1.getBolt11Invoice() == null
        id1.getInvoiceAmount() == null
        id1.getNodeInfo() == null
        id1.getInvoiceDate() == null
        when:
        id1.setPreImageHash("123".getBytes())
        id1.setBolt11Invoice("fksjeoskajduakdfhaskdismensuduajseusdke")
        id1.setInvoiceAmount(new BTC(123))
        id1.setNodeInfo(new NodeInfo("12312312@10.10.01.1"))
        id1.setExpireDate(Instant.ofEpochMilli(12345L))
        id1.setInvoiceDate(Instant.ofEpochMilli(2345L))
        then:
        id1.getPreImageHash() == "123".getBytes()
        id1.getBolt11Invoice() == "fksjeoskajduakdfhaskdismensuduajseusdke"
        id1.getInvoiceAmount() instanceof BTC
        id1.getNodeInfo().getNodeAddress() == "10.10.01.1"
        id1.getExpireDate().toEpochMilli() == 12345L
        id1.getInvoiceDate().toEpochMilli() == 2345L
        when:
        def id2 = new InvoiceData("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",new BTC(123),new NodeInfo("12312312@10.10.01.1"),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L))
        then:
        id2.getPreImageHash() == "123".getBytes()
        id2.getBolt11Invoice() == "fksjeoskajduakdfhaskdismensuduajseusdke"
        id2.getInvoiceAmount() instanceof BTC
        id2.getNodeInfo().getNodeAddress() == "10.10.01.1"
        id2.getExpireDate().toEpochMilli() == 12345L
        id2.getInvoiceDate().toEpochMilli() == 2345L
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new InvoiceData("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",new BTC(123),new NodeInfo("12312312@10.10.01.1"),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false) == """{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345}"""
        new InvoiceData("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false) == """{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345}"""
        when:
        new InvoiceData(null,"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new InvoiceData("123".getBytes(),null,null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key bolt11Invoice is null."
        when:
        new InvoiceData("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,null,Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key expireDate is null."
        when:
        new InvoiceData("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,Instant.ofEpochMilli(12345L),null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key invoiceDate is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        InvoiceData d = new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"
        d.getInvoiceAmount() instanceof CryptoAmount
        d.getNodeInfo().getNodeAddress() == "10.10.01.1"
        d.getExpireDate().toEpochMilli() == 12345L
        d.getInvoiceDate().toEpochMilli() == 2345L

        when:
        d = new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"
        d.getInvoiceAmount() == null
        d.getNodeInfo() == null
        d.getExpireDate().toEpochMilli() == 12345L
        d.getInvoiceDate().toEpochMilli() == 2345L

        when:
        new InvoiceData(toJsonObject("""{"bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","expireDate":12345,"invoiceDate":2345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key bolt11Invoice is required."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceDate":2345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is required."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key invoiceDate is required."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"åäö","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base64 data from field preImageHash."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":"abc","invoiceDate":2345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is not a number."

        when:
        new InvoiceData(toJsonObject("""{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":"cds"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key invoiceDate is not a number."
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new InvoiceData().getClaimName() == InvoiceData.CLAIM_NAME
    }
}
