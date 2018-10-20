/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
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
        Token t1 = new Token("token1", Instant.ofEpochMilli(123L), MERCHANT)
        then:
        t1.token == "token1"
        t1.expireDate == Instant.ofEpochMilli(123L)
        t1.facade == MERCHANT
        when:
        Token t2 = new Token()
        t2.token = "token2"
        t2.expireDate = Instant.ofEpochMilli(124L)
        t2.facade = POS
        then:
        t2.token == "token2"
        t2.expireDate == Instant.ofEpochMilli(124L)
        t2.facade == POS
        when:
        Token t3 = new Token("token1",Instant.ofEpochMilli(123L), MERCHANT)
        Token t4 = new Token("token4",Instant.ofEpochMilli(123L), MERCHANT)

        then:
        t1 == t3
        t1 != t2
        t1 != t4

        t1.hashCode() == t3.hashCode()
        t1.hashCode() != t2.hashCode()
        t1.hashCode() != t4.hashCode()

    }

    def "Verify toString()"(){
        expect:
        new Token("token1",Instant.ofEpochMilli(123L), MERCHANT).toString() == """
{
    "token": "token1",
    "expireDate": 123,
    "facade": "MERCHANT"
}"""
    }

    def "Verify convertToJson"(){
        expect:
        new Token().toJsonAsString(false) == "{}"
        new Token("token1",Instant.ofEpochMilli(123L), MERCHANT).toJsonAsString(false) == """{"token":"token1","expireDate":123,"facade":"MERCHANT"}"""
    }

    def "Verify parseJson"(){
        when:
        Token t = new Token(toJsonObject("{ }"))
        then:
        t.token == null
        when: // Simple Invoice
        t = new Token(toJsonObject("""{"token":"token1","expireDate":123,"facade":"MERCHANT"}"""), )
        then:
        t.token == "token1"
        t.expireDate == Instant.ofEpochMilli(123L)
        t.facade == BTCPayServerFacade.MERCHANT

    }
}
