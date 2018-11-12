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
package org.lightningj.paywall.vo.amount

import spock.lang.Specification

import javax.json.JsonException

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
/**
 * Unit tests for Amount
 *
 * Created by Philip Vendil on 2018-11-11.
 */
class AmountSpec extends Specification {

    def "Verify getType() returns type"(){
        expect:
        new BTC(123).getType() == AmountType.CRYTOCURRENCY
    }

    def "Verify that parseAmountObject converts json amount object into correct amount type"(){
        when:
        FiatAmount fa = Amount.parseAmountObject(toJsonObject("""{"type":"FIAT","value":1.2,"currencyCode":"USD"}"""))
        then:
        fa.currencyCode == "USD"
        fa.value == 1.2

        when:
        CryptoAmount ca = Amount.parseAmountObject(toJsonObject("""{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"NONE"}"""))
        then:
        ca.value == 3
        ca.currencyCode == "BTC"

        when:
        Amount.parseAmountObject(toJsonObject("""{"value":1.2,"currencyCode":"USD"}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON, no type field set in amount."

        when:
        Amount.parseAmountObject(toJsonObject("""{"type": "INVALID","value":1.2,"currencyCode":"USD"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON, invalid amount type INVALID."

    }
}
