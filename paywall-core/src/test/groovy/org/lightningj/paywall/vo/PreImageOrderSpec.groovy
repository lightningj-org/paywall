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
 * Unit tests for PreImageOrder
 *
 * Created by Philip Vendil on 2018-12-30.
 */
class PreImageOrderSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def pd1 = new PreImageOrder()
        then:
        pd1.getPreImage() == null
        pd1.getPreImageHash() == null
        pd1.getExpireDate() == null
        pd1.getDescription() == null
        pd1.getOrderAmount() == null
        when:
        pd1.setPreImage("234".getBytes())
        pd1.setPreImageHash("123".getBytes())
        pd1.setDescription("SomeDescription")
        pd1.setOrderAmount(new BTC(1234))
        pd1.setExpireDate(Instant.ofEpochMilli(12345L))
        then:
        pd1.getPreImage() == "234".getBytes()
        pd1.getPreImageHash() == "123".getBytes()
        pd1.getExpireDate().toEpochMilli() == 12345L
        pd1.getDescription() == "SomeDescription"
        pd1.getOrderAmount() instanceof BTC
        when:
        def pd2 = new PreImageOrder("234".getBytes(),new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)))
        then:
        pd2.getPreImage() == "234".getBytes()
        pd2.getPreImageHash() == "123".getBytes()
        pd2.getExpireDate().toEpochMilli() == 12345L
        pd2.getDescription() == "SomeDescription"
        pd2.getOrderAmount() instanceof BTC
    }

    def "Verify toPreImageData converts to a correct PreImageData"(){
        setup:
        PreImageOrder pio = new PreImageOrder("234".getBytes(),new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L)))
        when:
        PreImageData pid = pio.toPreImageData()
        then:
        pid.preImage == "234".getBytes()
        pid.preImageHash == "123".getBytes()
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new PreImageOrder("234".getBytes(),new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L))).toJsonAsString(false) == """{"preImageHash":"MTIz","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345,"preImage":"MjM0"}"""
        when:
        new PreImageOrder(null,new Order("123".getBytes(),"SomeDescription",new BTC(1234),Instant.ofEpochMilli(12345L))).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImage is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        PreImageOrder d = new PreImageOrder(toJsonObject("""{"preImageHash":"MTIz","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345,"preImage":"MjM0"}"""))
        then:
        d.preImage == "234".getBytes()
        d.preImageHash == "123".getBytes()
        d.description == "SomeDescription"
        d.orderAmount instanceof CryptoAmount
        d.orderAmount.currencyCode == "BTC"
        d.expireDate == Instant.ofEpochMilli(12345L)

        when:
        new PreImageOrder(toJsonObject("""{"preImageHash":"MTIz","description":"SomeDescription","orderAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"expireDate":12345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImage is required."
    }
}
