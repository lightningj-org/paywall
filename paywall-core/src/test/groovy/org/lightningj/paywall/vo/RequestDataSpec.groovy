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

import spock.lang.Specification

import javax.json.JsonException
import java.time.Duration
import java.time.Instant

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for RequestData
 * Created by Philip Vendil on 2018-11-23.
 */
class RequestDataSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def rd1 = new RequestData()
        then:
        rd1.getSignificantData() == null
        rd1.getRequestDate() == null

        when:
        rd1.setSignificantData("123".getBytes())
        rd1.setRequestDate(Instant.ofEpochMilli(12345L))

        then:
        rd1.getSignificantData() == "123".getBytes()
        rd1.getRequestDate().toEpochMilli() == 12345L

        when:
        def sd2 = new RequestData("123".getBytes(),Instant.ofEpochMilli(12345L))
        then:
        sd2.getSignificantData() == "123".getBytes()
        rd1.getRequestDate().toEpochMilli() == 12345L
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new RequestData("123".getBytes(),Instant.ofEpochMilli(12345L)).toJsonAsString(false) == """{"significantData":"HXRC","requestDate":12345}"""
        when:
        new RequestData(null,Instant.ofEpochMilli(12345L)).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key significantData is null."
        when:
        new RequestData("123".getBytes(),null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key requestDate is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        RequestData d = new RequestData(toJsonObject("""{"significantData":"HXRC","requestDate":12345}"""))
        then:
        d.getSignificantData() == "123".getBytes()
        d.getRequestDate().toEpochMilli() == 12345L

        when:
        new RequestData(toJsonObject("""{"requestDate":12345}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key significantData is required."

        when:
        new RequestData(toJsonObject("""{"significantData":"HXRC"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key requestDate is required."
        when:
        new RequestData(toJsonObject("""{"significantData":"HXRC","requestDate":"abc"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key requestDate is not a number."

    }

    def "Verify that equals and hashcode uses significantData only"(){
        setup:
        RequestData d1 = new RequestData("123".getBytes(), Instant.now().plus(Duration.parse("PT1M")))
        RequestData d2 = new RequestData("123".getBytes(), Instant.now().plus(Duration.parse("PT5M")))
        RequestData d3 = new RequestData("234".getBytes(), Instant.now().plus(Duration.parse("PT5M")))
        expect:
        d1 == d2
        d1 != d3
        d1.hashCode() == d2.hashCode()
        d1.hashCode() != d3.hashCode()
    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new RequestData().getClaimName() == RequestData.CLAIM_NAME
    }
}
