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
 * Unit tests for CryptoAmount
 *
 * Created by Philip Vendil on 2018-11-11.
 */
class CryptoAmountSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def ca1 = new CryptoAmount()
        then:
        ca1.getValue() == 0
        ca1.getCurrencyCode() == null
        ca1.getMagnetude() == Magnetude.NONE
        when:
        ca1.setValue(3)
        ca1.setCurrencyCode("BTC")
        ca1.setMagnetude(Magnetude.MILLI)
        then:
        ca1.getValue() == 3
        ca1.getCurrencyCode() == "BTC"
        ca1.getMagnetude() == Magnetude.MILLI
        when:
        def ca2 = new CryptoAmount(2,"LTC")
        then:
        ca2.getValue() == 2
        ca2.getCurrencyCode() == "LTC"

        when:
        def ca3 = new CryptoAmount(4,"LTC")
        then:
        ca3.getValue() == 4
        ca3.getCurrencyCode() == "LTC"
        ca3.getMagnetude() == Magnetude.NONE

        when:
        def ca4 = new CryptoAmount(5,"BTC", Magnetude.MILLI)
        then:
        ca4.getValue() == 5
        ca4.getCurrencyCode() == "BTC"
        ca4.getMagnetude() == Magnetude.MILLI
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new CryptoAmount(3,"BTC").toJsonAsString(false) == """{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"NONE"}"""
        new CryptoAmount(3,"BTC", Magnetude.MILLI).toJsonAsString(false) == """{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"MILLI"}"""
        when:
        new CryptoAmount(3,null).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key currencyCode is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        CryptoAmount d1 = new CryptoAmount(toJsonObject("""{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC"}"""))
        then:
        d1.value == 3
        d1.currencyCode == "BTC"
        d1.magnetude == Magnetude.NONE
        when:
        CryptoAmount d2 = new CryptoAmount(toJsonObject("""{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"MILLI"}"""))
        then:
        d2.value == 3
        d2.currencyCode == "BTC"
        d2.magnetude == Magnetude.MILLI
        when:
        new CryptoAmount(toJsonObject("""{"value":2}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key currencyCode is required."

        when:
        new CryptoAmount(toJsonObject("""{"value":3,"currencyCode":"BTC","magnetude":"INVALID"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, Invalid value INVALID for json key magnetude."
    }
}
