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
package org.lightningj.paywall.lightninghandler

import org.lightningj.lnd.wrapper.message.GetInfoResponse
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.keymgmt.DummyKeyManager
import org.lightningj.paywall.lightninghandler.lnd.LNDHelper
import org.lightningj.paywall.lightninghandler.lnd.SimpleBaseLNDLightningHandler
import org.lightningj.paywall.tokengenerator.SymmetricKeyTokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.ConvertedPaymentData
import org.lightningj.paywall.vo.InvoiceData
import org.lightningj.paywall.vo.PaymentData
import org.lightningj.paywall.vo.PreImageData
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.vo.amount.CryptoAmount
import org.lightningj.paywall.vo.amount.Magnetude
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.Instant

/**
 * Unit tests for BaseLNDLightningHandler.
 *
 * Created by Philip Vendil on 2018-11-25.
 */
@Stepwise
class DefaultLNDLightningHandlerIntegrationSpec extends Specification {

    @Shared SymmetricKeyTokenGenerator tokenGenerator
    @Shared TestDefaultLNDLightningHandler handler
    @Shared GetInfoResponse infoResponse
    @Shared LNDHelper helper
    @Shared TestEventListener listener
    @Shared PreImageData preImageData

    @Shared String lndHost
    @Shared String lndPort
    @Shared String tlsCertPath
    @Shared String macaroonPath

    def setupSpec(){
        BCUtils.installBCProvider()
        lndHost = System.getProperty("paywall.integration.test.lnd.host")
        lndPort = System.getProperty("paywall.integration.test.lnd.port")
        tlsCertPath = System.getProperty("paywall.integration.test.lnd.tlscertpath")
        macaroonPath = System.getProperty("paywall.integration.test.lnd.macaroonpath")
        DummyKeyManager dummyKeyManager = new DummyKeyManager()
        tokenGenerator = new SymmetricKeyTokenGenerator(dummyKeyManager)
        handler = new TestDefaultLNDLightningHandler(lndHost,Integer.parseInt(lndPort),
                tlsCertPath,
                macaroonPath)
        listener = new TestEventListener()
    }

    def "Register listener and verify it is added"(){
        when:
        handler.registerListener(listener)
        then:
        handler.listeners.contains(listener)
    }

    def "Verify connect() is working and init helper initializes properly"(){
        when:
        handler.connect(new LightningHandlerContext())
        infoResponse = handler.getSyncAPI().getInfo()
        helper = new LNDHelper(infoResponse)
        then:
        handler.isConnected()

        handler.asynchronousLndAPI != null
        handler.synchronousLndAPI != null

        helper.supportedCurrency == CryptoAmount.CURRENCY_CODE_BTC
    }

    def "Test to generateInvoice and verify the returned invoice data"(){
        setup:
        preImageData = tokenGenerator.genPreImageData()
        PaymentData paymentData = new PaymentData(preImageData.preImageHash,"Some Memo",new BTC(10),Instant.now().plusSeconds(1800))
        ConvertedPaymentData convertedPaymentData = new ConvertedPaymentData(paymentData,paymentData.requestedAmount)
        when:
        InvoiceData invoiceData = handler.generateInvoice(preImageData,convertedPaymentData)
        then:
        invoiceData.preImageHash == preImageData.preImageHash
        invoiceData.bolt11Invoice != null
        invoiceData.description == "Some Memo"
        invoiceData.invoiceAmount.value == 10
        invoiceData.invoiceAmount.currencyCode == CryptoAmount.CURRENCY_CODE_BTC
        invoiceData.invoiceAmount.magnetude == Magnetude.NONE
        invoiceData.nodeInfo != null
        long expireSec  =invoiceData.expireDate.epochSecond - invoiceData.invoiceDate.epochSecond
        1750 < expireSec
        expireSec < 1850
        !invoiceData.settled
        invoiceData.settledAmount != null
    }

    def "Verify lookupInvoice returns correct invoice"(){
        when:
        InvoiceData invoiceData = handler.lookupInvoice(preImageData.preImageHash)
        then:
        invoiceData.preImageHash == preImageData.preImageHash
    }

    def "Verify that lookup invoice for nonexisting invoice returns null"(){
        when:
        InvoiceData invoiceData = handler.lookupInvoice(new byte[32])
        then:
        invoiceData == null
    }

    def "Verify that event have been received"(){
        setup:
        Thread.sleep(3000)
        when:
        LightningEvent event = listener.events.find{LightningEvent it -> it.invoice.preImageHash == preImageData.preImageHash }
        then:
        event != null
        InvoiceData invoiceData = event.invoice
        event.type == LightningEventType.ADDED
        event.invoice.description == "Some Memo" // rest should be the
        invoiceData.preImageHash == preImageData.preImageHash
        invoiceData.bolt11Invoice != null
        invoiceData.description == "Some Memo"
        invoiceData.invoiceAmount.value == 10
        invoiceData.invoiceAmount.currencyCode == CryptoAmount.CURRENCY_CODE_BTC
        invoiceData.invoiceAmount.magnetude == Magnetude.NONE
        invoiceData.nodeInfo != null
        long expireSec  =invoiceData.expireDate.epochSecond - invoiceData.invoiceDate.epochSecond
        1750 < expireSec
        expireSec < 1850
        !invoiceData.settled
        invoiceData.settledAmount != null
    }

    def "Verify that close() closes the connection and free up resources"(){
        when:
        handler.close()
        then:
        !handler.isConnected()
        handler.asynchronousLndAPI == null
        handler.synchronousLndAPI == null
    }

    def "Verify that unregister removes the listener from the list"(){
        when:
        handler.unregisterListener(listener)
        then:
        !handler.listeners.contains(listener)
    }

    static class TestDefaultLNDLightningHandler extends SimpleBaseLNDLightningHandler{

        private String host
        private int port
        private String tlsCertPath
        private String macaroonPath

        TestDefaultLNDLightningHandler(String host, int port, String tlsCertPath, String macaroonPath){
            this.host = host
            this.port = port
            this.tlsCertPath = tlsCertPath
            this.macaroonPath = macaroonPath
        }

        @Override
        protected String getHost() throws InternalErrorException {
            return host
        }

        @Override
        protected int getPort() throws InternalErrorException {
            return port
        }

        @Override
        protected String getTLSCertPath() throws InternalErrorException {
            return tlsCertPath
        }

        @Override
        protected String getMacaroonPath() throws InternalErrorException {
            return macaroonPath
        }


    }

    static class TestEventListener implements LightningEventListener{
        List events = []

        @Override
        void onLightningEvent(LightningEvent event) {
            events << event
        }
    }
}
