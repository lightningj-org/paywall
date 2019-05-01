/*
 * ***********************************************************************
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
package org.lightningj.paywall.spring.response

import org.lightningj.paywall.paymentflow.InvoiceResult
import org.lightningj.paywall.requestpolicy.RequestPolicyType
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.amount.BTC
import spock.lang.Specification

import java.time.Instant

/**
 * Unit tests for InvoiceResponse
 *
 * @author Philip 2019-04-20
 */
class InvoiceResponseSpec extends Specification {

    InvoiceResult invoiceResult

    def setup() {
        Invoice invoice = new Invoice("123".getBytes(), "fksjeoskajduakdfhaskdismensuduajseusdke+=", "test desc", new BTC(123), new org.lightningj.paywall.vo.NodeInfo("12312312@10.10.01.1"), Instant.ofEpochMilli(12345L), Instant.ofEpochMilli(2345L))
        invoiceResult = new InvoiceResult(invoice, "SomeToken+=")
    }

    def "Verify constructors and getter and setters"() {
        when:
        InvoiceResponse r = new InvoiceResponse(invoiceResult, true, RequestPolicyType.WITH_BODY, true, "settlementlink", "qrlink")
        then:
        r.preImageHash == "123".getBytes()
        r.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke+="
        r.description == "test desc"
        r.invoiceAmount.magnetude == Magnetude.NONE
        r.invoiceAmount.currencyCode == org.lightningj.paywall.vo.amount.CryptoAmount.CURRENCY_CODE_BTC
        r.invoiceAmount.value == 123
        r.nodeInfo.connectString == "12312312@10.10.01.1"
        r.token == "SomeToken+="
        r.invoiceDate.time == 2345L
        r.invoiceExpireDate.time == 12345L
        r.payPerRequest
        r.requestPolicyType == RequestPolicyType.WITH_BODY.name()
        r.checkSettlementLink == "settlementlink?pwir=SomeToken%2B%3D"
        r.qrLink == "qrlink?d=fksjeoskajduakdfhaskdismensuduajseusdke%2B%3D"
        r.type == InvoiceResponse.TYPE

        when:
        r = new InvoiceResponse(invoiceResult, false, RequestPolicyType.WITH_BODY, false, "settlementlink", "qrlink")
        then:
        r.preImageHash == "123".getBytes()
        r.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke+="
        r.description == "test desc"
        r.invoiceAmount.magnetude == Magnetude.NONE
        r.invoiceAmount.currencyCode == org.lightningj.paywall.vo.amount.CryptoAmount.CURRENCY_CODE_BTC
        r.invoiceAmount.value == 123
        r.nodeInfo == null
        r.token == "SomeToken+="
        r.invoiceDate.time == 2345L
        r.invoiceExpireDate.time == 12345L
        !r.payPerRequest
        r.requestPolicyType == RequestPolicyType.WITH_BODY.name()
        r.checkSettlementLink == "settlementlink?pwir=SomeToken%2B%3D"
        r.qrLink == "qrlink?d=fksjeoskajduakdfhaskdismensuduajseusdke%2B%3D"
        r.type == InvoiceResponse.TYPE

        when:
        r = new InvoiceResponse()
        then:
        r.preImageHash == null
        r.bolt11Invoice == null
        r.description == null
        r.invoiceAmount == null
        r.nodeInfo == null
        r.token == null
        r.invoiceDate == null
        r.invoiceExpireDate == null
        r.payPerRequest == null
        r.requestPolicyType == null
        r.checkSettlementLink == null
        r.qrLink == null
        r.type == InvoiceResponse.TYPE

        when:
        r.preImageHash = "123".getBytes()
        r.bolt11Invoice = "fksjeoskajduakdfhaskdismensuduajseusdke"
        r.description = "test desc"
        r.invoiceAmount = new CryptoAmount()
        r.nodeInfo = new NodeInfo()
        r.token = "SomeToken"
        r.invoiceDate = new Date(2345L)
        r.invoiceExpireDate = new Date(12345L)
        r.payPerRequest = true
        r.requestPolicyType = RequestPolicyType.WITH_BODY.name()
        r.checkSettlementLink = "settlementlink"
        r.qrLink = "qrlink"

        then:
        r.preImageHash == "123".getBytes()
        r.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"
        r.description == "test desc"
        r.invoiceAmount != null
        r.nodeInfo != null
        r.token == "SomeToken"
        r.invoiceDate.time == 2345L
        r.invoiceExpireDate.time == 12345L
        r.payPerRequest
        r.requestPolicyType == RequestPolicyType.WITH_BODY.name()
        r.checkSettlementLink == "settlementlink"
        r.qrLink == "qrlink"
        r.type == InvoiceResponse.TYPE
    }

    def "Verify toString"() {
        expect:
        new InvoiceResponse(invoiceResult, true, RequestPolicyType.WITH_BODY, true, "settlementlink", "qrlink").toString() == "InvoiceResponse{, preImageHash='MTIz', bolt11Invoice='fksjeoskajduakdfhaskdismensuduajseusdke+=', description='test desc', invoiceAmount=CryptoAmount{value='123', currencyCode='BTC', magnetude=NONE}, nodeInfo=NodeInfo{publicKeyInfo='12312312', nodeAddress='10.10.01.1', nodePort=null, mainNet=true, connectString='12312312@10.10.01.1'}, token='SomeToken+=', invoiceDate=Thu Jan 01 01:00:02 CET 1970, invoiceExpireDate=Thu Jan 01 01:00:12 CET 1970, payPerRequest=true, requestPolicyType=WITH_BODY, checkSettlementLink='settlementlink?pwir=SomeToken%2B%3D', qrLink='qrlink?d=fksjeoskajduakdfhaskdismensuduajseusdke%2B%3D'}"
    }

}
