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
package org.lightningj.paywall.unitcalculator

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.annotations.PaymentRequired
import org.lightningj.paywall.web.CachableHttpServletRequest
import spock.lang.Specification

/**
 * Unit tests for UnitCalculatorFactory
 *
 * Created by philip on 2018-10-29.
 */
class UnitCalculatorFactorySpec extends Specification {

    UnitCalculatorFactory factory = new UnitCalculatorFactory()

    def "Verify that expected default unit calculator is returned of no unitCalculator attribute is specified in PaymentRequired annotation."(){
        expect:
        factory.getUnitCalculator(findAnnotation("callWithDefaultCalculator")) instanceof DefaultUnitCalculator
    }

    def "Verify that custom request policies are generated properly"(){
        when:
        def custom1 = factory.getUnitCalculator(findAnnotation("callWithCustom1"))
        def custom2 = factory.getUnitCalculator(findAnnotation("callWithCustom2"))
        then:
        custom1 instanceof CustomCalculator1
        custom2 instanceof CustomCalculator2
        factory.customCalculators.size() == 2
        factory.customCalculators[CustomCalculator1] != null
        factory.customCalculators[CustomCalculator2] != null
    }

    def "Verify that InternalErrorException occurs when problem occurred creating UnitCalculator"(){
        when:
        factory.getUnitCalculator(findAnnotation("callWithBadCalculator"))
        then:
        def e = thrown InternalErrorException
        e.message == 'Error constructing custom unit calculator : class org.lightningj.paywall.unitcalculator.UnitCalculatorFactorySpec$BadCalculator, message: Error bad calculator'
    }

    private findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest{

        @PaymentRequired(id= "notused")
        void callWithDefaultCalculator(){}

        @PaymentRequired(id= "notused", unitCalculator = CustomCalculator1)
        void callWithCustom1(){}

        @PaymentRequired(id= "notused", unitCalculator = CustomCalculator2)
        void callWithCustom2(){}

        @PaymentRequired(id= "notused", unitCalculator = BadCalculator)
        void callWithBadCalculator(){}
    }

    static class CustomCalculator1 implements UnitCalculator{
        @Override
        int getUnits(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return 10
        }
    }

    static class CustomCalculator2 implements UnitCalculator{
        @Override
        int getUnits(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return 20
        }
    }

    static class BadCalculator implements UnitCalculator{

        BadCalculator(){
            throw new IOException("Error bad calculator")
        }
        @Override
        int getUnits(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
            return 20
        }
    }

}
