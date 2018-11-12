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
package org.lightningj.paywall.annotations;

import org.lightningj.paywall.requestpolicy.RequestPolicy;
import org.lightningj.paywall.requestpolicy.RequestPolicyType;
import org.lightningj.paywall.unitcalculator.DefaultUnitCalculator;
import org.lightningj.paywall.unitcalculator.UnitCalculator;

import java.lang.annotation.*;

/**
 * TODO
 * Created by philip on 2018-09-07.
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentRequired {

    String id();

    int units() default 1;

    Class<? extends UnitCalculator> unitCalculator() default DefaultUnitCalculator.class;

    RequestPolicyType requestPolicy() default RequestPolicyType.WITH_BODY;

    Class customPolicy() default Object.class;

}
