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

import org.lightningj.paywall.annotations.PaymentRequired
import spock.lang.Specification

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for PaymentOption
 *
 * Created by Philip Vendil on 2018-12-07.
 */
class PaymentOptionSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def po1 = new PaymentOption()
        then:
        po1.getOption() == null
        po1.getValue() == null
        when:
        po1.setOption("someoption")
        po1.setValue("somevalue")
        then:
        po1.getOption() == "someoption"
        po1.getValue() == "somevalue"

        when:
        def po2 = new PaymentOption(findAnnotation("callWithPaymentOption").paymentOptions()[0])
        then:
        po2.getOption() == "someoption"
        po2.getValue() == "somevalue"
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new PaymentOption().toJsonAsString(false) == "{}"
        genPaymentOption().toJsonAsString(false) == """{"option":"someoption","value":"somevalue"}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        PaymentOption po = new PaymentOption(toJsonObject("""{"option":"someoption","value":"somevalue"}"""))
        then:
        po.getOption() == "someoption"
        po.getValue() == "somevalue"
        when:
        po = new PaymentOption(toJsonObject("""{}"""))
        then:
        po.getOption() == null
        po.getValue() == null

    }

    private PaymentOption genPaymentOption(){
        return new PaymentOption(findAnnotation("callWithPaymentOption").paymentOptions()[0])
    }

    private PaymentRequired findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest {
        @PaymentRequired(
                paymentOptions =  @org.lightningj.paywall.annotations.vo.PaymentOption(option="someoption", value="somevalue"))
        void callWithPaymentOption() {}
    }
}
