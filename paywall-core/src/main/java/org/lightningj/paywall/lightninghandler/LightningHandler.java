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
package org.lightningj.paywall.lightninghandler;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext;
import org.lightningj.paywall.vo.ConvertedOrder;
import org.lightningj.paywall.vo.Invoice;
import org.lightningj.paywall.vo.NodeInfo;
import org.lightningj.paywall.vo.PreImageData;

import java.io.IOException;

/**
 * Interface for used lightning implementation creating and listening for settled invoices.
 *
 * Created by Philip Vendil on 2018-11-24.
 */
public interface LightningHandler {


    /**
     * Method to open up a connection to the configured LND node. Calls to register and un-register listeners
     * should be done before opening a connection to make sure the listeners receives all invoice notifications.
     * @param context the last context by the payment handler, containing the indicies of the last invoices processed.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred opening up a connection with LND node.
     */
    void connect(LightningHandlerContext context) throws IOException, InternalErrorException;

    /**
     * Method to create an invoice in the underlying lightning node.
     *
     * @param preImageData the generated pre image and hash to use in invoice.
     * @param paymentData the payment data to generate invoice for.
     * @return the generated invoice data containing bolt11 invoice etc.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if problems occurred generating the invoice.
     */
    Invoice generateInvoice(PreImageData preImageData, ConvertedOrder paymentData) throws IOException, InternalErrorException;

    /**
     * Method to register a listener to recieve notification about updated invoices and settled invoices.
     *
     * @param listener the event listener to add to list receiving notifications.
     * @throws InternalErrorException if problems occurred registering from listeners.
     */
    void registerListener(LightningEventListener listener) throws InternalErrorException;

    /**
     * Method to remove a listener from the list of event listeners of events related to adding or settling
     * invoices.
     *
     * @param listener the listener to remove from list of listeners.
     * @throws InternalErrorException if problems occurred un-registering from listeners.
     */
    void unregisterListener(LightningEventListener listener) throws InternalErrorException;

    /**
     * Method to lookup an invoice in LND given the invoice's pre-image hash.
     * @param preImageHash the pre image hash of the invoice to lookup.
     * @return related invoice, null if not found.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred communication or parsing invoice with lightning node.
     */
    Invoice lookupInvoice(byte[] preImageHash) throws IOException, InternalErrorException;

    /**
     * Method to check if handler is connected to node.
     *
     * @return true if connected,
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred checking connection status.
     */
    boolean isConnected() throws IOException, InternalErrorException;

    /**
     * Method to close the LND connections and release underlying resources.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    void close() throws IOException, InternalErrorException;

    /**
     * Method to fetch the related lightning node's information to include in invoices.
     * @return the related lightning node's information
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    NodeInfo getNodeInfo() throws IOException, InternalErrorException;
}
