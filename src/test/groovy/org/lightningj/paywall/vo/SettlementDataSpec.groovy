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
 * Unit tests for SettlementData
 * Created by Philip Vendil on 2018-11-12.
 */
class SettlementDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def sd1 = new SettlementData()
        then:
        sd1.getPreImageHash() == null
        sd1.isSettled() == false
        sd1.getSettledAmount() == null
        sd1.getValidUntil() == null
        sd1.getSettlementDate() == null

        when:
        sd1.setPreImageHash("123".getBytes())
        sd1.setSettled(true)
        sd1.setSettledAmount(new BTC(1234));
        sd1.setValidUntil(Instant.ofEpochMilli(12345L))
        sd1.setSettlementDate(Instant.ofEpochMilli(12344L))

        then:
        sd1.getPreImageHash() == "123".getBytes()
        sd1.isSettled()
        sd1.getSettledAmount() instanceof BTC
        sd1.getValidUntil().toEpochMilli() == 12345L
        sd1.getSettlementDate().toEpochMilli() == 12344L

        when:
        def sd2 = new SettlementData("123".getBytes(),true,new BTC(1234),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12344L))
        then:
        sd2.getPreImageHash() == "123".getBytes()
        sd2.isSettled()
        sd2.getSettledAmount() instanceof BTC
        sd2.getValidUntil().toEpochMilli() == 12345L
        sd2.getSettlementDate().toEpochMilli() == 12344L
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new SettlementData("123".getBytes(),true,new BTC(1234),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12344L)).toJsonAsString(false) == """{"preImageHash":"MTIz","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":12344}"""
        when:
        new SettlementData(null,true,new BTC(1234),Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12344L)).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
        when:
        new SettlementData("123".getBytes(),true,null,Instant.ofEpochMilli(12345L),Instant.ofEpochMilli(12344L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key settledAmount is null."
        when:
        new SettlementData("123".getBytes(),true,new BTC(1234),null,Instant.ofEpochMilli(12344L)).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key validUntil is null."
        when:
        new SettlementData("123".getBytes(),true,new BTC(1234),Instant.ofEpochMilli(12345L),null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key settlementDate is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        SettlementData d = new SettlementData(toJsonObject("""{"preImageHash":"MTIz","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":12344}"""))
        then:
        d.getPreImageHash() == "123".getBytes()
        d.isSettled()
        d.getSettledAmount() instanceof CryptoAmount
        d.getValidUntil().toEpochMilli() == 12345L
        d.getSettlementDate().toEpochMilli() == 12344L

        when:
        new SettlementData(toJsonObject("""{"isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":12344}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":12344}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key isSettled is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","isSettled":true,"validUntil":12345,"settlementDate":12344}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key settledAmount is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key settlementDate is required."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"åäö","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":12344}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, problem decoding base64 data from field preImageHash."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":"abc","settlementDate":12344}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key validUntil is not a number."

        when:
        new SettlementData(toJsonObject("""{"preImageHash":"313233","isSettled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"validUntil":12345,"settlementDate":"cdf"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key settlementDate is not a number."
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new SettlementData().getClaimName() == SettlementData.CLAIM_NAME
    }
}
