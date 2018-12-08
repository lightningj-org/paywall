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
 * Interface for a listener listening on payment updates related to
 * a specific payment preImageHash (id in the system) or all payment events.
 *
 * Created by Philip Vendil on 2018-12-04.
 */
public interface PaymentListener {

    /**
     *
     * @return the pre image hash of the payment that the listener is interested in, null if all events
     * should be signaled.
     */
    byte[] getPreImageHash();

    /**
     *
     * @return the type of event the listener is interested in, use PaymentEventType.ANY_TYPE to receive
     * notification for any type.
     */
    PaymentEventType getType();

    /**
     *
     * @return flag indicating that the this listener should unregister itself after first matching
     * event have been triggered. This to avoid manual unregistration.
     */
    boolean unregisterAfterEvent();

    /**
     * This method every time an and event related to a payment have been triggered.
     * @param event the related payment event.
     * @see PaymentEvent
     */
    void onPaymentEvent(PaymentEvent event);
}
