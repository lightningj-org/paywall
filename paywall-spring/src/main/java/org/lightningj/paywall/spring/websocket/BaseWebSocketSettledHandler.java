/*
 * ***********************************************************************
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
package org.lightningj.paywall.spring.websocket;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.paymenthandler.PaymentListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

/**
 *
 * Base implement a WebSocket specific implementation when listening for settled event.
 * <p>
 *     Different implementations can be used depending on payment flow mode.
 * </p>
 * The class maintains a map of registered event listeners and cleans up itself after related
 * invoice have expired. Regular usage should unregister itself when disconnecting.
 *
 * @author Philip Vendil 2019-05-15
 */
public abstract class BaseWebSocketSettledHandler<T> {

    Logger log = Logger.getLogger(BaseWebSocketSettledHandler.class.getName());

    private static final int CLEANUP_SIZE = 100;

    Clock clock = Clock.systemDefaultZone();

    /**
     * Map of base64 paymentHash to paymentListener
     */
    protected Map<String,T> paymentListenerMap = new ConcurrentHashMap<>();

    /**
     * Priority queue of expiring date to paymentHash, in order to cleanup expiring listeners.
     */
    protected PriorityBlockingQueue<ExpirableListener> expiringListeners = new PriorityBlockingQueue<>();


    /**
     * Method to register a payment listener to a related preImageHash and expireDate.
     * Will perform a cleanup of expired listeners when it's ready.
     *
     * Method will only be added if not already registered.
     *
     * @param preImageHash the related preImageHash in Base64
     * @param expireDate the expireDate of the listener, should usually be the invoice expire date.
     * @param paymentListener the payment listener to register.
     * @throws InternalErrorException if internal exception occurred registering the payment listener.
     */
    public void registerPaymentListener(String preImageHash, long expireDate, T paymentListener) throws InternalErrorException {
        if(!hasPaymentListener(preImageHash)) {
            registerListener(paymentListener);
            paymentListenerMap.put(preImageHash, paymentListener);
            expiringListeners.add(new ExpirableListener(preImageHash, expireDate));
            cleanupInternalResources();
        }
    }

    /**
     * Method that returns true if related paymentListener is registered.
     * @param preImageHash the related preImageHash in Base64
     * @return true if already registered.
     */
    public boolean hasPaymentListener(String preImageHash){
        return paymentListenerMap.containsKey(preImageHash);
    }

    /**
     * Method that unregister the related payment listener and releases all related resources.
     * <p>
     *     This method won't release data from expireQueue, it will clean itself up automatically over time.
     * </p>
     * @param preImageHash the related PreImageHash in base64
     * @throws InternalErrorException if internal problems occurred releasing the payment listener.
     */
    public void unregisterPaymentListener(String preImageHash) throws InternalErrorException {
        T listener = paymentListenerMap.get(preImageHash);
        if(listener != null){
            unregisterListener(listener);
            paymentListenerMap.remove(preImageHash);
        }
    }

    /**
     * Abstract method to register in related event handler.
     * @param listener the event listener to register, depending on type.
     * @throws InternalErrorException thrown by underlying event handler.
     */
    protected abstract void registerListener(T listener) throws InternalErrorException;
    /**
     * Abstract method to unregister in related event handler.
     * @param listener the event listener to unregister, depending on type.
     * @throws InternalErrorException thrown by underlying event handler.
     */
    protected abstract void unregisterListener(T listener) throws InternalErrorException;

    /**
     * Private method that checks if it is time to cleanup old listeners, this
     * is done every time the current map size is a multiple of 100 to avoid performing
     * a cleanup for every new call.
     */
    private void cleanupInternalResources(){
        if(paymentListenerMap.size() % CLEANUP_SIZE == 0){
            // listenerMap is large enough to start a cleanup thread.
            Thread t = new Thread(new ExpiredEntriesRemover());
            t.start();
        }
    }

    /**
     * Inner class used in paymentListenerMap in order to have the listeners expirable.
     */
    private static class ExpirableListener implements Comparable<ExpirableListener> {

        /**
         * expireDate in milliseconds
         */
        long expireDate;
        /**
         * Related PreImageHash in base64
         */
        String preImageHash;

        /**
         * Contructor that should be used when adding an entry to map.
         * @param preImageHash related PreImageHash in base64
         * @param expireDate  in milliseconds
         */
        ExpirableListener(String preImageHash, long expireDate){
            this.preImageHash = preImageHash;
            this.expireDate = expireDate;
        }


        @Override
        public String toString() {
            return "ExpirableObject{" +
                    "expireDate=" + expireDate +
                    ", preImageHash='" + preImageHash + '\'' +
                    '}';
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(ExpirableListener o) {
            return Long.compare(expireDate, o.expireDate);
        }

    }

    /**
     * Thread to asynchronously remove all expired entries from the
     * listeners.
     */
    private class ExpiredEntriesRemover implements Runnable{


        /**
         * Thread to asynchronously remove all expired entries from the
         * listeners.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            boolean foundNonExpired = false;
            while(!foundNonExpired && expiringListeners.size() > 0) {
                ExpirableListener expirableKey = expiringListeners.peek();
                if (expirableKey.expireDate < clock.millis()) {
                    expirableKey = expiringListeners.poll();
                    T listener = paymentListenerMap.get(expirableKey.preImageHash);
                    if(listener != null) {
                        paymentListenerMap.remove(expirableKey.preImageHash);
                        try {
                            unregisterListener(listener);
                        } catch (InternalErrorException e) {
                            log.severe("Internal error when cleaning up expired PaymentListeners: " + e.getMessage());
                        }
                    }
                } else {
                    foundNonExpired = true;
                }
            }
        }
    }


}
