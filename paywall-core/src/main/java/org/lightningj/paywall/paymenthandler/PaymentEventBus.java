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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Helper class in charge of notifying listeners matching
 * interested events after a payment event have been
 * triggered.
 *
 * Created by Philip Vendil on 2018-12-04.
 */
public class PaymentEventBus {

    protected List<PaymentListener> listeners = new CopyOnWriteArrayList(new ArrayList<PaymentListener>());

    /**
     * Method to add the listener to the set of listeners listening
     * on payment events.
     * @param listener the listener to register.
     */
    void registerListener(PaymentListener listener){
        listeners.add(listener);
    }

    /**
     * Method to remove the listener from the set of listeners.
     * @param listener the listener to remove.
     */
    void unregisterListener(PaymentListener listener){
        listeners.remove(listener);
    }

    /**
     * Method to send notification to all matching listeners. It also
     * automatically removes all listeners that have unregisterAfterEvent flag
     * set.
     * @param type the type of event to trigger
     * @param payment the payment value object either Order, Invoice or Settlement.
     */
    void triggerEvent(PaymentEventType type, Payment payment){
        PaymentEvent event = new PaymentEvent(type,payment);

        List<PaymentListener> unregisterListeners = new ArrayList<>();
        for(PaymentListener listener : listeners){
            if(matches(listener,event)){
                listener.onPaymentEvent(event);
            }
            if(listener.unregisterAfterEvent()){
                unregisterListeners.add(listener);
            }
        }

        listeners.removeAll(unregisterListeners);
    }

    private boolean matches(PaymentListener listener, PaymentEvent event){
        if(listener.getPreImageHash() != null &&
                !Arrays.equals(listener.getPreImageHash(), event.getPayment().getPreImageHash())){
            return false;
        }
        if(listener.getType() != PaymentEventType.ANY_TYPE && listener.getType() != event.getType()){
            return false;
        }

        return true;
    }
}
