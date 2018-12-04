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

import spock.lang.Specification

import javax.json.JsonException
import java.time.Instant

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for SettlementData
 * Created by Philip Vendil on 2018-11-12.
 */
class SettlementDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def sd1 = new SettlementData()
        then:
        sd1.getPreImageHash() == null
        sd1.getInvoice() == null
        sd1.getValidUntil() == null
        sd1.getValidFrom() == null

        when:
        sd1.setPreImageHash("123".getBytes())
        sd1.setInvoice(InvoiceDataSpec.genFullInvoiceData(true))
        sd1.setValidUntil(Instant.ofEpochMilli(12345L))
        sd1.setValidFrom(Instant.ofEpochMilli(12346L))

        then:
        sd1.getPreImageHash() == "123".getBytes()
        sd1.getInvoice() instanceof InvoiceData
        sd1.getValidUntil().toEpochMilli() == 12345L
        sd1.getValidFrom().toEpochMilli() == 12346L

        when:
        def sd2 = new SettlementData("123".getBytes(),InvoiceDataSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12346L))
        then:
        sd2.getPreImageHash() == "123".getBytes()
        sd2.getInvoice() instanceof InvoiceData
        sd2.getValidUntil().toEpochMilli() == 12345L
        sd2.getValidFrom().toEpochMilli() == 12346L
    }


    def "Verify that miminize() removes invoice field"(){
        setup:
        def sd = new SettlementData("123".getBytes(),InvoiceDataSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12346L))
        expect:
        sd.invoice != null
        when:
        sd.minimizeData()
        then:
        sd.invoice == null

    }
    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new SettlementData("123".getBytes(),InvoiceDataSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(123446)).toJsonAsString(false) == """{"preImageHash":"MTIz","invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"validUntil":12345,"validFrom":123446}"""
        new SettlementData("123".getBytes(),null,Instant.ofEpochMilli(12345L),null).toJsonAsString(false) == """{"preImageHash":"MTIz","validUntil":12345}"""
        when:
        new SettlementData(null,null,Instant.ofEpochMilli(12345L),null).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new SettlementData("123".getBytes(),null,null,null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key validUntil is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        SettlementData d = new SettlementData(toJsonObject("""{"preImageHash":"MTIz","invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"validUntil":12345,"validFrom":12346}"""))
        then:
        d.getPreImageHash() == "123".getBytes()
        d.getInvoice() instanceof InvoiceData
        d.getValidUntil().toEpochMilli() == 12345L
        d.getValidFrom().toEpochMilli() == 12346L

        when:
        SettlementData d2 = new SettlementData(toJsonObject("""{"preImageHash":"MTIz","validUntil":12345}"""))
        then:
        d2.getPreImageHash() == "123".getBytes()
        d2.getInvoice() == null
        d2.getValidUntil().toEpochMilli() == 12345L
        d2.getValidFrom() == null

        when:
        new SettlementData(toJsonObject("""{"validUntil":12345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"MTIz"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is required."


        when:
        new SettlementData(toJsonObject("""{"preImageHash":"aäö","validUntil":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base64 data from field preImageHash."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"MTIz","validUntil":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is not a number."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"MTIz","validUntil":12345, "validFrom":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validFrom is not a number."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"MTIz","invoice":"invalidinvoice","validUntil":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing json object invoice, message: org.glassfish.json.JsonStringImpl cannot be cast to javax.json.JsonObject"
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new SettlementData().getClaimName() == SettlementData.CLAIM_NAME
    }
}
