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

import org.lightningj.paywall.annotations.vo.PaymentOption;
import org.lightningj.paywall.requestpolicy.RequestPolicyType;
import org.lightningj.paywall.orderrequestgenerator.DefaultOrderRequestGenerator;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGenerator;

import java.lang.annotation.*;

/**
 * TODO
 * Created by philip on 2018-09-07.
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentRequired {

    String articleId() default "";

    int units() default 1;

    boolean payPerRequest() default false;

    Class<? extends OrderRequestGenerator> orderRequestGenerator() default DefaultOrderRequestGenerator.class;

    RequestPolicyType requestPolicy() default RequestPolicyType.WITH_BODY;

    Class customPolicy() default Object.class;

    PaymentOption[] paymentOptions() default {};

}
