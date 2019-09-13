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
package org.lightningj.paywall.lightninghandler.lnd

import io.grpc.stub.StreamObserver
import org.lightningj.lnd.wrapper.AsynchronousLndAPI
import org.lightningj.lnd.wrapper.SynchronousLndAPI
import org.lightningj.lnd.wrapper.message.InvoiceSubscription
import org.lightningj.lnd.wrapper.message.Invoice as LndInvoice
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.lightninghandler.LightningEvent
import org.lightningj.paywall.lightninghandler.LightningEventListener
import org.lightningj.paywall.lightninghandler.LightningEventType
import org.lightningj.paywall.lightninghandler.LightningHandlerContext
import org.lightningj.paywall.paymenthandler.BasePaymentHandler
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.NodeInfo
import org.lightningj.paywall.vo.amount.CryptoAmount
import spock.lang.Specification
import spock.lang.Unroll

import javax.json.Json
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Unit tests for BaseLNDLightningHandler
 *
 * Created by Philip Vendil on 2018-12-03.
 */
class BaseLNDLightningHandlerSpec extends Specification {

    // Most of the BaseLNDLightningHandler functionality
    // is done in the integration test. This test suite
    // only test parts that couldn't be tested during
    // integration tests.
    TestDefaultLNDLightningHandler handler

    def setup(){
        handler = new TestDefaultLNDLightningHandler()
        handler.asynchronousLndAPI = Mock(AsynchronousLndAPI)
        handler.synchronousLndAPI = Mock(SynchronousLndAPI)
        handler.lndHelper = Mock(LNDHelper)
        BaseLNDLightningHandler.log = Mock(Logger)
        BaseLNDLightningHandler.log.isLoggable(Level.FINE) >> true
    }

    def "check that listenToInvoices throws InternalErrorException if LightningHandlerContext is not of type LNDLightningHandlerContext"(){
        when:
        handler.listenToInvoices(Mock(LightningHandlerContext))
        then:
        def e = thrown InternalErrorException
        e.message == "Error initializing LightningHandler invoice subscription, LightningHandlerContext must be of type LNDLightningHandlerContext."

    }
    def "check that listenToInvoices detects added and settled invoices correctly and verify that close method stops the background thread."(){
        setup:
        def ctx = new LNDLightningHandlerContext(20,10)
        def eventListener = new TestLightningEventListener()
        StreamObserver<Invoice> observer = null
        handler.configuredNodeInfo = new NodeInfo("abc@10.10.10.11:9001")
        // Add listener
        handler.registerListener(eventListener)

        LndInvoice unsettled = toInvoice(unsettledInvoice)
        LndInvoice settled = toInvoice(settledInvoice)
        when:
        handler.listenToInvoices(ctx)
        while(!handler.lightningInvoiceListenerRunnable.listening){
            Thread.sleep(50)
        }
        then:
        1 * handler.asynchronousLndAPI.subscribeInvoices(_,_) >> {
            InvoiceSubscription invoiceSubscription, StreamObserver<Invoice> o ->
                assert invoiceSubscription.addIndex == 20
                assert invoiceSubscription.settleIndex == 10
                assert o != null
                observer = o
        }
        1 * BaseLNDLightningHandler.log.log(Level.FINE,{ it =~ "Subscribed to invoices in LND, context:"} )
        handler.lightningInvoiceListenerRunnable.isRunning
        when:
        observer.onNext(unsettled)
        then:
        1 * handler.lndHelper.convert(handler.nodeInfo,unsettled) >> new Invoice()
        eventListener.eventList.size() == 1
        eventListener.eventList[0].type == LightningEventType.ADDED
        eventListener.eventList[0].invoice != null
        1 * BaseLNDLightningHandler.log.log(Level.FINE,{ it =~ "Received invoice event from LND, invoice:"} )
        when:
        observer.onNext(settled)
        then:
        1 * handler.lndHelper.convert(handler.nodeInfo,settled) >> new Invoice()
        eventListener.eventList.size() == 2
        eventListener.eventList[1].type == LightningEventType.SETTLEMENT
        eventListener.eventList[1].invoice != null

        when:
        observer.onError(new IOException("Testmessage"))
        then:
        1 * BaseLNDLightningHandler.log.log(Level.SEVERE, "Error occurred listening for settled invoices from LND: Testmessage")
        1 * BaseLNDLightningHandler.log.log(Level.FINE, "LND Error Stacktrace: ", !null)
        !handler.lightningInvoiceListenerRunnable.listening
        !handler.lightningInvoiceListenerRunnable.connectionOpen

        when: // Verify that it reconnects again after sleeping for a period of time.
        handler.lightningInvoiceListenerThread.interrupt()
        while(!handler.lightningInvoiceListenerRunnable.listening){
            Thread.sleep(50)
        }
        then:
        1 * BaseLNDLightningHandler.log.log(Level.FINE,{ it =~ "Subscribed to invoices in LND, context:"} )
        handler.lightningInvoiceListenerRunnable.isRunning
        handler.lightningInvoiceListenerRunnable.connectionOpen
        handler.lightningInvoiceListenerRunnable.listening

        when:
        observer.onCompleted()
        then:
        1 * BaseLNDLightningHandler.log.info( "LND Invoice subscription completed. This shouldn't happen.")

        when: // Verify that it reconnects again after sleeping for a period of time.
        handler.lightningInvoiceListenerThread.interrupt()
        while(!handler.lightningInvoiceListenerRunnable.listening){
            Thread.sleep(50)
        }
        then:
        1 * BaseLNDLightningHandler.log.log(Level.FINE,{ it =~ "Subscribed to invoices in LND, context:"} )
        handler.lightningInvoiceListenerRunnable.isRunning
        handler.lightningInvoiceListenerRunnable.connectionOpen
        handler.lightningInvoiceListenerRunnable.listening

        when:
        handler.close()
        then:
        handler.lightningInvoiceListenerRunnable.isStopped()
        1 * BaseLNDLightningHandler.log.log(Level.FINE,"LightningInvoiceListenerThread stopped." )

    }

    def "Verify that getNodeInfo returns configured node info if configuration exists"(){
        setup:
        handler.configuredNodeInfo = new NodeInfo("abcdef@10.10.10.12:9002")
        expect:
        handler.getNodeInfo().connectString == "abcdef@10.10.10.12:9002"
    }

    @Unroll
    def "Verify that genCurrentContext generates expected LNDLightningHandlerContext"(){
        setup:
        def invoice = new org.lightningj.lnd.wrapper.message.Invoice()
        invoice.addIndex = invoiceAddIndex
        invoice.settleIndex = invoiceSettleIndex
        def lastKnownCtx = new LNDLightningHandlerContext(lastKnownAddIndex, lastKnownSettleIndex)
        when:
        def newCtx = handler.genCurrentContext(lastKnownCtx,invoice)
        then:
        newCtx.addIndex == expectedAddIndex
        newCtx.settleIndex == expectedSettleIndex
        where:
        invoiceAddIndex | invoiceSettleIndex | lastKnownAddIndex | lastKnownSettleIndex | expectedAddIndex | expectedSettleIndex
        0               | 12                 | 1                 | 0                    | 1                | 12
        11              | 0                  | 12                | 5                    | 11               | 5

    }

    def "Verify that genCurrentContext with lastKnownContext set to null returns values from invoice"(){
        setup:
        def invoice = new org.lightningj.lnd.wrapper.message.Invoice()
        invoice.addIndex = 2
        invoice.settleIndex = 1
        when:
        def newCtx = handler.genCurrentContext(null,invoice)
        then:
        newCtx.addIndex == 2
        newCtx.settleIndex == 1
        when:
        newCtx = handler.genCurrentContext(new LNDLightningHandlerContext(null,null),invoice)
        then:
        newCtx.addIndex == 2
        newCtx.settleIndex == 1
    }

    static LndInvoice toInvoice(String invoiceData){
        def reader = Json.createReader(new StringReader(invoiceData))

        LndInvoice i = new LndInvoice(reader)
        return i
    }

    static class TestLightningEventListener implements LightningEventListener{

        List<LightningEvent> eventList = []
        /**
         * This method every time an lightning invoice was added or settled.
         * @param event the related lightning event.
         * @see LightningEvent
         */
        @Override
        void onLightningEvent(LightningEvent event) {
            eventList << event
        }
    }

    static class TestDefaultLNDLightningHandler extends BaseLNDLightningHandler{

        AsynchronousLndAPI asynchronousLndAPI
        SynchronousLndAPI synchronousLndAPI
        LNDLightningHandlerContext context
        LNDHelper lndHelper
        NodeInfo configuredNodeInfo

        TestDefaultLNDLightningHandler(){
        }


        @Override
        void connect(LightningHandlerContext context) throws IOException, InternalErrorException {
            this.context = context
        }

        @Override
        boolean isConnected() throws IOException, InternalErrorException {
            return false
        }


        @Override
        protected AsynchronousLndAPI getAsyncAPI() throws IOException, InternalErrorException {
            return asynchronousLndAPI
        }
        @Override
        protected SynchronousLndAPI getSyncAPI() throws IOException, InternalErrorException {
            return synchronousLndAPI
        }

        @Override
        protected NodeInfo getNodeInfoFromConfiguration() throws InternalErrorException {
            return configuredNodeInfo
        }

        @Override
        protected String getSupportedCurrencyCode() throws InternalErrorException {
            return CryptoAmount.CURRENCY_CODE_BTC
        }

        /**
         * Method to reconnect API connections with a node, should be called after a restart of LND Node.
         * @throws InternalErrorException if internal problems occurred opening up a connection with LND node.
         */
        @Override
        protected void reconnect() throws InternalErrorException {

        }

        @Override
        protected LNDHelper getLndHelper(){
            return lndHelper
        }

    }

    static def unsettledInvoice = """{
    "memo": "",
    "receipt": "",
    "r_preimage": "X2M1ep3aqXQhThnlvNzdOYUcYQ5D03Ghl6xmgLiEI9c=",
    "r_hash": "ej6+/ROOkAF6Ml1la8KwV/3FYTCZqv5eZpNclUjhadc=",
    "value": 70000,
    "settled": false,
    "creation_date": 1537362101,
    "settle_date": 0,
    "payment_request": "lntb700u1pd6yj94pp50gltalgn36gqz73jt4jkhs4s2l7u2cfsnx40uhnxjdwf2j8pd8tsdqqcqzysrz25ejcmfpj2jnff4txez4jstlt9fp42z0ms573yz2sa8gzc707hutatf8n53h6zauvuta6tvwyk7qu4l88hx8z9d2psg9czymc57mqqmuds8w",
    "description_hash": "",
    "expiry": 3600,
    "fallback_addr": "",
    "cltv_expiry": 144,
    "route_hints": [
    ],
    "private": false,
    "add_index": 24,
    "settle_index": 0,
    "amt_paid": 0,
    "amt_paid_sat": 0,
    "amt_paid_msat": 0
}"""

    static def settledInvoice = """{
    "memo": "",
    "receipt": "",
    "r_preimage": "04Ryp26Gji635V20d4VMy/s/3AeIvv+0l+t3ps9EUjs=",
    "r_hash": "SfuE+p9uMihxwVCijsjOSVXdTfsa8MiQTu3gxDLWhfY=",
    "value": 1000,
    "settled": true,
    "creation_date": 1543180018,
    "settle_date": 1543180034,
    "payment_request": "lntb10u1pdlkrhjpp5f8acf75ldcezsuwp2z3gajxwf92a6n0mrtcv3yzwahsvgvkkshmqdqqcqzysrzjqt4w86ax0qeehv39jvq869p7e4vqa4hnc8dt8pghjtnt9xdjvlkzw9syl5qqqdsqqqqqqqlgqqqqqqgqjqrzjq0c8ywxvx4kz6u8nr8gff36vprk63n349xc5pk7ttj9fpc3585fl5900kvqqqpqqqqqqqqlgqqqqqqgqjqvf7mhse0ae23lxdnqamred2me7tlv3hs5mx9cs7zvsuptl08pnxy568a08uw5vklylupnsf5yelkxy7aj4e3sh3j76twn75dasdpzvspudzg7q",
    "description_hash": "",
    "expiry": 3600,
    "fallback_addr": "",
    "cltv_expiry": 144,
    "route_hints": [
        {
            "hop_hints": [
                {
                    "node_id": "02eae3eba678339bb22593007d143ecd580ed6f3c1dab3851792e6b299b267ec27",
                    "chan_id": 1586671145186623488,
                    "fee_base_msat": 1000,
                    "fee_proportional_millionths": 1,
                    "cltv_expiry_delta": 144
                }
            ]
        },
        {
            "hop_hints": [
                {
                    "node_id": "03f07238cc356c2d70f319d094c74c08eda8ce3529b140dbcb5c8a90e2343d13fa",
                    "chan_id": 1580678806811967488,
                    "fee_base_msat": 1000,
                    "fee_proportional_millionths": 1,
                    "cltv_expiry_delta": 144
                }
            ]
        }
    ],
    "private": false,
    "add_index": 29,
    "settle_index": 11,
    "amt_paid": 1000000,
    "amt_paid_sat": 1000,
    "amt_paid_msat": 1000000
}"""
}
