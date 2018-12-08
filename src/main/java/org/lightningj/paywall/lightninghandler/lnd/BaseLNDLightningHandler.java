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
package org.lightningj.paywall.lightninghandler.lnd;

import io.grpc.stub.StreamObserver;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.ServerSideException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.lightningj.lnd.wrapper.message.InvoiceSubscription;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.*;
import org.lightningj.paywall.util.Base64Utils;
import org.lightningj.paywall.vo.ConvertedOrderData;
import org.lightningj.paywall.vo.InvoiceData;
import org.lightningj.paywall.vo.NodeInfo;
import org.lightningj.paywall.vo.PreImageData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.grpc.Status.Code.NOT_FOUND;
import static io.grpc.Status.Code.UNKNOWN;

/**
 * Base implementation of LND Lightning Handler handling the methods for generateInvoice, lookupInvoice and
 * invoice subscribing. See SimpleBaseLNDLightningHandler that manages APIs, opening/closing connection.
 *
 * Extends this if custom management of LND APIs should be done, otherwise use SimpleBaseLNDLightningHandler.
 *
 * @see SimpleBaseLNDLightningHandler
 * Created by Philip Vendil on 2018-11-24.
 */
public abstract class BaseLNDLightningHandler implements LightningHandler {

    protected static Logger log = Logger.getLogger(BaseLNDLightningHandler.class.getName());

    protected List<LightningEventListener> listeners = Collections.synchronizedList(new ArrayList<LightningEventListener>());

    /**
     * Method to create an invoice in the underlying LND node.
     *
     * @param preImageData the generated pre image and hash to use in invoice.
     * @param paymentData the payment data to generate invoice for.
     * @return the generated invoice data containing bolt11 invoice etc.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if problems occurred generating the invoice.
     */
    @Override
    public InvoiceData generateInvoice(PreImageData preImageData, ConvertedOrderData paymentData) throws IOException, InternalErrorException {
        checkConnection();
        try {
            Invoice lndInvoice = getLndHelper().genLNDInvoice(preImageData,paymentData);
            AddInvoiceResponse addInvoiceResponse = getSyncAPI().addInvoice(lndInvoice);
            return getLndHelper().convert(getNodeInfo(),getSyncAPI().lookupInvoice(null,addInvoiceResponse.getRHash()));
        } catch (Exception e) {
            throw new InternalErrorException("Internal error adding invoice to LND, preImageHash: " + Base64Utils.encodeBase64String(paymentData.getPreImageHash()) + ", message: " + e.getMessage(),e);
        }
    }

    /**
     * Method to lookup an invoice in LND given the invoice's pre-image hash.
     * @param preImageHash the pre image hash of the invoice to lookup.
     * @return related invoice, null if not found.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred communication or parsing invoice with LND node.
     */
    @Override
    public InvoiceData lookupInvoice(byte[] preImageHash) throws IOException, InternalErrorException{
        checkConnection();
        try {
            return getLndHelper().convert(getNodeInfo(),getSyncAPI().lookupInvoice(null,preImageHash));
        } catch (Exception e) {
            if(e instanceof ServerSideException){
                if(((ServerSideException) e).getStatus().getCode() == UNKNOWN || ((ServerSideException) e).getStatus().getCode() == NOT_FOUND){
                    return null;
                }
            }
            throw new InternalErrorException("Internal error during lookup of invoice in LND, preImageHash: " + Base64Utils.encodeBase64String(preImageHash) + ", message: " + e.getMessage(),e);
        }
    }

    /**
     * Method to register a listener to recieve notification about updated invoices and settled invoices.
     *
     * @param listener the event listener to add to list receiving notifications.
     * @throws InternalErrorException if problems occurred registering from listeners.
     */
    @Override
    public void registerListener(LightningEventListener listener) throws InternalErrorException {
        listeners.add(listener);
    }

    /**
     * Method to remove a listener from the list of event listeners of events related to adding or settling
     * invoices.
     *
     * @param listener the listener to remove from list of listeners.
     * @throws InternalErrorException if problems occurred un-registering from listeners.
     */
    @Override
    public void unregisterListener(LightningEventListener listener) throws InternalErrorException {
        listeners.remove(listener);
    }

    /**
     * Help method that setup invoice subscription with underlying LND node.
     * @param context the related LND context with latest indicies.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred with subscribing to LND node.
     */
    protected void listenToInvoices(LightningHandlerContext context) throws IOException,InternalErrorException {
        InvoiceSubscription invoiceSubscription = new InvoiceSubscription();
        if(context.getAddIndex() != null) {
            invoiceSubscription.setAddIndex(context.getAddIndex());
        }
        if(context.getSettleIndex() != null) {
            invoiceSubscription.setSettleIndex(context.getSettleIndex());
        }
        try {
            getAsyncAPI().subscribeInvoices(invoiceSubscription, new StreamObserver<Invoice>() {
                @Override
                public void onNext(Invoice invoice) {
                    LightningEventType type = invoice.getSettled() ? LightningEventType.SETTLEMENT : LightningEventType.ADDED;
                    try {
                        InvoiceData invoiceData = getLndHelper().convert(getNodeInfo(),invoice);
                        LightningEvent event = new LightningEvent(type,invoiceData);
                        for(LightningEventListener listener : listeners){
                            listener.onLightningEvent(event);
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error occurred converting LND Invoice into InvoiceData: " +e.getMessage(),e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log.log(Level.SEVERE, "Error occurred listening for settled invoices from LND: " + t.getMessage());
                    log.log(Level.FINE, "LND Error Stacktrace: ",t);
                }

                @Override
                public void onCompleted() {
                    log.info("LND Invoice subscription completed. This shouldn't happen.");
                }
            });
        } catch (Exception e) {
            throw new InternalErrorException("Internal error subscribing to LND Invoice events, message: " + e.getMessage(),e);
        }
    }

    protected void checkConnection() throws IOException, InternalErrorException{
        if(!isConnected()){
                throw new InternalErrorException("Internal error, the LightningHandler must be connected before LND API call can be made.");
        }
    }


    /**
     * Method to clear all cached objects, should be called when reconnecting to LND node.
     */
    protected void clearCache(){
        cachedLndHelper = null;
        cachedNodeInfo = null;
        cachedInfoResponse = null;
    }

    private LNDHelper cachedLndHelper = null;
    protected LNDHelper getLndHelper() throws IOException, InternalErrorException{
        if(cachedLndHelper == null) {
            checkConnection();
            cachedLndHelper = new LNDHelper(getInfoResponse());
        }
        return cachedLndHelper;
    }

    private NodeInfo cachedNodeInfo = null;
    protected NodeInfo getNodeInfo() throws IOException, InternalErrorException{
        if(cachedNodeInfo == null){
            cachedNodeInfo = getLndHelper().parseNodeInfo(getInfoResponse());
        }
        return cachedNodeInfo;
    }

    private GetInfoResponse cachedInfoResponse = null;
    private GetInfoResponse getInfoResponse() throws IOException, InternalErrorException{
        if(cachedInfoResponse == null){
            try {
                cachedInfoResponse =  getSyncAPI().getInfo();
            } catch (Exception e) {
                throw new InternalErrorException("Internal error fetching node info from LND Node: " + e.getMessage(),e);
            }
        }
        return cachedInfoResponse;
    }

    /**
     * Method to get the asynchronous lnd api from lightningj.
     * @return the asynchronous lnd api from lightningj.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred with LND node.
     */
    protected abstract AsynchronousLndAPI getAsyncAPI()  throws IOException, InternalErrorException;

    /**
     * Method to get the synchronous lnd api from lightningj.
     * @return the synchronous lnd api from lightningj.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred with LND node.
     */
    protected abstract SynchronousLndAPI getSyncAPI()  throws IOException, InternalErrorException;
}
