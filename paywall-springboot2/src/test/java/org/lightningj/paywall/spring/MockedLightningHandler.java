package org.lightningj.paywall.spring;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.*;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.ConvertedOrder;
import org.lightningj.paywall.vo.Invoice;
import org.lightningj.paywall.vo.NodeInfo;
import org.lightningj.paywall.vo.PreImageData;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockedLightningHandler implements LightningHandler {

    Clock clock = Clock.systemDefaultZone();
    Duration invoiceValidity = Duration.of(1, ChronoUnit.HOURS);
    List<LightningEventListener> lightningEventListeners = new ArrayList<>();
    Map<String,Invoice> invoiceMap = new HashMap<>();
    String internalErrorMessage = null;


    /**
     * Method to open up a connection to the configured LND node. Calls to register and un-register listeners
     * should be done before opening a connection to make sure the listeners receives all invoice notifications.
     *
     * @param context the last context by the payment handler, containing the indicies of the last invoices processed.
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred opening up a connection with LND node.
     */
    @Override
    public void connect(LightningHandlerContext context) throws IOException, InternalErrorException {

    }

    /**
     * Method to create an invoice in the underlying lightning node.
     *
     * @param preImageData the generated pre image and hash to use in invoice.
     * @param paymentData  the payment data to generate invoice for.
     * @return the generated invoice data containing bolt11 invoice etc.
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if problems occurred generating the invoice.
     */
    @Override
    public Invoice generateInvoice(PreImageData preImageData, ConvertedOrder paymentData) throws IOException, InternalErrorException {

        if(internalErrorMessage != null){
            String message = internalErrorMessage;
            internalErrorMessage = null;
            throw new InternalErrorException(message);

        }
        Instant invoiceDate = clock.instant();
        Invoice retval =  new Invoice(preImageData.getPreImageHash(),
                "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa",
                "Some description",
                paymentData.getConvertedAmount(),
                getNodeInfo(),
                invoiceDate.plus(invoiceValidity),invoiceDate);
        invoiceMap.put(Base58.encodeToString(preImageData.getPreImageHash()),retval);
        for(LightningEventListener l : lightningEventListeners){
            l.onLightningEvent(new LightningEvent(LightningEventType.ADDED,retval,null));
        }
        return retval;
    }

    /**
     * Method to register a listener to recieve notification about updated invoices and settled invoices.
     *
     * @param listener the event listener to add to list receiving notifications.
     * @throws InternalErrorException if problems occurred registering from listeners.
     */
    @Override
    public void registerListener(LightningEventListener listener) throws InternalErrorException {
        lightningEventListeners.add(listener);
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
        lightningEventListeners.remove(listener);
    }

    /**
     * Method to lookup an invoice in LND given the invoice's pre-image hash.
     *
     * @param preImageHash the pre image hash of the invoice to lookup.
     * @return related invoice, null if not found.
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred communication or parsing invoice with lightning node.
     */
    @Override
    public Invoice lookupInvoice(byte[] preImageHash) throws IOException, InternalErrorException {
        return invoiceMap.get(Base58.encodeToString(preImageHash));
    }

    /**
     * Method to check if handler is connected to node.
     *
     * @return true if connected,
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred checking connection status.
     */
    @Override
    public boolean isConnected() throws IOException, InternalErrorException {
        return true;
    }

    /**
     * Method to close the LND connections and release underlying resources.
     *
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    @Override
    public void close() throws IOException, InternalErrorException {

    }

    /**
     * Method to fetch the related lightning node's information to include in invoices.
     *
     * @return the related lightning node's information
     * @throws IOException            if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    @Override
    public NodeInfo getNodeInfo() throws IOException, InternalErrorException {
        return new NodeInfo("03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735");
    }

    /**
     * Test method to simulate a signal that related payment have been settled in full.
     *
     */
    public void simulateSettleInvoice(byte[] preImageHash) throws Exception{
        Invoice i = lookupInvoice(preImageHash);
        i.setSettled(true);
        i.setSettledAmount(i.getInvoiceAmount());
        i.setSettlementDate(clock.instant());
        for(LightningEventListener l : lightningEventListeners){
            l.onLightningEvent(new LightningEvent(LightningEventType.SETTLEMENT,i,null));
        }
    }

    /**
     * Method that simulates an internal exception next created invoice
     */
    public void simulateInternalError(String message){
        internalErrorMessage = message;
    }

    /**
     *
     * @return mocked invoice validity set in invoices.
     */
    public Duration getInvoiceValidity(){
        return invoiceValidity;
    }

    /**
     *
     * @param duration mocked invoice validity set in invoices.
     */
    public void setInvoiceValidity(Duration duration){
        invoiceValidity = duration;
    }


}
