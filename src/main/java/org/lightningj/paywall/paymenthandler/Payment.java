package org.lightningj.paywall.paymenthandler;

/**
 * Generic interface of value object that can be handled
 * by the PaymentHandler. Either an Order, Invoice
 * or and Settlement
 * Created by philip on 2018-12-05.
 */
public interface Payment {

    /**
     *
     * @return the pre image hash that is used by the framework to
     * be a unique identifier of a payment flow.
     */
    byte[] getPreImageHash();
}
