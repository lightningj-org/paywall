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
 * Unit tests for Invoice.
 *
 * Created by Philip Vendil on 2018-11-12.
 */
class InvoiceSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def id1 = new Invoice()
        then:
        id1.getPreImageHash() == null
        id1.getExpireDate() == null
        id1.getBolt11Invoice() == null
        id1.getDescription() == null
        id1.getInvoiceAmount() == null
        id1.getNodeInfo() == null
        id1.getInvoiceDate() == null
        !id1.isSettled()
        id1.getSettledAmount() == null
        id1.getSettlementDate() == null
        id1.getSourceNode() == null
        when:
        id1.setPreImageHash("123".getBytes())
        id1.setBolt11Invoice("fksjeoskajduakdfhaskdismensuduajseusdke")
        id1.setDescription("test desc")
        id1.setInvoiceAmount(new BTC(123))
        id1.setNodeInfo(new NodeInfo("12312312@10.10.01.1"))
        id1.setExpireDate(Instant.ofEpochMilli(12345L))
        id1.setInvoiceDate(Instant.ofEpochMilli(2345L))
        id1.setSettled(true)
        id1.setSettledAmount(new BTC(1234))
        id1.setSettlementDate(Instant.ofEpochMilli(12344L))
        id1.setSourceNode("SomeSourceNode")
        then:
        id1.getPreImageHash() == "123".getBytes()
        id1.getBolt11Invoice() == "fksjeoskajduakdfhaskdismensuduajseusdke"
        id1.getDescription() == "test desc"
        id1.getInvoiceAmount() instanceof BTC
        id1.getNodeInfo().getNodeAddress() == "10.10.01.1"
        id1.getExpireDate().toEpochMilli() == 12345L
        id1.getInvoiceDate().toEpochMilli() == 2345L
        id1.isSettled()
        id1.getSettledAmount()  instanceof BTC
        id1.getSettlementDate().toEpochMilli() == 12344L
        id1.getSourceNode() == "SomeSourceNode"
        when:
        def id2 = genFullInvoiceData(false)
        then:
        id2.getPreImageHash() == "123".getBytes()
        id2.getBolt11Invoice() == "fksjeoskajduakdfhaskdismensuduajseusdke"
        id2.getDescription() == "test desc"
        id2.getInvoiceAmount() instanceof BTC
        id2.getNodeInfo().getNodeAddress() == "10.10.01.1"
        id2.getExpireDate().toEpochMilli() == 12345L
        id2.getInvoiceDate().toEpochMilli() == 2345L
        !id2.isSettled()
        id2.getSettledAmount() == null
        id2.getSettlementDate() == null
        id2.getSourceNode() == null
        when:
        def id3 = genFullInvoiceData(true)
        then:
        id3.getPreImageHash() == "123".getBytes()
        id3.getBolt11Invoice() == "fksjeoskajduakdfhaskdismensuduajseusdke"
        id3.getDescription() == "test desc"
        id3.getInvoiceAmount() instanceof BTC
        id3.getNodeInfo().getNodeAddress() == "10.10.01.1"
        id3.getExpireDate().toEpochMilli() == 12345L
        id3.getInvoiceDate().toEpochMilli() == 2345L
        id3.isSettled()
        id3.getSettledAmount()  instanceof BTC
        id3.getSettlementDate().toEpochMilli() == 12344L
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        setup:
        def fullInvoice = genFullInvoiceData(true)
        fullInvoice.sourceNode = "SomeSourceNode"
        expect:
        new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke","test desc",new BTC(123),new NodeInfo("12312312@10.10.01.1"),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false) == """{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":false}"""
        new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false) == """{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345,"settled":false}"""
        fullInvoice.toJsonAsString(false) == """{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344,"sourceNode":"SomeSourceNode"}"""

        when:
        new Invoice(null,"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new Invoice("123".getBytes(),null,null,null,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key bolt11Invoice is null."
        when:
        new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,null,null,Instant.ofEpochMilli(2345L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key expireDate is null."
        when:
        new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke",null,null,null,Instant.ofEpochMilli(12345L),null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key invoiceDate is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        Invoice d = new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description": "test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344,"sourceNode":"SomeSourceNode"}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"
        d.description == "test desc"
        d.getInvoiceAmount() instanceof CryptoAmount
        d.getNodeInfo().getNodeAddress() == "10.10.01.1"
        d.getExpireDate().toEpochMilli() == 12345L
        d.getInvoiceDate().toEpochMilli() == 2345L
        d.isSettled()
        d.getSettledAmount() instanceof CryptoAmount
        d.getSettlementDate().toEpochMilli() == 12344L
        d.getSourceNode() == "SomeSourceNode"

        when:
        d = new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345, "settled": false}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"
        d.description == null
        d.getInvoiceAmount() == null
        d.getNodeInfo() == null
        d.getExpireDate().toEpochMilli() == 12345L
        d.getInvoiceDate().toEpochMilli() == 2345L
        !d.isSettled()

        when:
        new Invoice(toJsonObject("""{"bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345, "settled": false}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","expireDate":12345,"invoiceDate":2345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key bolt11Invoice is required."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceDate":2345, "settled": false}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is required."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345, "settled": false}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key invoiceDate is required."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"åäö","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345, "settled": false}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base58 data from field preImageHash."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":"abc","invoiceDate":2345, "settled": false}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is not a number."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":"cds", "settled": false}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key invoiceDate is not a number."

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345, "settled": false, "settledAmount": "123"}"""))
        then:
        e = thrown(JsonException)
        e.message =~ "Error parsing json object settledAmount, message:"

        when:
        new Invoice(toJsonObject("""{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","expireDate":12345,"invoiceDate":2345, "settled": false, "settlementDate": "abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key settlementDate is not a number."
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new Invoice().getClaimName() == Invoice.CLAIM_NAME
    }

    static Invoice genFullInvoiceData(boolean withSettlement=true){
        if(withSettlement){
            new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke","test desc",new BTC(123),new NodeInfo("12312312@10.10.01.1"),
                    Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L),true,new BTC(1234),
                    Instant.ofEpochMilli(12344L))
        }else{
            return new Invoice("123".getBytes(),"fksjeoskajduakdfhaskdismensuduajseusdke","test desc",new BTC(123),new NodeInfo("12312312@10.10.01.1"),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(2345L))
        }
    }
}
