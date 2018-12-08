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
package org.lightningj.paywall.paymenthandler;

/**
 * Enumeration describing the type of payment event that is signaled.
 *
 * Created by Philip Vendil on 2018-12-04.
 */
public enum PaymentEventType {
    /**
     * Event signaling a order was created.
     */
    ORDER_CREATED,
    /**
     * Event signaling that a payments invoice have been created by lightning handler.
     */
    INVOICE_CREATED,
    /**
     * Event signaling that a invoice have been settled.
     */
    INVOICE_SETTLED;

    /**
     * Special constant indicating ANY type is applicable.
     */
    public static final PaymentEventType ANY_TYPE = null;
}
