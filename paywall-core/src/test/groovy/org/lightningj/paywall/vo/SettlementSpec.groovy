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
 * Unit tests for Settlement
 * Created by Philip Vendil on 2018-11-12.
 */
class SettlementSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def sd1 = new Settlement()
        then:
        sd1.getPreImageHash() == null
        sd1.getInvoice() == null
        sd1.getValidUntil() == null
        sd1.getValidFrom() == null
        !sd1.isPayPerRequest()

        when:
        sd1.setPreImageHash("123".getBytes())
        sd1.setInvoice(InvoiceSpec.genFullInvoiceData(true))
        sd1.setValidUntil(Instant.ofEpochMilli(12345L))
        sd1.setValidFrom(Instant.ofEpochMilli(12346L))
        sd1.setPayPerRequest(true)

        then:
        sd1.getPreImageHash() == "123".getBytes()
        sd1.getInvoice() instanceof Invoice
        sd1.getValidUntil().toEpochMilli() == 12345L
        sd1.getValidFrom().toEpochMilli() == 12346L
        sd1.isPayPerRequest()

        when:
        def sd2 = new Settlement("123".getBytes(),InvoiceSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12346L), true)
        then:
        sd2.getPreImageHash() == "123".getBytes()
        sd2.getInvoice() instanceof Invoice
        sd2.getValidUntil().toEpochMilli() == 12345L
        sd2.getValidFrom().toEpochMilli() == 12346L
        sd2.isPayPerRequest()
    }


    def "Verify that miminize() removes invoice field"(){
        setup:
        def sd = new Settlement("123".getBytes(),InvoiceSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12346L), true)
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
        new Settlement("123".getBytes(),InvoiceSpec.genFullInvoiceData(true),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(123446), true).toJsonAsString(false) == """{"preImageHash":"HXRC","invoice":{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"validUntil":12345,"validFrom":123446,"payPerRequest":true}"""
        new Settlement("123".getBytes(),null,Instant.ofEpochMilli(12345L),null, false).toJsonAsString(false) == """{"preImageHash":"HXRC","validUntil":12345,"payPerRequest":false}"""
        when:
        new Settlement(null,null,Instant.ofEpochMilli(12345L),null, true).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new Settlement("123".getBytes(),null,null,null, false).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key validUntil is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        Settlement d = new Settlement(toJsonObject("""{"preImageHash":"HXRC","invoice":{"preImageHash":"HXRC","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"validUntil":12345,"validFrom":12346,"payPerRequest":true}"""))
        then:
        d.getPreImageHash() == "123".getBytes()
        d.getInvoice() instanceof Invoice
        d.getValidUntil().toEpochMilli() == 12345L
        d.getValidFrom().toEpochMilli() == 12346L
        d.isPayPerRequest()

        when:
        Settlement d2 = new Settlement(toJsonObject("""{"preImageHash":"HXRC","validUntil":12345,"payPerRequest":true}"""))
        then:
        d2.getPreImageHash() == "123".getBytes()
        d2.getInvoice() == null
        d2.getValidUntil().toEpochMilli() == 12345L
        d2.getValidFrom() == null
        d2.isPayPerRequest()

        when:
        new Settlement(toJsonObject("""{"validUntil":12345,"payPerRequest":true}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"HXRC","payPerRequest":true}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is required."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"HXRC","validUntil":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key payPerRequest is required."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"aäö","validUntil":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base58 data from field preImageHash."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"HXRC","validUntil":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is not a number."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"HXRC","validUntil":12345, "validFrom":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validFrom is not a number."

        when:
        new Settlement(toJsonObject("""{"preImageHash":"HXRC","invoice":"invalidinvoice","validUntil":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing json object invoice, message: org.glassfish.json.JsonStringImpl cannot be cast to javax.json.JsonObject"
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new Settlement().getClaimName() == Settlement.CLAIM_NAME
    }
}
