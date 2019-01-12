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

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.paymenthandler.data.FullPaymentData
import org.lightningj.paywall.paymenthandler.data.MinimalPaymentData
import org.lightningj.paywall.paymenthandler.data.PaymentData
import org.lightningj.paywall.paymenthandler.data.PerRequestPaymentData
import org.lightningj.paywall.paymenthandler.data.StandardPaymentData
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.InvoiceSpec
import org.lightningj.paywall.vo.NodeInfo
import org.lightningj.paywall.vo.Order
import org.lightningj.paywall.vo.Settlement
import org.lightningj.paywall.vo.amount.Amount
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.vo.amount.CryptoAmount
import org.lightningj.paywall.vo.amount.FiatAmount
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for PaymentDataConverter.
 *
 * Created by Philip Vendil on 2018-12-11.
 */
class PaymentDataConverterSpec extends Specification {

    PaymentDataConverter converter
    LightningHandler lightningHandler = Mock(LightningHandler)

    Invoice settledInvoice = InvoiceSpec.genFullInvoiceData(true)

    def setup(){
        converter = new PaymentDataConverter(lightningHandler, Duration.ofMinutes(60), Duration.ofMinutes(120))
        converter.clock = Mock(Clock)
        converter.clock.instant() >> Instant.ofEpochMilli(5000)
    }


    def "Verify that convertToOrder converts a MinimalPaymentData correctly."(){
        setup:
        def pd = new TestMinimalData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        Order order = converter.convertToOrder(pd)
        then:
        order.orderAmount == pd.orderAmount
        order.preImageHash == pd.preImageHash
        order.expireDate.toEpochMilli() == 7205000
        order.description == null
    }

    def "Verify that convertToOrder converts a StandardPaymentData correctly if expire data is NOT set."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123),
        invoiceExpireDate: null, description: "Some Desc")
        when:
        Order order = converter.convertToOrder(pd)
        then:
        order.orderAmount == pd.orderAmount
        order.preImageHash == pd.preImageHash
        order.expireDate.toEpochMilli() == 7205000
        order.description == "Some Desc"
    }

    def "Verify that convertToOrder converts a StandardPaymentData correctly if expire data is set."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123),
                invoiceExpireDate: Instant.ofEpochMilli(10000), description: "Some Desc")
        when:
        Order order = converter.convertToOrder(pd)
        then:
        order.orderAmount == pd.orderAmount
        order.preImageHash == pd.preImageHash
        order.expireDate.toEpochMilli() == 10000
        order.description == "Some Desc"
    }

    def "Verify that convertToOrder throws InternalErrorException if payment data isn't implementing MinimalPaymentData"(){
        when:
        converter.convertToOrder(new TestPaymentData())
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData."
    }

    def "Verify that convertToInvoice converts FullPaymentData into Invoice without looking up the invoice in LightningHandler."(){
        setup:
        def pd = new TestFullData(settled: true, preImageHash: "abc".bytes, invoiceAmount: new BTC(123),
        bolt11Invoice: "somebolt", description: "Some Description", invoiceExpireDate:  Instant.ofEpochMilli(3000),
        invoiceDate: Instant.ofEpochMilli(1000), settlementDate: Instant.ofEpochMilli(5000), settledAmount: new BTC(122))
        when:
        Invoice invoice = converter.convertToInvoice(pd)
        then:
        0 * lightningHandler.lookupInvoice(_)
        1 * lightningHandler.getNodeInfo() >> { return new NodeInfo("abcdef@10.10.10.1:1444")}
        invoice.preImageHash == pd.preImageHash
        invoice.invoiceDate == pd.invoiceDate
        invoice.expireDate == pd.invoiceExpireDate
        invoice.description == pd.description
        invoice.bolt11Invoice == pd.bolt11Invoice
        invoice.settled == pd.settled
        invoice.settlementDate == pd.settlementDate
        invoice.settledAmount == pd.settledAmount
        invoice.nodeInfo.connectString == "abcdef@10.10.10.1:1444"
    }

    def "Verify that convertToInvoice converts FullPaymentData into Invoice for unsettled invoice."(){
        setup:
        def pd = new TestFullData(settled: false, preImageHash: "abc".bytes, invoiceAmount: new BTC(123),
                bolt11Invoice: "somebolt", description: "Some Description", invoiceExpireDate:  Instant.ofEpochMilli(3000),
                invoiceDate: Instant.ofEpochMilli(1000))
        when:
        Invoice invoice = converter.convertToInvoice(pd)
        then:
        0 * lightningHandler.lookupInvoice(_)
        1 * lightningHandler.getNodeInfo() >> { return new NodeInfo("abcdef@10.10.10.1:1444")}
        invoice.preImageHash == pd.preImageHash
        invoice.invoiceDate == pd.invoiceDate
        invoice.expireDate == pd.invoiceExpireDate
        invoice.description == pd.description
        invoice.bolt11Invoice == pd.bolt11Invoice
        invoice.settled == pd.settled
        invoice.settlementDate == null
        invoice.settledAmount == null
        invoice.nodeInfo.connectString == "abcdef@10.10.10.1:1444"
    }

    def "Verify that convertToInvoice fetches the invoice from LightningHandler if payment data is of type StandardPaymentData."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        Invoice invoice = converter.convertToInvoice(pd)
        then:
        1 * lightningHandler.lookupInvoice("abc".bytes) >> {return settledInvoice}
        invoice == settledInvoice
    }

    def "Verify that convertToInvoice throws InternalErrorException if lightningHandler cannot lookup the related invoice if payment data is of type StandardPaymentData."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        converter.convertToInvoice(pd)
        then:
        1 * lightningHandler.lookupInvoice("abc".bytes) >> {return null}
        def e = thrown(InternalErrorException)
        e.message == "Internal error converting payment data into invoice, invoice with preImageHash YWJj not found by LightningHandler."
    }

    def "Verify that convertToInvoice throws InternalErrorException if payment data isn't implementing MinimalPaymentData"(){
        when:
        converter.convertToInvoice(new TestPaymentData())
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData."
    }

    def "Verify that convertToSettlement converts MinimalPaymentData correctly"(){
        setup:
        def pd = new TestMinimalData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        Settlement settlement = converter.convertToSettlement(pd,false)
        then:
        settlement.preImageHash == "abc".bytes
        settlement.invoice == null
        settlement.validUntil.toEpochMilli() == 3605000
        settlement.validFrom == null
        !settlement.payPerRequest
    }

    def "Verify that convertToSettlement includes invoice if includeInvoice is set."(){
        setup:
        def pd = new TestMinimalData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        Settlement settlement = converter.convertToSettlement(pd,true)
        then:
        1 * lightningHandler.lookupInvoice("abc".bytes) >> {return settledInvoice}
        settlement.preImageHash == "abc".bytes
        settlement.invoice == settledInvoice
        settlement.validUntil.toEpochMilli() == 3605000
        settlement.validFrom == null
    }

    def "Verify that convertToSettlement converts StandardPaymentData with default validity duration."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123))
        when:
        Settlement settlement = converter.convertToSettlement(pd,false)
        then:
        settlement.preImageHash == "abc".bytes
        settlement.invoice == null
        settlement.validUntil.toEpochMilli() == 3605000
        settlement.validFrom == null
        !settlement.payPerRequest
    }

    def "Verify that convertToSettlement converts StandardPaymentData with payment data validity if set."(){
        setup:
        def pd = new TestStandardData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123), settlementExpireDate: Instant.ofEpochMilli(7000))
        when:
        Settlement settlement = converter.convertToSettlement(pd,false)
        then:
        settlement.preImageHash == "abc".bytes
        settlement.invoice == null
        settlement.validUntil.toEpochMilli() == 7000
        settlement.validFrom == null
        !settlement.payPerRequest
    }

    def "Verify that convertToSettlement converts FullPaymentData with payment data valid from set in settlement."(){
        setup:
        def pd = new TestFullData(settled: true, preImageHash: "abc".bytes, orderAmount: new BTC(123),
                settlementExpireDate: Instant.ofEpochMilli(7000), settlementValidFrom: Instant.ofEpochMilli(1000), payPerRequest: true)
        when:
        Settlement settlement = converter.convertToSettlement(pd,false)
        then:
        settlement.preImageHash == "abc".bytes
        settlement.invoice == null
        settlement.validUntil.toEpochMilli() == 7000
        settlement.validFrom.toEpochMilli() == 1000
        settlement.payPerRequest
    }



    def "Verify that convertToSettlement throws InternalErrorException if payment data isn't implementing MinimalPaymentData"(){
        when:
        converter.convertToSettlement(new TestPaymentData(),false)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData."
    }

    def "Verify populatePaymentDataFromInvoice sets all expected fields for a FullPaymentData"(){
        setup:
        FullPaymentData pd = new TestFullData()
        when:
        converter.populatePaymentDataFromInvoice(settledInvoice, pd)
        then:
        pd.isSettled() == settledInvoice.settled
        pd.preImageHash == null
        pd.orderAmount == null
        pd.description == settledInvoice.description
        pd.invoiceAmount == settledInvoice.invoiceAmount
        pd.invoiceDate == settledInvoice.invoiceDate
        pd.invoiceExpireDate == settledInvoice.expireDate
        pd.settlementDate == settledInvoice.settlementDate
        pd.settledAmount == settledInvoice.settledAmount
        pd.settlementExpireDate == null
        pd.bolt11Invoice == settledInvoice.bolt11Invoice
    }

    def "Verify populatePaymentDataFromInvoice sets all expected fields for a StandardPaymentData"(){
        setup:
        StandardPaymentData pd = new TestStandardData()
        when:
        converter.populatePaymentDataFromInvoice(settledInvoice, pd)
        then:
        pd.isSettled() == settledInvoice.settled
        pd.preImageHash == null
        pd.orderAmount == null
        pd.description == settledInvoice.description
        pd.invoiceAmount == settledInvoice.invoiceAmount
        pd.invoiceDate == settledInvoice.invoiceDate
        pd.invoiceExpireDate == settledInvoice.expireDate
        pd.settlementDate == settledInvoice.settlementDate
        pd.settledAmount == settledInvoice.settledAmount
        pd.settlementExpireDate == null
    }

    def "Verify populatePaymentDataFromInvoice sets all expected fields for a MinimalPaymentData"(){
        setup:
        MinimalPaymentData pd = new TestMinimalData()
        when:
        converter.populatePaymentDataFromInvoice(settledInvoice, pd)
        then:
        pd.isSettled() == settledInvoice.settled
        pd.preImageHash == null
        pd.orderAmount == null
    }

    def "Verify that populatePaymentDataFromInvoice throws InternalErrorException if payment data isn't implementing MinimalPaymentData"(){
        when:
        converter.populatePaymentDataFromInvoice(settledInvoice, new TestPaymentData())
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData."
    }

    @Unroll
    def "Verify that isSettled returns #expected if paymentData.isSettled is #expected"(){
        expect:
        converter.isSettled(new TestMinimalData(settled: expected)) == expected
        where:
        expected << [true,false]
    }

    def "Verify that isSettled throws InternalErrorException if paymentData doesn't implement MinimalPaymentData"(){
        when:
        converter.isSettled(new TestPaymentData())
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData."
    }

    def "Verify that basicCheck throws InternalErrorException if paymentData is null."(){
        when:
        converter.basicCheck(null)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error converting PaymentData, PaymentData cannot be null."
    }

    static class TestPaymentData implements PaymentData{}

    static class TestMinimalData implements MinimalPaymentData{
        byte[] preImageHash
        boolean settled
        Amount orderAmount
    }

    static class TestMinimalPayPerRequestData implements MinimalPaymentData, PerRequestPaymentData{
        byte[] preImageHash
        boolean settled
        Amount orderAmount
        boolean payPerRequest
        boolean executed
    }

    static class TestStandardData extends TestMinimalData implements StandardPaymentData{
        String description
        CryptoAmount invoiceAmount
        Instant invoiceDate
        Instant invoiceExpireDate
        CryptoAmount settledAmount
        Instant settlementDate
        Instant settlementExpireDate
    }

    static class TestFullData extends TestStandardData implements FullPaymentData{
        Instant settlementValidFrom
        String bolt11Invoice
        boolean payPerRequest
        boolean executed
    }
}
