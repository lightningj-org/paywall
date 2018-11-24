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
package org.lightningj.paywall.btcpayserver

import org.lightningj.paywall.btcpayserver.vo.Invoice
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager
import org.lightningj.paywall.keymgmt.TestDefaultFileKeyManager
import org.lightningj.paywall.util.BCUtils
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Unit tests for BTCPayServerFacade
 *
 * Created by Philip Vendil on 2018-10-14.
 */
@Ignore
class BTCPayServerSpec extends Specification {

    @Shared TestBTCPayServer server

    def setupSpec(){
        BCUtils.installBCProvider()

        System.setProperty("javax.net.ssl.trustStore","/Users/philip/paywallkeys/letencrypttrust.jks")
        System.setProperty("javax.net.ssl.trustStorePassword","foobar123")

        server = new TestBTCPayServer()
    }

//    def "First test"(){
//
//        when:
//        server.keyManager.getPublicKey(BTCPayServerKeyContext.INSTANCE)
//
//        then:
//        true
//
//    }

    def "Create Invoice"(){
        setup:
        Invoice invoice = new Invoice()
        invoice.setPrice(0.00001)
        invoice.setCurrency("BTC")
        invoice.setPosData("JSONTOKENDATA")
        invoice.token = "By88EtE2sBRjbEfxsRNHpzyctx9EhYG9qDYgsmSiqMmv"

        when:
           // println server.fetchToken(BTCPayServerFacade.MERCHANT)
            Invoice result = server.registerInvoice(invoice)

        println result

        then:
        true

//        when:
//            Invoice result2 = server.fetchInvoice("4yyZTXvVfxdvfKe86VGdtq")
//        println result2
//
//        then:
//        true
    }

     class TestBTCPayServer extends BTCPayServer{



        @Override
        protected String getBTCPayServerClientName() {
            return "Paywall Test Client"
        }

        @Override
        protected String getPairingCode() {
            return null;// "oAZb5F3"
        }

        @Override
        protected String getBaseURL() {
            return "https://btcpay302112.lndyn.com/"
        }

        TestDefaultFileKeyManager keyManager = null
        @Override
        protected AsymmetricKeyManager getKeyManager() {
            if(keyManager == null){
                keyManager =  new TestDefaultFileKeyManager("/Users/philip/paywallkeys","/Users/philip/paywallkeys/trust","foobar123")
            }
            return keyManager
        }
    }
}
