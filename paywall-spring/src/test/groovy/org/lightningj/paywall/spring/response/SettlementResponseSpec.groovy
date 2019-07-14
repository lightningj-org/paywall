/*
 * ***********************************************************************
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
package org.lightningj.paywall.spring.response

import org.lightningj.paywall.paymentflow.SettlementResult
import org.lightningj.paywall.vo.Settlement
import spock.lang.Shared
import spock.lang.Specification

import javax.json.Json
import java.time.Instant

/**
 * Unit tests for SettlementResponse
 */
class SettlementResponseSpec extends Specification {

    SettlementResult settlementResult

    @Shared def currentTimeZone
    @Shared def currentLocale

    def setupSpec(){
        currentTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        currentLocale = Locale.getDefault()
        Locale.setDefault(new Locale("sv","SE"))
    }

    def setup() {
        Settlement settlement = new Settlement("abc".getBytes(), null, Instant.ofEpochMilli(10000), Instant.ofEpochMilli(5000), true)
        settlementResult = new SettlementResult(settlement, "SomeToken")
    }

    def cleanupSpec(){
        TimeZone.setDefault(currentTimeZone)
        Locale.setDefault(currentLocale)
    }

    def "Verify constructors and getter and setters"() {
        when:
        SettlementResponse r = new SettlementResponse(settlementResult)
        then:
        r.settled
        r.preImageHash == "ZiCa"
        r.payPerRequest
        r.settlementValidFrom == new Date(5000);
        r.settlementValidUntil == new Date(10000)
        r.token == "SomeToken"

        r.type == SettlementResponse.TYPE
        when:
        r = new SettlementResponse()
        r.settled = true
        r.preImageHash = "abc"
        r.payPerRequest = true
        r.settlementValidFrom = new Date(6000)
        r.settlementValidUntil = new Date(11000)
        r.token = "token1"
        then:
        r.settled
        r.preImageHash == "abc"
        r.payPerRequest
        r.settlementValidFrom == new Date(6000)
        r.settlementValidUntil == new Date(11000)
        r.token == "token1"
    }

    def "Verify toString"() {
        expect:
        new SettlementResponse(settlementResult).toString() == """SettlementResponse
{
    "status": "OK",
    "preImageHash": "ZiCa",
    "token": "SomeToken",
    "settlementValidUntil": "1970-01-01T01:00:10.000+0100",
    "settlementValidFrom": "1970-01-01T01:00:05.000+0100",
    "payPerRequest": true,
    "settled": true
}"""
    }

    def "Verify json conversion works."(){
        when:
        SettlementResponse r = new SettlementResponse(settlementResult)
        def json1 = r.toJsonAsString(true)
        then:
        json1 == """
{
    "status": "OK",
    "preImageHash": "ZiCa",
    "token": "SomeToken",
    "settlementValidUntil": "1970-01-01T01:00:10.000+0100",
    "settlementValidFrom": "1970-01-01T01:00:05.000+0100",
    "payPerRequest": true,
    "settled": true
}"""

        when:
        SettlementResponse r2 = new SettlementResponse(Json.createReader(new StringReader(json1)).readObject())

        then:
        r2.settled
        r2.preImageHash == "ZiCa"
        r2.payPerRequest
        r2.settlementValidFrom == new Date(5000);
        r2.settlementValidUntil == new Date(10000)
        r2.token == "SomeToken"

        when:
        SettlementResponse r3 = new SettlementResponse()
        def json2 = r3.toJsonAsString(true)
        then:
        json2 == """
{
    "status": "OK",
    "settled": false
}"""

        when:
        SettlementResponse r4 = new SettlementResponse(Json.createReader(new StringReader(json2)).readObject())
        then:
        !r4.settled
        r4.preImageHash == null
        !r4.payPerRequest
        r4.settlementValidFrom == null
        r4.settlementValidUntil == null
        r4.token == null
    }

    // TODO To - from JSON

    // TODO Complete all Code
}
