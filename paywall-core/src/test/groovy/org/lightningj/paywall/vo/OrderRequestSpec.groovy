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
 * Unit tests for OrderRequest
 *
 * Created by Philip Vendil on 2018-12-08.
 */
class OrderRequestSpec extends Specification {

    def paymentOptions = genPaymentOption(["testoption1": "testvalue1","testoption2": "testvalue2"])

    def "Verify constructors and getter and setters"(){
        when:
        def or1 = new OrderRequest()
        then:
        or1.getArticleId() == null
        or1.getUnits() == 0
        or1.getPaymentOptions() == null
        !or1.isPayPerRequest()
        when:
        or1.setArticleId("someartid")
        or1.setUnits(2)
        or1.setPaymentOptions(paymentOptions)
        or1.setPayPerRequest(true)
        then:
        or1.getArticleId() == "someartid"
        or1.getUnits() == 2
        or1.getPaymentOptions() == paymentOptions
        or1.isPayPerRequest()
        when:
        def or2 = new OrderRequest("someartid",2,paymentOptions, true)
        then:
        or2.getArticleId() == "someartid"
        or2.getUnits() == 2
        or2.getPaymentOptions() == paymentOptions
        or2.isPayPerRequest()
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new OrderRequest().toJsonAsString(false) == """{"units":0,"payPerRequest":false}"""
        new OrderRequest("someartid",2,paymentOptions, true).toJsonAsString(false) == """{"articleId":"someartid","units":2,"paymentOptions":[{"option":"testoption1","value":"testvalue1"},{"option":"testoption2","value":"testvalue2"}],"payPerRequest":true}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        OrderRequest or = new OrderRequest(toJsonObject("""{"articleId":"someartid","units":2,"paymentOptions":[{"option":"testoption1","value":"testvalue1"},{"option":"testoption2","value":"testvalue2"}],"payPerRequest":true}"""))
        then:
        or.getArticleId() == "someartid"
        or.getUnits() == 2
        or.getPaymentOptions().size() == 2
        or.getPaymentOptions()[0].option == "testoption1"
        or.getPaymentOptions()[0].value == "testvalue1"
        or.getPaymentOptions()[1].option == "testoption2"
        or.getPaymentOptions()[1].value == "testvalue2"
        or.isPayPerRequest()
        when:
        or = new OrderRequest(toJsonObject("""{}"""))
        then:
        or.getArticleId() == null
        or.getUnits() == 0
        or.getPaymentOptions() == null
    }

    def "Verify that invalid payment options data throws JsonException"() {
        when:
        new OrderRequest(toJsonObject("""{"articleId":"someartid","units":2,"paymentOptions":{"option":"testoption1","value":"testvalue1"}}"""))
        then:
        thrown JsonException
    }

    def "Verify getClaimName() returns correct clain name"(){
        expect:
        new OrderRequest().getClaimName() == "orderRequest"
    }

    private static List genPaymentOption(Map options){
        List retval = []
        options.keySet().each {
            def po = new PaymentOption()
            po.option = it
            po.value = options[it]
            retval << po
        }
        return retval
    }
}
