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
package org.lightningj.paywall.btcpayserver.vo

import org.lightningj.paywall.btcpayserver.BTCPayServerFacade
import org.lightningj.paywall.btcpayserver.vo.Token
import spock.lang.Specification

import java.time.Instant

import static org.lightningj.paywall.btcpayserver.BTCPayServerFacade.*

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
/**
 * Unit tests for Token
 *
 * Created by Philip Vendil on 2018-10-13.
 */
class TokenSpec extends Specification {

    def "Verify constructors, getter, setters, hashcode and equals"(){
        when:
        Token t1 = new Token("someid", "token1", Instant.ofEpochMilli(123L), MERCHANT)
        then:
        t1.id == "someid"
        t1.token == "token1"
        t1.expireDate == Instant.ofEpochMilli(123L)
        t1.facade == MERCHANT
        when:
        Token t2 = new Token()
        t2.token = "token2"
        t2.expireDate = Instant.ofEpochMilli(124L)
        t2.facade = POS
        t2.pairingCode = "1234"
        then:
        t2.token == "token2"
        t2.expireDate == Instant.ofEpochMilli(124L)
        t2.facade == POS
        t2.pairingCode == "1234"

        when:
        Token t3 = new Token("someid","token1",Instant.ofEpochMilli(123L), MERCHANT)
        Token t4 = new Token("someid2","token1",Instant.ofEpochMilli(123L), MERCHANT)
        Token t5 = new Token("someid","token4",Instant.ofEpochMilli(123L), MERCHANT)

        then:
        t1 == t3
        t1 != t2
        t1 != t4
        t1 != t5

        t1.hashCode() == t3.hashCode()
        t1.hashCode() != t2.hashCode()
        t1.hashCode() != t4.hashCode()
        t1.hashCode() != t5.hashCode()

    }

    def "Verify toString()"(){
        expect:
        new Token("someid","token1",Instant.ofEpochMilli(123L), MERCHANT).toString() == """
{
    "id": "someid",
    "token": "token1",
    "expireDate": 123,
    "facade": "merchant"
}"""
    }

    def "Verify convertToJson"(){
        expect:
        new Token().toJsonAsString(false) == "{}"
        new Token("someId","token1",Instant.ofEpochMilli(123L), MERCHANT).toJsonAsString(false) == """{"id":"someId","token":"token1","expireDate":123,"facade":"merchant"}"""
        genFullToken().toJsonAsString(false) == """{"id":"someId","token":"token1","expireDate":123,"facade":"merchant","pairingCode":"1234123","label":"some label"}"""


    }

    def "Verify parseJson"(){
        when:
        Token t = new Token(toJsonObject("{ }"))
        then:
        t.token == null
        when: // Simple Invoice
        t = new Token(toJsonObject("""{"id":"someId","token":"token1","expireDate":123,"facade":"MERCHANT"}"""))
        then:
        t.id == "someId"
        t.token == "token1"
        t.expireDate == Instant.ofEpochMilli(123L)
        t.facade == BTCPayServerFacade.MERCHANT

        when:
        t =  new Token(toJsonObject("""{"id":"someId","token":"token1","expireDate":123,"facade":"MERCHANT","pairingCode":"1234123","label":"some label"}"""), )
        then:
        t.id == "someId"
        t.token == "token1"
        t.expireDate == Instant.ofEpochMilli(123L)
        t.facade == BTCPayServerFacade.MERCHANT
        t.pairingCode == "1234123"
        t.label == "some label"

    }

    private Token genFullToken(){
        Token t = new Token("someId","token1",Instant.ofEpochMilli(123L), MERCHANT)
        t.pairingCode = "1234123"
        t.label = "some label"
        return t
    }
}
