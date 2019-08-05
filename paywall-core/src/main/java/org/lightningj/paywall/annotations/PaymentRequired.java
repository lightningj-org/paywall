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
import org.lightningj.paywall.requestpolicy.NoCustomRequestPolicy;
import org.lightningj.paywall.requestpolicy.RequestPolicy;
import org.lightningj.paywall.requestpolicy.RequestPolicyType;
import org.lightningj.paywall.orderrequestgenerator.DefaultOrderRequestGenerator;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGenerator;

import java.lang.annotation.*;

/**
 * Main annotation used on a WebService of WebPage controller or similar endpoint
 * to indicate that payment is required to allow access.
 * <p>
 * It contains a number of options:
 * <ul>
 *     <li>articleId, determines the type of order that should be generated, used by PaymentHandler to determine order amount depending
 *                 on article an units. (Required if not a custom OrderRequestGenerator is used).
 *     <li>units, The number of units for given article number, default is 1
 *     <li>payPerRequest, if payment is valid for one request only. (Usually for WebService Request)
 *     <li>orderRequestGenerator, possibility to specify a custom order request generator, instead of the default one using
 *      the articleId and units to request an order.
 *     <li>requestPolicy, defining what in HTTP request that is considered relevant for determining a unique payment.
 *     <li>customPolicy, if the redefined request policy types isn't applicable and a custom implementation is necessary.
 *     <li>paymentOptions, extra options that might be needed by PaymentHandler to generate an Order (first type in a payment flow).
 * </ul>
 * <p>
 * Created by Philip Vendil on 2018-09-07.
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentRequired {

    /**
     *
     * @return determines the type of order that should be generated, used by PaymentHandler to determine order amount depending
     * on article an units. (Required if not a custom OrderRequestGenerator is used).
     */
    String articleId() default "";

    /**
     *
     * @return The number of units for given article number, default is 1
     */
    int units() default 1;

    /**
     *
     * @return if payment is valid for one request only. (Usually for WebService Request)
     */
    boolean payPerRequest() default false;

    /**
     * @see OrderRequestGenerator
     *
     * @return possibility to specify a custom order request generator, instead of the default one using
     *      the articleId and units to request an order.
     */
    Class<? extends OrderRequestGenerator> orderRequestGenerator() default DefaultOrderRequestGenerator.class;

    /**
     * @see RequestPolicyType
     *
     * @return defining what in HTTP request that is considered relevant for determining a unique payment.
     */
    RequestPolicyType requestPolicy() default RequestPolicyType.WITH_BODY;

    /**
     * @see RequestPolicy
     * @return custom class if none of the predefined request policy types isn't applicable and a custom implementation is necessary.
     */
    Class<? extends RequestPolicy> customPolicy() default NoCustomRequestPolicy.class;

    /**
     * @see PaymentOption
     * @return extra options that might be needed by PaymentHandler to generate an Order (first type in a payment flow).
     */
    PaymentOption[] paymentOptions() default {};

}
