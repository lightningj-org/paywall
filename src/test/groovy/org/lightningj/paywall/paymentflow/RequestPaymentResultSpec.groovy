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
package org.lightningj.paywall.paymentflow

import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.InvoiceSpec
import spock.lang.Specification

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for RequestPaymentResult.
 *
 * Created by Philip Vendil on 2018-12-29.
 */
class RequestPaymentResultSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def rpr1 = new RequestPaymentResult()
        then:
        rpr1.getInvoice() == null
        rpr1.getToken() == null

        when:
        rpr1.setInvoice(InvoiceSpec.genFullInvoiceData(true))
        rpr1.setToken("sometokendata")

        then:
        rpr1.getInvoice() instanceof Invoice
        rpr1.getToken() == "sometokendata"

        when:
        def rpr2 = new RequestPaymentResult(InvoiceSpec.genFullInvoiceData(true), "sometokendata")
        then:
        rpr2.getInvoice() instanceof Invoice
        rpr2.getToken() == "sometokendata"
    }


    def "Verify that toJsonAsString works as expected"(){
        expect:
        new RequestPaymentResult(InvoiceSpec.genFullInvoiceData(true), "sometokendata").toJsonAsString(false) == """{"invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"token":"sometokendata"}"""
        new RequestPaymentResult(null,null).toJsonAsString(false) == "{}"
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        RequestPaymentResult d = new RequestPaymentResult(toJsonObject("""{"invoice":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344},"token":"sometokendata"}"""))
        then:
        d.getInvoice() instanceof Invoice
        d.getToken() == "sometokendata"

        when:
        RequestPaymentResult d2 = new RequestPaymentResult(toJsonObject("{}"))
        then:
        d2.getInvoice() == null
        d2.getToken() == null
    }
}
