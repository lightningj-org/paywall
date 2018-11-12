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

import org.lightningj.paywall.annotations.PaymentRequired
import spock.lang.Specification

/**
 * Unit tests for DefaultUnitCalculator
 * Created by philip on 2018-10-29.
 */
class DefaultUnitCalculatorSpec extends Specification {

    DefaultUnitCalculator unitCalculator = new DefaultUnitCalculator()

    def "Verify that expect units are extracted from DefaultUnitCalculator"(){
        expect:
        unitCalculator.getUnits(findAnnotation("callWithDefaultUnits"),null) == 1
        unitCalculator.getUnits(findAnnotation("callWith10Units"),null) == 10
    }

    private findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest{

        @PaymentRequired(id= "notused")
        void callWithDefaultUnits(){}

        @PaymentRequired(id= "notused", units = 10)
        void callWith10Units(){}


    }
}
