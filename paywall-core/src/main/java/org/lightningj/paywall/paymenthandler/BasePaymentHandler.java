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

import org.lightningj.paywall.AlreadyExecutedException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.*;
import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext;
import org.lightningj.paywall.paymenthandler.data.PaymentData;
import org.lightningj.paywall.paymenthandler.data.PerRequestPaymentData;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.Invoice;
import org.lightningj.paywall.vo.Order;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.vo.Settlement;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lightningj.paywall.paymenthandler.PaymentEventType.*;

/**
 * Abstract Base PaymentHandler containing common logic to simplify implementation
 * of specific implementation and let them focus on persisting payment data and
 * looking up information for a given order from an order request.
 * <p>
 * Created by Philip Vendil on 2018-12-09.
 */
public abstract class BasePaymentHandler implements PaymentHandler, LightningEventListener{

    protected static Logger log = Logger.getLogger(BasePaymentListener.class.getName());

    protected PaymentEventBus paymentEventBus;
    protected PaymentDataConverter paymentDataConverter;

    /**
     * Empty Constructor
     */
    public BasePaymentHandler(){
    }

    /**
     * Method that is called during the startup the application to
     * set up all required sub components of the PaymentHandler.
     *
     * @throws InternalErrorException if internal error occurred setting
     * up the PaymentHandler.
     */
    @Override
    public void init() throws InternalErrorException{
        paymentEventBus = new PaymentEventBus();
        paymentDataConverter = new PaymentDataConverter(getLightningHandler(),
                getDefaultSettlementValidity(),
                getDefaultInvoiceValidity());
        getLightningHandler().registerListener(this);
        log.log(Level.FINE, "Initialized BasePaymentHandler.");
    }

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
    @Override
    public Order createOrder(byte[] preImageHash, OrderRequest orderRequest) throws IOException, InternalErrorException {
        PaymentData paymentData = newPaymentData(preImageHash,orderRequest);
        checkIfPayPerRequest(paymentData, orderRequest);

        Order order =  paymentDataConverter.convertToOrder(paymentData);
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Created order: " + order);
        }
        return order;
    }

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
    @Override
    public Invoice lookupInvoice(byte[] preImageHash) throws IOException, InternalErrorException {
        Invoice retval = null;
        PaymentData paymentData = findPaymentData(preImageHash);
        if(paymentData != null){
            retval = paymentDataConverter.convertToInvoice(paymentData);
        }
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Lookup of preImageHash: " + Base58.encodeToString(preImageHash) + " resulted in invoice: " + retval);
        }
        return retval;
    }

    /**
     * Method to check if a given preImageHash is settled and returns a settlement value
     * object if invoice is settled.
     * @param preImageHash the preImageHash of the invoice to check.
     * @param includeInvoice if a invoice should be included in the settlement response. This might
     *                       consume more resources.
     * @return a settlement value object of related preImageHash if invoice is settled, otherwise null.
     * @throws AlreadyExecutedException if related payment is pay per request and is already executed.
     * @throws IllegalArgumentException if related payment is per request and is already executed.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred looking up the settlement.
     */
    @Override
    public Settlement checkSettlement(byte[] preImageHash, boolean includeInvoice) throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException {
        Settlement retval = null;
        PaymentData paymentData = findPaymentData(preImageHash);
        if(paymentData != null){
            if(paymentData instanceof PerRequestPaymentData && ((PerRequestPaymentData) paymentData).isPayPerRequest()){
                if(((PerRequestPaymentData) paymentData).isExecuted()){
                    throw new AlreadyExecutedException(preImageHash,"Invalid request with preImageHash: " + Base58.encodeToString(preImageHash) + ", request have already been processed.");
                }
            }
            if(paymentDataConverter.isSettled(paymentData)){
                retval = paymentDataConverter.convertToSettlement(paymentData, includeInvoice);
            }
        }
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Check settlement of preImageHash: " + Base58.encodeToString(preImageHash) + " resulted in settlement: " + retval);
        }
        return retval;
    }

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
     * @param context the latest known state of the lightning handler.  Null if no known state exists.
     * @return a settlement value object for the given settledInvoice.
     * @throws IllegalArgumentException if settled invoices preImageHash does exists as payment data and registerNew
     * is false or related invoice isn't settled.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException  if internal exception occurred registering a settled invoice.
     */
    @Override
    public Settlement registerSettledInvoice(Invoice settledInvoice, boolean registerNew, OrderRequest orderRequest,
                                             LightningHandlerContext context) throws IllegalArgumentException,IOException,InternalErrorException{
        if(!settledInvoice.isSettled()){
            throw new IllegalArgumentException("Error trying to register settled invoice with preImageHash " + Base58.encodeToString(settledInvoice.getPreImageHash()) + ". Invoice is not settled.");
        }
        PaymentData paymentData = findPaymentData(settledInvoice.getPreImageHash());
        if(paymentData != null && paymentDataConverter.isSettled(paymentData)){
            throw new IllegalArgumentException("Error trying to register settled invoice with preImageHash " + Base58.encodeToString(settledInvoice.getPreImageHash()) + ". Payment is already settled.");
        }
        if(paymentData == null){
            if(registerNew){
                paymentData = newPaymentData(settledInvoice.getPreImageHash(), orderRequest);
                checkIfPayPerRequest(paymentData,orderRequest);
            }else{
                throw new IllegalArgumentException("Error trying to register unknown settled invoice. Invoice preImageHash: " + Base58.encodeToString(settledInvoice.getPreImageHash()));
            }
        }

        paymentDataConverter.populatePaymentDataFromInvoice(settledInvoice,paymentData);
        updatePaymentData(PaymentEventType.INVOICE_SETTLED,paymentData, context);
        Settlement settlement = paymentDataConverter.convertToSettlement(paymentData,false);
        settlement.setInvoice(settledInvoice);
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Register settled invoice of preImageHash: " + Base58.encodeToString(settledInvoice.getPreImageHash()) + " resulted in settlement: " + settlement);
        }
        return settlement;
    }

    /**
     * Method to flag a related payment flow is a pay per request type and has been processed successfully.
     * @param preImageHash the preImageHash of the payment to mark as processed.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred updating the payment or no related payment found.
     */
    public void markAsExecuted(byte[] preImageHash) throws IOException, InternalErrorException{
        PaymentData paymentData = findPaymentData(preImageHash);
        if(paymentData == null){
            throw new InternalErrorException("Internal Error marking payment with preImageHash " + Base58.encodeToString(preImageHash) + " as executed. Payment not found.");
        }
        if(paymentData instanceof PerRequestPaymentData){
            ((PerRequestPaymentData) paymentData).setExecuted(true);
            updatePaymentData(PaymentEventType.REQUEST_EXECUTED,paymentData,null);
        }else{
            throw new InternalErrorException("Internal Error marking payment with preImageHash " + Base58.encodeToString(preImageHash) + " as executed. Related PaymentData doesn't implement PerRequestPaymentData.");
        }
    }

    /**
     * Method to add the listener to the set of listeners listening
     * on payment events.
     * @param listener the listener to register.
     * @throws InternalErrorException  if internal exception occurred registering the listener.
     */
    @Override
    public void registerListener(PaymentListener listener) throws InternalErrorException {
        paymentEventBus.registerListener(listener);
    }

    /**
     * Method to remove the listener to the set of listeners listening
     * on payment events.
     * @param listener the listener to un-register.
     * @throws InternalErrorException if internal exception occurred un-registering the listener.
     */
    @Override
    public void unregisterListener(PaymentListener listener) throws InternalErrorException {
        paymentEventBus.unregisterListener(listener);
    }

    /**
     * Method to generate the latest LNDLightningHandlerContext of latest
     * known state of the lightning node. Used to make sure the PaymentHandler
     * starts listening on the correct location of the invoice event queue after
     * restart.
     *
     * The BasePaymentHandler just returns empty context for simplicity. This isn't
     * recommended for an production setup since settled invoices in lightning node might
     * be missed during restart.
     *
     * Lightning context is updated through when lightning events are triggered.
     *
     * @return the last known state of lightning handler context.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred fetching latest known state of lightning handler.
     */
    @Override
    public LNDLightningHandlerContext getLightningHandlerContext() throws IOException, InternalErrorException {
        return new LNDLightningHandlerContext();
    }

    /**
     * Method that should returned the used LightningHandler.
     * @return the related LightningHandler.
     */
    protected abstract LightningHandler getLightningHandler();

    /**
     *
     * @return the default validity for generated invoices if no expire date have
     * been set explicit in PaymentData.
     */
    protected abstract Duration getDefaultInvoiceValidity();

    /**
     *
     * @return the default validity for generated settlements if no valid until date have
     * been set explicit in PaymentData.
     */
    protected abstract Duration getDefaultSettlementValidity();

    /**
     * Method that should generate a new PaymentData for a given order request.
     * This is the first call in a payment flow and the implementation should
     * look up the order amount from the article id, units and other options in
     * the order request.
     *
     * The generated PaymentData should be at least MinimalPaymentData with preImageHash
     * and orderedAmount set.
     *
     * It is recommended that the PaymentData is persisted in this call but could
     * be skipped for performance in certain payment flows.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @param orderRequest the specification of the payment data that should be created calculated
     *                     from data in the PaymentRequired annotation.
     * @return a newly generated PaymentData signaling a new payment flow used to
     * create an Order value object.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred generating new payment data.
     */
    protected abstract PaymentData newPaymentData(byte[] preImageHash, OrderRequest orderRequest) throws IOException, InternalErrorException;

    /**
     * Method to lookup a payment data in the payment handler.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @return return related payment data or null if not found.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred fetching related payment data.
     */
    protected abstract PaymentData findPaymentData(byte[] preImageHash) throws IOException, InternalErrorException;

    /**
     * Method called on update events about a given payment data. This could be when
     * the payment is added as invoice in LND and contains complementary data or when
     * the invoice was settled and contains settled flag set and settled amount and date
     * (depending on the type of PaymentData used in PaymentHandler).
     *
     * The related payment data (using preImageHash as unique identifier) is automatically
     * looked up and the implementing method should at least persist the updated data.
     *
     * @param type the type of event such as INVOICE_CREATED or INVOICE_SETTLED.
     * @param paymentData the payment data to update and persist.
     * @param context the latest known state of the lightning handler.  Null if no known state exists.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred updating related payment data.
     */
    protected abstract void updatePaymentData(PaymentEventType type, PaymentData paymentData,
                                          LightningHandlerContext context) throws IOException, InternalErrorException;


    /**
     * This method is called every time an lightning invoice was added or settled.
     *
     * @param event the related lightning event.
     * @see LightningEvent
     */
    @Override
    public void onLightningEvent(LightningEvent event) {
        try {
            if(log.isLoggable(Level.FINE)){
                log.log(Level.FINE, "Received lightningEvent: " + event);
            }
            PaymentEventType type = event.getType() == LightningEventType.ADDED ? INVOICE_CREATED : INVOICE_SETTLED;
            byte[] preImageHash = event.getInvoice().getPreImageHash();
            PaymentData paymentData = findPaymentData(preImageHash);
            if(paymentData == null){
                log.log(Level.INFO, "Received Lightning Invoice that does not exists as payment data, invoice preImageHash: " + Base58.encodeToString(preImageHash) + ". Skipping.");
                return;
            }
            paymentDataConverter.populatePaymentDataFromInvoice(event.getInvoice(),paymentData);
            updatePaymentData(type, paymentData, event.getContext());
            Payment eventPayment = event.getInvoice();
            if(type == INVOICE_SETTLED){
                eventPayment = paymentDataConverter.convertToSettlement(paymentData,false);
                ((Settlement) eventPayment).setInvoice(event.getInvoice());
            }
            paymentEventBus.triggerEvent(type, eventPayment);
        }catch(Exception e){
            log.log(Level.SEVERE, "Error updating payment data on Lightning event of type " + event.getType() + ", invoice preimage hash: " + Base58.encodeToString(event.getInvoice().getPreImageHash()) + ", message: " + e.getMessage(),e);
        }
    }

    /**
     * Help method that verifies that if order request has payPerRequest flag, then
     * the related PaymentData implements PerRequestPaymentData otherwise throws
     * InternalErrorException.
     */
    private void checkIfPayPerRequest(PaymentData paymentData, OrderRequest orderRequest) throws InternalErrorException {
        if(orderRequest.isPayPerRequest() && !(paymentData instanceof PerRequestPaymentData)){
            throw new InternalErrorException("Internal error, order request specified payPerRequest but generated PaymentData by PaymentHandler doesn't implement PerRequestPaymentData.");
        }
    }
}
