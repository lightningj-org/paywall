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
package org.lightningj.paywall.paymentflow


import org.lightningj.paywall.vo.Settlement
import spock.lang.Specification

import java.time.Instant

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for SettlementResult.
 *
 * Created by Philip Vendil on 2018-12-29.
 */
class SettlementResultSpec extends Specification {

    def settlement = new Settlement("abasrekwsdf".getBytes(),null, Instant.ofEpochMilli(12340000),null, true)

    def "Verify constructors and getter and setters"(){
        when:
        def sr1 = new SettlementResult()
        then:
        sr1.getSettlement() == null
        sr1.getToken() == null

        when:
        sr1.setSettlement(settlement)
        sr1.setToken("sometokendata")

        then:
        sr1.getSettlement() instanceof Settlement
        sr1.getToken() == "sometokendata"

        when:
        def sr2 = new SettlementResult(settlement, "sometokendata")
        then:
        sr2.getSettlement() instanceof Settlement
        sr2.getToken() == "sometokendata"
    }


    def "Verify that toJsonAsString works as expected"(){
        expect:
        new SettlementResult(settlement, "sometokendata").toJsonAsString(false) == """{"settlement":{"preImageHash":"YWJhc3Jla3dzZGY=","validUntil":12340000,"payPerRequest":true},"token":"sometokendata"}"""
        new SettlementResult(null,null).toJsonAsString(false) == "{}"
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        SettlementResult d = new SettlementResult(toJsonObject("""{"settlement":{"preImageHash":"YWJhc3Jla3dzZGY=","validUntil":12340000,"payPerRequest":true},"token":"sometokendata"}"""))
        then:
        d.getSettlement() instanceof Settlement
        d.getToken() == "sometokendata"

        when:
        SettlementResult d2 = new SettlementResult(toJsonObject("{}"))
        then:
        d2.getSettlement() == null
        d2.getToken() == null
    }
}
