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
import spock.lang.Specification

import javax.json.JsonException
import java.time.Instant

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for PreImageData
 *
 * Created by Philip Vendil on 2018-10-29.
 */
class PreImageDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def pd1 = new PreImageData()
        then:
        pd1.getPreImageHash() == null
        pd1.getPreImage() == null
        when:
        pd1.setPreImageHash("123".getBytes())
        pd1.setPreImage("234".getBytes())
        then:
        pd1.getPreImageHash() == "123".getBytes()
        pd1.getPreImage() == "234".getBytes()

        when:
        def pd2 = genPreImageData()
        then:
        pd2.getPreImageHash() == "321".getBytes()
        pd2.getPreImage() == "123".getBytes()
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new PreImageData(null, "321".getBytes()).toJsonAsString(false) == """{"preImageHash":"JCP2"}"""
        genPreImageData().toJsonAsString(false) == """{"preImage":"HXRC","preImageHash":"JCP2"}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        PreImageData d = new PreImageData(toJsonObject("""{"preImageHash":"HXRC"}"""))
        then:
        d.preImage == null
        d.preImageHash == "123".getBytes()
        when:
        d = new PreImageData(toJsonObject("""{"preImage":"HXRC","preImageHash":"JCP2"}"""))
        then:
        new String(d.preImage) == "123"
        new String(d.preImageHash) == "321"
        when:
        new PreImageData(toJsonObject("""{}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

    }

    private PreImageData genPreImageData(){
        def d = new PreImageData("123".getBytes(),"321".getBytes())
        return d
    }
}
