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
 * Unit tests for ConvertedOrder
 * Created by Philip Vendil on 2018-11-11.
 */
class ConvertedOrderSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def pd1 = new ConvertedOrder()
        then:
        pd1.getPreImageHash() == null
        pd1.getExpireDate() == null
        pd1.getDescription() == null
        pd1.getOrderAmount() == null
        pd1.getConvertedAmount() == null
        when:
        pd1.setPreImageHash("123".getBytes())
        pd1.setDescription("SomeDescription")
        pd1.setOrderAmount(new BTC(1234))
        pd1.setExpireDate(Instant.ofEpochMilli(12345L))
        pd1.setConvertedAmount(new BTC(2345))
        then:
        pd1.getPreImageHash() == "123".getBytes()
        pd1.getExpireDate().toEpochMilli() == 12345L
        pd1.getDescription() == "SomeDescription"
        pd1.getOrderAmount() instanceof BTC
        pd1.getConvertedAmount().value == 2345L
        when:
        def pd2 = new ConvertedOrder(new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)),new BTC(2345))
        then:
        pd2.getPreImageHash() == "123".getBytes()
        pd2.getExpireDate().toEpochMilli() == 12345L
        pd2.getDescription() == "SomeDescription"
        pd2.getOrderAmount() instanceof BTC
        pd2.getConvertedAmount().value == 2345L
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new ConvertedOrder(new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)),new BTC(2345)).toJsonAsString(false) == """{"preImageHash":"HXRC","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345,"convertedAmount":{"type":"CRYTOCURRENCY","value":2345,"currencyCode":"BTC","magnetude":"NONE"}}"""
        when:
        new ConvertedOrder(new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)),null).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key convertedAmount is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        ConvertedOrder d = new ConvertedOrder(toJsonObject("""{"preImageHash":"HXRC","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345,"convertedAmount":{"type":"CRYTOCURRENCY","value":2345,"currencyCode":"BTC","magnetude":"NONE"}}"""))
        then:
        d.preImageHash == "123".getBytes()
        d.description == "SomeDescription"
        d.orderAmount instanceof CryptoAmount
        d.orderAmount.currencyCode == "BTC"
        d.expireDate == Instant.ofEpochMilli(12345L)
        d.getConvertedAmount().value == 2345L

        when:
        new ConvertedOrder(toJsonObject("""{"preImageHash":"HXRC","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key convertedAmount is required."
    }
}
