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
package org.lightningj.paywall.annotations.vo;

import java.lang.annotation.*;

/**
 * Payment options is a way of addind extra option related to a
 * payment order where the annotation can contain extra information
 * that could be used by the PaymentHandler to create an order.
 *
 * Created by Philip Vendil on 2018-12-07.
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentOption {

    /**
     *
     * @return
     */
    String option();

    /**
     *
     * @return  the options value. Values should be supported
     * by the configured payment handler.
     */
    String value();

}
