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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.LightningHandlerContext;
import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext;
import org.lightningj.paywall.vo.Invoice;
import org.lightningj.paywall.vo.Order;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.vo.Settlement;

import java.io.IOException;

/**
 * Interface for a PaymentHandler handling orders, invoices and settlements.
 * <p>
 * All payment flows need a custom implementations of a payment handler persisting
 * generated invoices and constructing orders from an order request constructed
 * from a WebService with PaymentRequired annotation.
 * <p>
 * It is recommended to implement a class that inherits the BasePaymentHandler
 * that takes care of most of the logic.
 * <p>
 * Created by Philip Vendil on 2018-12-04.
 */
public interface PaymentHandler {

    /**
     * Method that is called during the startup the application to
     * set up all required sub components of the PaymentHandler.
     *
     * @throws InternalErrorException if internal error occurred setting
     * up the PaymentHandler.
     */
    void init() throws InternalErrorException;

    /**
     * Method to create an order from a preImageHash and and order request.
     * The implementation should lookup the article id and return an order
     * with all required fields set.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @param orderRequest the specification of the order that should be created calculated
     *                     from data in the PaymentRequired annotation.
     * @return a newly created order used to create a lightning invoice in LightningHandler.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred creating the order.
     */
    Order createOrder(byte[] preImageHash, OrderRequest orderRequest) throws IOException,InternalErrorException;// TODO check

    /**
     * Method to lookup an invoice in the PaymentHandler. The payment handler might
     * call the LightningHandler to get complementary data about the invoice.
     *
     * @param preImageHash the unique preImageHash used to identify an invoice
     *                     withing a lightning payment.
     * @return the related invoice or null if no invoice with related preImageHash found.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred looking up the invoice.
     */
    Invoice lookupInvoice(byte[] preImageHash) throws IOException,InternalErrorException;

    /**
     * Method to check if a given preImageHash is settled and returns a settlement value
     * object if invoice is settled.
     * @param preImageHash the preImageHash of the invoice to check.
     * @param includeInvoice if a invoice should be included in the settlement response. This might
     *                       consume more resources.
     * @return a settlement value object of related preImageHash if invoice is settled, otherwise null.
     * @throws IllegalArgumentException if related payment is per request and is already executed.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred looking up the settlement.
     */
    Settlement checkSettlement(byte[] preImageHash, boolean includeInvoice) throws IllegalArgumentException, IOException,InternalErrorException;

    /**
     * Method to manually register an invoice as settled (isSettled must be set to true) used
     * in some payment flows instead of listening on lightningHandler events.
     *
     * @param settledInvoice a settled invoice that should be registered in the payment handler.
     * @param registerNew if a new payment data should be created if no prior object existed for related
     *                    preImageHash.
     * @param orderRequest the specification of the order that should be created calculated
     *                     from data in the PaymentRequired annotation. Only used if new payment data needs to be
     *                     registered.
     * @param context the latest known state of the lightning handler. Null if no known state exists.
     * @return a settlement value object for the given settledInvoice.
     * @throws IllegalArgumentException if settled invoices preImageHash does exists as payment data and registerNew is false.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException  if internal exception occurred registering a settled invoice.
     */
    Settlement registerSettledInvoice(Invoice settledInvoice, boolean registerNew, OrderRequest orderRequest,
                                      LightningHandlerContext context) throws IllegalArgumentException,IOException,InternalErrorException;

    /**
     * Method to flag a related payment flow is a pay per request type and has been processed successfully.
     * @param preImageHash the preImageHash of the payment to mark as processed.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred updating the payment or no related payment found.
     */
    void markAsExecuted(byte[] preImageHash) throws IOException, InternalErrorException;

    /**
     * Method to add the listener to the set of listeners listening
     * on payment events.
     * @param listener the listener to register.
     * @throws InternalErrorException  if internal exception occurred registering the listener.
     */
    void registerListener(PaymentListener listener) throws InternalErrorException;

    /**
     * Method to remove the listener to the set of listeners listening
     * on payment events.
     * @param listener the listener to un-register.
     * @throws InternalErrorException if internal exception occurred un-registering the listener.
     */
    void unregisterListener(PaymentListener listener) throws InternalErrorException;

    /**
     * Method to generate the latest LNDLightningHandlerContext of latest
     * known state of the lightning node. Used to make sure the PaymentHandler
     * starts listening on the correct location of the invoice event queue after
     * restart.
     * @return the last known state of lightning handler context.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred fetching latest known state of lightning handler.
     */
    LNDLightningHandlerContext getLightningHandlerContext() throws IOException,InternalErrorException;
}
