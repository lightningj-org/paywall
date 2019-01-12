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

import org.lightningj.lnd.wrapper.message.GetInfoResponse
import org.lightningj.lnd.wrapper.message.Invoice as LndInvoice
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.currencyconverter.CurrencyConverter
import org.lightningj.paywall.currencyconverter.SameCryptoCurrencyConverter
import org.lightningj.paywall.keymgmt.DummyKeyManager
import org.lightningj.paywall.tokengenerator.SymmetricKeyTokenGenerator
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.ConvertedOrder
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.NodeInfo
import org.lightningj.paywall.vo.Order
import org.lightningj.paywall.vo.PreImageData
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.vo.amount.CryptoAmount
import org.lightningj.paywall.vo.amount.Magnetude
import spock.lang.Specification

import javax.json.Json
import java.time.Duration
import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Unit tests for LNDHelper
 *
 * Created by Philip Vendil on 2018-11-26.
 */
class LNDHelperSpec extends Specification {

    LNDHelper helper

    def setup(){
        helper = new LNDHelper(getInfoResponse())
    }

    def "Verify that constructor parses the supported currency correctly"(){
        expect:
        helper.supportedCurrency == CryptoAmount.CURRENCY_CODE_BTC
        when: // Verify that first valid found is used.
        LNDHelper helper2 = new LNDHelper(getInfoResponse(infoResponseWithDualChain1))
        then:
        helper2.supportedCurrency == CryptoAmount.CURRENCY_CODE_BTC
        when:  // Verify that first valid found is used.
        helper2 = new LNDHelper(getInfoResponse(infoResponseWithDualChain2))
        then:
        helper2.supportedCurrency == CryptoAmount.CURRENCY_CODE_LTC
        when: // Verify that no chain found throws InternalErrorException
        new LNDHelper(getInfoResponse(infoResponseWithNoChain1))
        then:
        def e = thrown(InternalErrorException)
        e.message == "Error in LightningHandler, no supported crypto currency could be found in node info."
        when: // Verify that no chain found throws InternalErrorException
        new LNDHelper(getInfoResponse(infoResponseWithNoChain2))
        then:
        e = thrown(InternalErrorException)
        e.message == "Error in LightningHandler, no supported crypto currency could be found in node info."
        when: // Verify that no chain found throws InternalErrorException
        new LNDHelper(getInfoResponse(infoResponseWithNoValidChain))
        then:
        e = thrown(InternalErrorException)
        e.message == "Error in LightningHandler, no supported crypto currency could be found in node info."
    }

    def "Verify that convert() converts unsettled invoice correctly"(){
        setup:
        NodeInfo nodeInfo = helper.parseNodeInfo(getInfoResponse(infoResponse))
        LndInvoice lndInvoice = getLNDInvoice(unsettledInvoice)
        when:
        Invoice invoiceData = helper.convert(nodeInfo,lndInvoice)
        then:
        invoiceData.preImageHash == lndInvoice.getRHash()
        invoiceData.nodeInfo.connectString == "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
        invoiceData.description == lndInvoice.getMemo()
        invoiceData.invoiceAmount.value == lndInvoice.value
        invoiceData.bolt11Invoice == lndInvoice.paymentRequest
        invoiceData.settlementDate == null
        !invoiceData.settled
        invoiceData.settledAmount.value == 0
        invoiceData.invoiceDate.epochSecond == lndInvoice.getCreationDate()
        invoiceData.expireDate.epochSecond - invoiceData.invoiceDate.epochSecond == 3600
    }

    def "Verify that convert() converts settled invoice correctly"(){
        setup:
        NodeInfo nodeInfo = helper.parseNodeInfo(getInfoResponse(infoResponse))
        LndInvoice lndInvoice = getLNDInvoice(settledInvoice)
        when:
        Invoice invoiceData = helper.convert(nodeInfo,lndInvoice)
        then:
        invoiceData.preImageHash == lndInvoice.getRHash()
        invoiceData.nodeInfo.connectString == "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
        invoiceData.description == lndInvoice.getMemo()
        invoiceData.invoiceAmount.value == lndInvoice.value
        invoiceData.bolt11Invoice == lndInvoice.paymentRequest
        invoiceData.settlementDate.epochSecond == lndInvoice.getSettleDate()
        invoiceData.settled
        invoiceData.settledAmount.value == lndInvoice.getAmtPaidSat()
        invoiceData.settledAmount.magnetude == Magnetude.NONE
        invoiceData.invoiceDate.epochSecond == lndInvoice.getCreationDate()
        invoiceData.expireDate.epochSecond - invoiceData.invoiceDate.epochSecond == 3600
    }

    def "Verify that convert() converts settled with millisats paid correctly"(){
        setup:
        NodeInfo nodeInfo = helper.parseNodeInfo(getInfoResponse(infoResponse))
        LndInvoice lndInvoice = getLNDInvoice(settledInvoiceWithMilliSat)
        when:
        Invoice invoiceData = helper.convert(nodeInfo,lndInvoice)
        then:
        invoiceData.settledAmount.value == lndInvoice.getAmtPaidMsat()
        invoiceData.settledAmount.magnetude == Magnetude.MILLI
    }

    def "Verify that genLNDInvoice generates a correct LND invoice"(){
        setup:
        BCUtils.installBCProvider()
        DummyKeyManager dummyKeyManager = new DummyKeyManager()
        TokenGenerator tokenGenerator = new SymmetricKeyTokenGenerator(dummyKeyManager)
        PreImageData preImageData = tokenGenerator.genPreImageData()
        Order paymentData = new Order(preImageData.preImageHash, "Some Description", new BTC(10),Instant.now().plus(Duration.ofMinutes(5)))
        CurrencyConverter currencyConverter = new SameCryptoCurrencyConverter()
        ConvertedOrder convertedPaymentData = new ConvertedOrder(paymentData,currencyConverter.convert( new BTC(20)))
        when:
        LndInvoice invoice = helper.genLNDInvoice(preImageData,convertedPaymentData)
        then:
        invoice.RHash == preImageData.preImageHash
        invoice.RPreimage == preImageData.preImage
        !invoice.settled
        invoice.value == 20
        invoice.memo  == "Some Description"
        invoice.expiry == 300
    }

    def "Verify that checkCryptoAmount only accept Magnitude NONE and supported currency"(){
        when:
        helper.checkCryptoAmount(new CryptoAmount(123,CryptoAmount.CURRENCY_CODE_BTC, Magnetude.NONE))
        then:
        true
        when:
        helper.checkCryptoAmount(new CryptoAmount(123,CryptoAmount.CURRENCY_CODE_BTC, Magnetude.MILLI))
        then:
        def e = thrown InternalErrorException
        e.message == "Error in LightningHandler, Invalid crypto currency amount in payload data. Unsupported magnetude: MILLI"
        when:
        helper.checkCryptoAmount(new CryptoAmount(123,CryptoAmount.CURRENCY_CODE_LTC, Magnetude.NONE))
        then:
        e = thrown InternalErrorException
        e.message == "Error in LightningHandler, Unsupported crypto currency code in payload data: LTC"
    }

    def "Verify that parseNodeInfo returns valid NodeInfo"(){
        setup:
        helper.log = Mock(Logger)
        when:
        NodeInfo ni = helper.parseNodeInfo(getInfoResponse())
        then:
        ni.getPublicKeyInfo() == "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa"
        ni.getNodeAddress() == "82.196.97.86"
        ni.getNodePort() == 9735
        !ni.getMainNet()
        ni.getConnectString() == "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"

        when:
        ni = helper.parseNodeInfo(getInfoResponse(infoResponseDualURIs))
        then:
        ni.getConnectString() == "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
        1 * helper.log.log(Level.INFO, "LND Node Info contains 2 URIs, using the first one: 03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735")

        when:
        ni = helper.parseNodeInfo(getInfoResponse(infoResponseMainNet))
        then:
        ni.getMainNet()

        when:
        helper.parseNodeInfo(getInfoResponse(infoResponseInvalidURI))
        then:
        def e = thrown(InternalErrorException)
        e.message == "Invalid Lightning node info connect string: invalid. It should have format publicKeyInfo@nodeaddress:port, where port is optional."

    }

    private GetInfoResponse getInfoResponse(String data = infoResponse){
        return new GetInfoResponse(Json.createReader(new StringReader(data)))
    }

    private LndInvoice getLNDInvoice(String data = settledInvoice){
        return new LndInvoice(Json.createReader(new StringReader(data)))
    }

    def infoResponse = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
        "bitcoin"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def infoResponseWithDualChain1 = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
        "invalid",
        "bitcoin"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""
    def infoResponseWithDualChain2 = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
        "litecoin",
        "bitcoin"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def infoResponseWithNoChain1 = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def infoResponseWithNoChain2 = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def infoResponseWithNoValidChain = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
        "invalid"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def infoResponseDualURIs = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": true,
    "chains": [
        "bitcoin"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735",
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9736"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""
    def infoResponseMainNet = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": false,
    "chains": [
        "bitcoin"
    ],
    "uris": [
        "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa@82.196.97.86:9735"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""
    def infoResponseInvalidURI = """{
    "identity_pubkey": "03977f437e05f64b36fa973b415049e6c36c0163b0af097bab2eb3642501055efa",
    "alias": "TomSelleck",
    "num_pending_channels": 0,
    "num_active_channels": 16,
    "num_peers": 11,
    "block_height": 1445377,
    "block_hash": "0000000000000114087b92159db020478e094e1e693d63a462e3e73f45481d69",
    "synced_to_chain": true,
    "testnet": false,
    "chains": [
        "bitcoin"
    ],
    "uris": [
        "invalid"
    ],
    "best_header_timestamp": 1543258388,
    "version": "0.5.0-beta commit=3b2c807288b1b7f40d609533c1e96a510ac5fa6d"
}"""

    def unsettledInvoice = """{
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

    def settledInvoice = """{
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

    def settledInvoiceWithMilliSat = """{
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
    "amt_paid_msat": 1000200
}"""
}
