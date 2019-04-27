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

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for MinimalInvoice.
 *
 * Created by Philip Vendil on 2018-11-12.
 */
class MinimalInvoiceSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def id1 = new MinimalInvoice()
        then:
        id1.getPreImageHash() == null
        when:
        id1.setPreImageHash("123".getBytes())
        then:
        id1.getPreImageHash() == "123".getBytes()
        when:
        def id2 = genFullMinInvoiceData()
        then:
        id2.getPreImageHash() == "123".getBytes()
    }

    // JWTClaims constructor tested in BaseTokenGeneratorSpec

    def "Verify that toJsonAsString works as expected"(){
        setup:
        def fullInvoice = genFullMinInvoiceData()
        expect:
        fullInvoice.toJsonAsString(false) == """{"preImageHash":"MTIz"}"""

        when:
        new MinimalInvoice().toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key preImageHash is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        MinimalInvoice d = new MinimalInvoice(toJsonObject("""{"preImageHash":"MTIz"}"""))
        then:
        d.preImageHash == "123".getBytes()

        when:
        new Invoice(toJsonObject("""{}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key preImageHash is required."

    }

    def "Verify getClaimName() returns correct value"(){
        expect:
        new MinimalInvoice().getClaimName() == MinimalInvoice.CLAIM_NAME
    }

    static MinimalInvoice genFullMinInvoiceData(){
        return new MinimalInvoice(InvoiceSpec.genFullInvoiceData(false))
    }
}
