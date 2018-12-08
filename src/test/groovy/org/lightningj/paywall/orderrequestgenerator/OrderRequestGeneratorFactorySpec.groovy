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
import org.lightningj.paywall.vo.OrderRequest
import org.lightningj.paywall.web.CachableHttpServletRequest
import spock.lang.Specification

/**
 * Unit tests for OrderRequestGeneratorFactory
 *
 * Created by philip on 2018-10-29.
 */
class OrderRequestGeneratorFactorySpec extends Specification {

    OrderRequestGeneratorFactory factory = new OrderRequestGeneratorFactory()

    def "Verify that expected default order request generator is returned of no unitCalculator attribute is specified in PaymentRequired annotation."(){
        expect:
        factory.getGenerator(findAnnotation("callWithDefaultCalculator")) instanceof DefaultOrderRequestGenerator
    }

    def "Verify that custom request policies are generated properly"(){
        when:
        def custom1 = factory.getGenerator(findAnnotation("callWithCustom1"))
        def custom2 = factory.getGenerator(findAnnotation("callWithCustom2"))
        then:
        custom1 instanceof CustomOrderRequestGenerator1
        custom2 instanceof CustomOrderRequestGenerator2
        factory.customGenerators.size() == 2
        factory.customGenerators[CustomOrderRequestGenerator1] != null
        factory.customGenerators[CustomOrderRequestGenerator2] != null
    }

    def "Verify that InternalErrorException occurs when problem occurred creating OrderRequestGenerator"(){
        when:
        factory.getGenerator(findAnnotation("callWithBadCalculator"))
        then:
        def e = thrown InternalErrorException
        e.message == 'Error constructing custom order request generator : class org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactorySpec$BadOrderRequestGenerator, message: Error bad OrderRequestGenerator'
    }

    private findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest{

        @PaymentRequired()
        void callWithDefaultCalculator(){}

        @PaymentRequired( orderRequestGenerator = CustomOrderRequestGenerator1)
        void callWithCustom1(){}

        @PaymentRequired( orderRequestGenerator = CustomOrderRequestGenerator2)
        void callWithCustom2(){}

        @PaymentRequired( orderRequestGenerator = BadOrderRequestGenerator)
        void callWithBadCalculator(){}
    }

    static class CustomOrderRequestGenerator1 implements OrderRequestGenerator{
        @Override
        OrderRequest generate(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return 10
        }
    }

    static class CustomOrderRequestGenerator2 implements OrderRequestGenerator{
        @Override
        OrderRequest generate(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return 20
        }
    }

    static class BadOrderRequestGenerator implements OrderRequestGenerator{

        BadOrderRequestGenerator(){
            throw new IOException("Error bad OrderRequestGenerator")
        }
        @Override
        OrderRequest generate(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return new OrderRequest()
        }
    }

}
