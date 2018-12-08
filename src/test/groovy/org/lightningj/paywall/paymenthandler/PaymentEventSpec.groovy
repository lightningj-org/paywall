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
package org.lightningj.paywall.paymenthandler

import org.lightningj.paywall.vo.InvoiceData
import org.lightningj.paywall.vo.InvoiceDataSpec
import spock.lang.Specification

import javax.json.JsonException

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for PaymentEvent
 *
 * Created by Philip Vendil on 2018-12-05.
 */
class PaymentEventSpec extends Specification {

    InvoiceData invoiceData = InvoiceDataSpec.genFullInvoiceData()

    def "Verify constructors and getter and setters"(){
        when:
        def pe1 = new PaymentEvent()
        then:
        pe1.getType() == null
        pe1.getPayment() == null
        when:
        pe1.setType(PaymentEventType.INVOICE_CREATED)
        pe1.setPayment(invoiceData)
        then:
        pe1.getType() == PaymentEventType.INVOICE_CREATED
        pe1.getPayment() == invoiceData

        when:
        def pe2 = new PaymentEvent(PaymentEventType.INVOICE_CREATED,invoiceData)
        then:
        pe2.getType() == PaymentEventType.INVOICE_CREATED
        pe2.getPayment() == invoiceData
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new PaymentEvent(PaymentEventType.INVOICE_CREATED,invoiceData).toJsonAsString(false) == """{"type":"INVOICE_CREATED","payment":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""
        when:
        new PaymentEvent(null,invoiceData).toJsonAsString(false)
        then:
        def e = thrown(JsonException)
        e.message == "Error building JSON object, required key type is null."
        when:
        new PaymentEvent(PaymentEventType.INVOICE_CREATED,null).toJsonAsString(false)
        then:
        e = thrown(JsonException)
        e.message == "Error building JSON object, required key payment is null."
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        PaymentEvent d = new PaymentEvent(toJsonObject("""{"type":"INVOICE_CREATED","payment":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""))
        then:
        d.type == PaymentEventType.INVOICE_CREATED
        d.payment != null
        when:
        new PaymentEvent(toJsonObject("""{"type":"INVALID","payment":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""))
        then:
        def e = thrown(JsonException)
        e.message == "Error parsing JSON, invalid payment event type INVALID."
        when:
        new PaymentEvent(toJsonObject("""{"payment":{"preImageHash":"MTIz","bolt11Invoice":"fksjeoskajduakdfhaskdismensuduajseusdke","description":"test desc","invoiceAmount":{"type":"CRYTOCURRENCY","value":123,"currencyCode":"BTC","magnetude":"NONE"},"nodeInfo":{"publicKeyInfo":"12312312","nodeAddress":"10.10.01.1","connectString":"12312312@10.10.01.1"},"expireDate":12345,"invoiceDate":2345,"settled":true,"settledAmount":{"type":"CRYTOCURRENCY","value":1234,"currencyCode":"BTC","magnetude":"NONE"},"settlementDate":12344}}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key type is required."
        when:
        new PaymentEvent(toJsonObject("""{"type":"INVOICE_CREATED"}"""))
        then:
        e = thrown(JsonException)
        e.message == "Error parsing JSON data, field key payment is required."
    }
}
