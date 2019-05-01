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
import spock.lang.Specification

import java.time.Instant

/**
 * Unit tests for SettlementResponse
 */
class SettlementResponseSpec extends Specification {

    SettlementResult settlementResult

    def setup() {
        Settlement settlement = new Settlement("abc".getBytes(), null, Instant.ofEpochMilli(10000), Instant.ofEpochMilli(5000), true)
        settlementResult = new SettlementResult(settlement, "SomeToken")
    }

    def "Verify constructors and getter and setters"() {
        when:
        SettlementResponse r = new SettlementResponse(settlementResult)
        then:
        r.settled
        r.preImageHash == "YWJj"
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
        new SettlementResponse(settlementResult).toString() == "SettlementResponse{preImageHash='YWJj', token='SomeToken', settlementValidUntil=Thu Jan 01 01:00:10 CET 1970, settlementValidFrom=Thu Jan 01 01:00:05 CET 1970, payPerRequest=true, settled=true}"
    }
}
