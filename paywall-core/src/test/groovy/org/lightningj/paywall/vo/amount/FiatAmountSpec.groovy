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
 * Unit tests for FiatAmount.
 *
 * Created by Philip Vendil on 2018-11-11.
 */
class FiatAmountSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def fa1 = new FiatAmount()
        then:
        fa1.getValue() == 0.0
        fa1.getCurrencyCode() == null
        when:
        fa1.setValue(1.3)
        fa1.setCurrencyCode("USD")
        then:
        fa1.getValue() == 1.3
        fa1.getCurrencyCode() == "USD"
        when:
        def fa2 = new FiatAmount(1.2,"USD")
        then:
        fa2.getValue() == 1.2
        fa2.getCurrencyCode() == "USD"
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new FiatAmount(1.2,"USD").toJsonAsString(false) == """{"type":"FIAT","value":1.2,"currencyCode":"USD"}"""
        when:
        new FiatAmount(1.2,null).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key currencyCode is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        FiatAmount d = new FiatAmount(toJsonObject("""{"value":1.2,"currencyCode":"USD"}"""))
        then:
        d.value == 1.2
        d.currencyCode == "USD"
        when:
        new FiatAmount(toJsonObject("""{"value":1.2}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key currencyCode is required."

        when:
        new FiatAmount(toJsonObject("""{"currencyCode":"USD"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key value is required."
    }
}
