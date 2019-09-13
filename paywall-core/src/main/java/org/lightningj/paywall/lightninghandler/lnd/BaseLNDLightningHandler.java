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
import org.lightningj.lnd.wrapper.message.InvoiceSubscription;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.*;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.ConvertedOrder;
import org.lightningj.paywall.vo.Invoice;
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

    LightningInvoiceListenerRunnable lightningInvoiceListenerRunnable;
    Thread lightningInvoiceListenerThread;

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
    public Invoice generateInvoice(PreImageData preImageData, ConvertedOrder paymentData) throws IOException, InternalErrorException {
        checkConnection();
        try {
            org.lightningj.lnd.wrapper.message.Invoice lndInvoice = getLndHelper().genLNDInvoice(preImageData,paymentData);
            AddInvoiceResponse addInvoiceResponse = getSyncAPI().addInvoice(lndInvoice);
            Invoice invoice = getLndHelper().convert(getNodeInfo(),getSyncAPI().lookupInvoice(null,addInvoiceResponse.getRHash()));
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Generated Invoice in LND: " + invoice);
            }
            return invoice;
        } catch (Exception e) {
            throw new InternalErrorException("Internal error adding invoice to LND, preImageHash: " + Base58.encodeToString(paymentData.getPreImageHash()) + ", message: " + e.getMessage(),e);
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
    public Invoice lookupInvoice(byte[] preImageHash) throws IOException, InternalErrorException{
        checkConnection();
        try {
            Invoice invoice = getLndHelper().convert(getNodeInfo(),getSyncAPI().lookupInvoice(null,preImageHash));
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Lookup Invoice in LND: " + invoice);
            }
            return invoice;
        } catch (Exception e) {
            if(e instanceof ServerSideException){
                if(((ServerSideException) e).getStatus().getCode() == UNKNOWN || ((ServerSideException) e).getStatus().getCode() == NOT_FOUND){
                    return null;
                }
            }
            throw new InternalErrorException("Internal error during lookup of invoice in LND, preImageHash: " + Base58.encodeToString(preImageHash) + ", message: " + e.getMessage(),e);
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
        if(!(context instanceof LNDLightningHandlerContext)){
            throw new InternalErrorException("Error initializing LightningHandler invoice subscription, LightningHandlerContext must be of type LNDLightningHandlerContext.");
        }

        LNDLightningHandlerContext ctx = (LNDLightningHandlerContext) context;
        lightningInvoiceListenerRunnable = new LightningInvoiceListenerRunnable(ctx);
        lightningInvoiceListenerThread = new Thread(lightningInvoiceListenerRunnable);
        lightningInvoiceListenerThread.start();
    }

    protected void checkConnection() throws IOException, InternalErrorException{
        if(!isConnected()){
                throw new InternalErrorException("Internal error, the LightningHandler must be connected before LND API call can be made.");
        }
    }

    /**
     * Method to close the LND connections and release underlying resources.
     *
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    @Override
    public void close() throws IOException, InternalErrorException {
        if(lightningInvoiceListenerRunnable != null) {
            lightningInvoiceListenerRunnable.stopListening();
            lightningInvoiceListenerThread.interrupt();
            int waitCounter = 0;
            while (!lightningInvoiceListenerRunnable.isStopped()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.log(Level.FINE, "LightningInvoiceListenerThread stop waiting interrupted: " + e.getMessage(), e);
                }
                waitCounter++;
                if (waitCounter % 10 == 0) {
                    log.log(Level.INFO, "Waiting for LightningInvoiceListenerThread to stop");
                }
            }
            log.log(Level.FINE, "LightningInvoiceListenerThread stopped.");
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

    private NodeInfo cachedNodeInfo = null;
    /**
     * Method to fetch the related lightning node's information to include in invoices.
     * @return the related lightning node's information
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    @Override
    public NodeInfo getNodeInfo() throws IOException, InternalErrorException{
        if(cachedNodeInfo == null){
            cachedNodeInfo = getNodeInfoFromConfiguration();
            if(cachedNodeInfo == null) {
                cachedNodeInfo = getLndHelper().parseNodeInfo(getInfoResponse());
            }
        }
        return cachedNodeInfo;
    }

    private LNDHelper cachedLndHelper = null;
    protected LNDHelper getLndHelper() throws IOException, InternalErrorException{
        if(cachedLndHelper == null) {
            checkConnection();
            cachedLndHelper = new LNDHelper(getSupportedCurrencyCode());
        }
        return cachedLndHelper;
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

    /**
     * Method to retrieve node information from configuration or null if not configured.
     * <p>
     *     Used when the LND macaroon doesn't have access rights to retrieve LND Node information.
     * </p>
     * @return populated NodeInfo from configuration or nulll if no configuration exists.
     * @throws InternalErrorException if problems occurred parsing the configuration.
     */
    protected abstract NodeInfo getNodeInfoFromConfiguration() throws InternalErrorException;

    /**
     * Method to retrieve configured supported currency code. Should be one of CryptoAmount CURRENCY_CODE_ constants.
     * @return The used currency code
     * @throws InternalErrorException if currency code configuration was unparsable.
     */
    protected abstract String getSupportedCurrencyCode() throws InternalErrorException;

    /**
     * Method to reconnect API connections with a node, should be called after a restart of LND Node.
     * @throws InternalErrorException if internal problems occurred opening up a connection with LND node.
     */
    protected abstract void reconnect() throws InternalErrorException;

    /**
     * Runnable that maintains the asynchronous invoice event stream with the LND Node and
     * restarts it after waiting a short while.
     */
    protected class LightningInvoiceListenerRunnable implements Runnable{

        static final int RECONNECT_WAITTIME_IN_SEC = 1;

        LNDLightningHandlerContext lastKnownContext;
        boolean isRunning;
        boolean stopped = false;
        boolean listening = false;
        boolean connectionOpen = true;

        /**
         * Default constructor.
         *
         * @param lastKnownContext last known LNDLightningHandlerContext from where
         *                         to start listening for invoices.
         */
        LightningInvoiceListenerRunnable(LNDLightningHandlerContext lastKnownContext){
            this.lastKnownContext = lastKnownContext;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            isRunning = true;
            while(isRunning){
                if(!connectionOpen){
                    try {
                        reconnect();
                        connectionOpen = true;
                    }catch (Exception e){
                        log.log(Level.SEVERE, "Internal error reconnecting to LND APIs, message: " + e.getMessage() + " will try to reconnect in " + RECONNECT_WAITTIME_IN_SEC + " seconds.", e);
                    }
                }
                if(!listening){
                    InvoiceSubscription invoiceSubscription = new InvoiceSubscription();
                    if(lastKnownContext.getAddIndex() != null) {
                        invoiceSubscription.setAddIndex(lastKnownContext.getAddIndex());
                    }
                    if(lastKnownContext.getSettleIndex() != null) {
                        invoiceSubscription.setSettleIndex(lastKnownContext.getSettleIndex());
                    }
                    try {

                        // Maybe a loop that for each second check if listening, if not is restarted.
                        getAsyncAPI().subscribeInvoices(invoiceSubscription, new StreamObserver<org.lightningj.lnd.wrapper.message.Invoice>() {
                            @Override
                            public void onNext(org.lightningj.lnd.wrapper.message.Invoice invoice) {
                                LightningEventType type = invoice.getSettled() ? LightningEventType.SETTLEMENT : LightningEventType.ADDED;
                                try {
                                    Invoice invoiceData = getLndHelper().convert(getNodeInfo(),invoice);
                                    lastKnownContext = genCurrentContext(lastKnownContext,invoice);
                                    // Create a copy that is sent to listeners, without possibility to affect current state.
                                    LNDLightningHandlerContext context = genCurrentContext(lastKnownContext,invoice);
                                    if(log.isLoggable(Level.FINE)) {
                                        log.log(Level.FINE, "Received invoice event from LND, invoice: " + invoice + "\ncontext: " + context);
                                    }
                                    LightningEvent event = new LightningEvent(type,invoiceData,context);
                                    for(LightningEventListener listener : listeners){
                                        listener.onLightningEvent(event);
                                    }
                                } catch (Exception e) {
                                    log.log(Level.SEVERE, "Error occurred converting LND Invoice into Invoice: " +e.getMessage(),e);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                log.log(Level.SEVERE, "Error occurred listening for settled invoices from LND: " + t.getMessage());
                                log.log(Level.FINE, "LND Error Stacktrace: ",t);
                                connectionOpen = false;
                                listening = false;
                            }

                            @Override
                            public void onCompleted() {
                                log.info("LND Invoice subscription completed. This shouldn't happen.");
                                connectionOpen = false;
                                listening = false;
                            }
                        });
                        if(log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Subscribed to invoices in LND, context: " + lastKnownContext);
                        }
                        listening = true;

                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Internal error subscribing to LND Invoice events, message: " + e.getMessage() + " will try to reconnect in " + RECONNECT_WAITTIME_IN_SEC + " seconds.", e);
                    }

                }
                try {
                    Thread.sleep(RECONNECT_WAITTIME_IN_SEC * 1000);
                } catch (InterruptedException e) {
                    log.fine("LightningInvoiceListener listener interrupted.");
                }

                log.fine("LightningInvoiceListenerThread listening in background.");
            }

            stopped = true;
        }

        public void stopListening(){
            isRunning = false;
        }

        public boolean isStopped(){
            return stopped;
        }
    }


    /**
     * Help method to generate a new last known context which set to the values of the new context
     * object if they are not set to 0, in that case is the last known index used.
     * @param lastKnownContext last known LNDLightningHandlerContext
     * @param newContext the new LNDLightningHandlerContext
     * @return a new instance LNDLightningHandlerContext.
     */
    protected static LNDLightningHandlerContext genCurrentContext(LNDLightningHandlerContext lastKnownContext, org.lightningj.lnd.wrapper.message.Invoice newContext){
        if(lastKnownContext == null){
            return new LNDLightningHandlerContext(newContext.getAddIndex(),newContext.getSettleIndex());
        }
        return new LNDLightningHandlerContext(
                newContext.getAddIndex() == 0 && lastKnownContext.getAddIndex() != null ? lastKnownContext.getAddIndex() : newContext.getAddIndex(),
                newContext.getSettleIndex() == 0 && lastKnownContext.getSettleIndex() != null ? lastKnownContext.getSettleIndex() : newContext.getSettleIndex());

    }
}
