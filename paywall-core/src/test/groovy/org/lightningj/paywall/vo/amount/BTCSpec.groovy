package org.lightningj.paywall.vo.amount

import spock.lang.Specification

import javax.json.JsonException

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for BTC amount
 * Created by philip on 2018-11-11.
 */
class BTCSpec extends Specification {

    def "Verify constructors"(){
        when:
        def ca1 = new BTC()
        then:
        ca1.getValue() == 0
        ca1.getCurrencyCode() == BTC.CURRENCY_CODE_BTC
        ca1.getMagnetude() == Magnetude.NONE
        when:
        def ca2 = new BTC(2)
        then:
        ca2.getValue() == 2
        ca2.getCurrencyCode() == BTC.CURRENCY_CODE_BTC
        ca2.getMagnetude() == Magnetude.NONE
        when:
        def ca3 = new BTC(3,Magnetude.MILLI)
        then:
        ca3.getValue() == 3
        ca3.getCurrencyCode() == BTC.CURRENCY_CODE_BTC
        ca3.getMagnetude() == Magnetude.MILLI
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new BTC(3).toJsonAsString(false) == """{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"NONE"}"""
        new BTC(3, Magnetude.MILLI).toJsonAsString(false) == """{"type":"CRYTOCURRENCY","value":3,"currencyCode":"BTC","magnetude":"MILLI"}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        BTC d1 = new BTC(toJsonObject("""{"value":3,"currencyCode":"BTC"}"""))
        then:
        d1.value == 3
        d1.currencyCode == "BTC"
        d1.magnetude == Magnetude.NONE
        when:
        BTC d2 = new BTC(toJsonObject("""{"value":3,"currencyCode":"BTC","magnetude":"MILLI"}"""))
        then:
        d2.value == 3
        d2.currencyCode == "BTC"
        d2.magnetude == Magnetude.MILLI
    }
}
