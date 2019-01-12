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
 * Abstract base class for a BasePaymentListener so extending classes only need to
 * implement onPaymentEvent()
 *
 * Created by Philip Vendil on 2018-12-05.
 */
public abstract class BasePaymentListener implements PaymentListener {

    private byte[] preImageHash;
    private PaymentEventType type;
    private boolean unregisterAfterEvent;

    /**
     * Default Constructor.
     *
     * @param preImageHash the pre image hash of the payment that the listener is interested in, null if all events
     * should be signaled.
     * @param type the type of event the listener is interested in, use PaymentEventType.ANY_TYPE to receive
     * notification for any type.
     * @param unregisterAfterEvent flag indicating that the this listener should unregister itself after first matching
     * event have been triggered. This to avoid manual unregistration.
     */
    public BasePaymentListener(byte[] preImageHash, PaymentEventType type, boolean unregisterAfterEvent){
        this.preImageHash = preImageHash;
        this.type = type;
        this.unregisterAfterEvent = unregisterAfterEvent;
    }
    /**
     * @return the pre image hash of the payment that the listener is interested in, null if all events
     * should be signaled.
     */
    @Override
    public byte[] getPreImageHash() {
        return preImageHash;
    }

    /**
     * @return the type of event the listener is interested in, use PaymentEventType.ANY_TYPE to receive
     * notification for any type.
     */
    @Override
    public PaymentEventType getType() {
        return type;
    }

    /**
     * @return flag indicating that the this listener should unregister itself after first matching
     * event have been triggered. This to avoid manual unregistration.
     */
    @Override
    public boolean unregisterAfterEvent() {
        return unregisterAfterEvent;
    }
}
