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
 * Unit tests for PaymentData
 *
 * Created by Philip Vendil on 2018-11-11.
 */
class PaymentDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def pd1 = new PaymentData()
        then:
        pd1.getPreImageHash() == null
        pd1.getExpireDate() == null
        pd1.getDescription() == null
        pd1.getRequestedAmount() == null
        when:
        pd1.setPreImageHash("123".getBytes())
        pd1.setDescription("SomeDescription")
        pd1.setRequestedAmount(new BTC(1234))
        pd1.setExpireDate(Instant.ofEpochMilli(12345L))
        then:
        pd1.getPreImageHash() == "123".getBytes()
        pd1.getExpireDate().toEpochMilli() == 12345L
        pd1.getDescription() == "SomeDescription"
        pd1.getRequestedAmount() instanceof BTC
        when:
        def pd2 = new PaymentData("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L))
        then:
        pd2.getPreImageHash() == "123".getBytes()
        pd2.getExpireDate().toEpochMilli() == 12345L
        pd2.getDescription() == "SomeDescription"
        pd2.getRequestedAmount() instanceof BTC
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new PaymentData("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)).toJsonAsString(false) == """{"preImageHash":"MTIz","description":"SomeDescription","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""
        new PaymentData("123".getBytes(),null,new BTC(1234),Instant.ofEpochMilli(12345L)).toJsonAsString(false) == """{"preImageHash":"MTIz","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""
        when:
        new PaymentData(null,null,new BTC(1234),Instant.ofEpochMilli(12345L)).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new PaymentData("123".getBytes(),null,null,Instant.ofEpochMilli(12345L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key requestedAmount is null."
        when:
        new PaymentData("123".getBytes(),null,new BTC(1234),null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key expireDate is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        PaymentData d = new PaymentData(toJsonObject("""{"preImageHash":"MTIz","description":"SomeDescription","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.description == "SomeDescription"
        d.requestedAmount instanceof CryptoAmount
        d.requestedAmount.currencyCode == "BTC"
        d.expireDate == Instant.ofEpochMilli(12345L)

        when:
        d = new PaymentData(toJsonObject("""{"preImageHash":"MTIz","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.description == null

        when:
        new PaymentData(toJsonObject("""{"requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new PaymentData(toJsonObject("""{"preImageHash":"MTIz","expireDate":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key requestedAmount is required."

        when:
        new PaymentData(toJsonObject("""{"preImageHash":"MTIz","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"}}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is required."

        when:
        new PaymentData(toJsonObject("""{"preImageHash":"åäö","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base64 data from field preImageHash."

        when:
        new PaymentData(toJsonObject("""{"preImageHash":"MTIz","requestedAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key expireDate is not a number."
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new PaymentData().getClaimName() == PaymentData.CLAIM_NAME
    }
}
