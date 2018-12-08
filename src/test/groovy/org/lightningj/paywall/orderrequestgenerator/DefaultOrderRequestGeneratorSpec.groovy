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
package org.lightningj.paywall.orderrequestgenerator

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.annotations.PaymentRequired
import org.lightningj.paywall.annotations.vo.PaymentOption
import spock.lang.Specification

/**
 * Unit tests for DefaultOrderRequestGenerator
 * Created by philip on 2018-10-29.
 */
class DefaultOrderRequestGeneratorSpec extends Specification {

    DefaultOrderRequestGenerator orderRequestGenerator = new DefaultOrderRequestGenerator()

    def "Verify that expected data are extracted from DefaultUnitCalculator"(){
        when:
        def or1 = orderRequestGenerator.generate(findAnnotation("callWithDefaultUnits"),null)
        then:
        or1.getArticleId() == "someArticleId"
        or1.getUnits() == 1
        or1.getPaymentOptions().size() == 0
        when:
        def or2 = orderRequestGenerator.generate(findAnnotation("callWith10UnitsAndPaymentOptions"),null)
        then:
        or2.getArticleId() == "someArticleId"
        or2.getUnits() == 10
        or2.getPaymentOptions().size() == 2
        or2.getPaymentOptions()[0].option == "option1"
        or2.getPaymentOptions()[0].value == "value1"
        or2.getPaymentOptions()[1].option == "option2"
        or2.getPaymentOptions()[1].value == "value2"
    }

    def "Verify that InternalErrorException is thrown if no articleId was specified"(){
        when:
        orderRequestGenerator.generate(findAnnotation("callWithNoArticleId"),null)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error in DefaultOrderRequestGenerator, error in PaymentRequired annotation, article id is mandatory."
    }



    private findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest{

        @PaymentRequired()
        void callWithNoArticleId(){}

        @PaymentRequired(articleId = "someArticleId")
        void callWithDefaultUnits(){}

        @PaymentRequired(articleId= "someArticleId", units = 10, paymentOptions = [
            @PaymentOption(option = "option1",value = "value1"), @PaymentOption(option = "option2",value = "value2")])
        void callWith10UnitsAndPaymentOptions(){}

    }
}
