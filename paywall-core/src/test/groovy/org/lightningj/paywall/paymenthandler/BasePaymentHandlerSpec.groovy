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

import org.lightningj.paywall.AlreadyExecutedException
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.lightninghandler.LightningEvent
import org.lightningj.paywall.lightninghandler.LightningEventType
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.lightninghandler.LightningHandlerContext
import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext
import org.lightningj.paywall.paymenthandler.data.PaymentData
import org.lightningj.paywall.util.Base58
import org.lightningj.paywall.vo.Invoice
import org.lightningj.paywall.vo.Order
import org.lightningj.paywall.vo.OrderRequest
import org.lightningj.paywall.vo.Settlement
import org.lightningj.paywall.vo.amount.BTC
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger

import static org.lightningj.paywall.paymenthandler.PaymentDataConverterSpec.*
import static org.lightningj.paywall.paymenthandler.PaymentEventBusSpec.TestPaymentEventListener
import static org.lightningj.paywall.vo.InvoiceSpec.*

/**
 * Unit test for BasePaymentHandler
 *
 * Created by Philip Vendil on 2018-12-14.
 */
class BasePaymentHandlerSpec extends Specification {

    LightningHandler lightningHandler
    TestPaymentHandler paymentHandler
    TestPaymentEventListener paymentEventListener = new TestPaymentEventListener(null, PaymentEventType.ANY_TYPE, false)
    LNDLightningHandlerContext context = new LNDLightningHandlerContext(10,20)

    def setup(){
        lightningHandler = Mock(LightningHandler)
        paymentHandler = new TestPaymentHandler(lightningHandler)
        paymentHandler.init()
        paymentHandler.paymentDataConverter.clock = Mock(Clock)
        paymentHandler.paymentDataConverter.clock.instant() >> Instant.ofEpochMilli(1544917114514L)
        BasePaymentHandler.log = Mock(Logger)
        BasePaymentHandler.log.isLoggable(Level.FINE) >> true
    }

    def "Verify init setup up handler correctly"(){
        when:
        paymentHandler.init()
        then:
        paymentHandler.paymentEventBus != null
        paymentHandler.paymentDataConverter.lightningHandler == lightningHandler
        paymentHandler.paymentDataConverter.defaultSettlementValidity.toMinutes() == 5
        paymentHandler.paymentDataConverter.defaultInvoiceValidity.toMinutes() == 60
        1 * lightningHandler.registerListener(paymentHandler)
        1 * BasePaymentHandler.log.log(Level.FINE,"Initialized BasePaymentHandler.")
    }

    def "Verify that createOrder calls newPaymentData and converts it into an order"(){
        setup:
        OrderRequest or = new OrderRequest()
        or.articleId = "article1"
        when:
        Order order = paymentHandler.createOrder("abc".bytes, or)
        then:
        order.preImageHash ==  "abc".bytes
        order.orderAmount instanceof BTC

        paymentHandler.newPaymentDataCalls.size() == 1
        paymentHandler.newPaymentDataCalls[0].preImageHash == "abc".bytes
        paymentHandler.newPaymentDataCalls[0].orderRequest == or
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Created order:"})
    }

    def """Verify that createOrder throws InternalErrorException if orderRequest has payPerRequest flag and newPaymentData
doesn't return PaymentData implementing PerRequestPaymentData"""(){
        setup:
        OrderRequest or = new OrderRequest()
        or.articleId = "article1"
        or.payPerRequest = true
        when:
        paymentHandler.createOrder("abc".bytes, or)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error, order request specified payPerRequest but generated PaymentData by PaymentHandler doesn't implement PerRequestPaymentData."
    }

    def "Verify that lookupInvoice returns calls findPaymentData and converts the PaymentData into an invoice."(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = Base58.decode("HXRC")
        when:
        Invoice result = paymentHandler.lookupInvoice(preImageHash)
        then:
        result.preImageHash == preImageHash
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        1 * lightningHandler.lookupInvoice(preImageHash) >> invoice
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Lookup of preImageHash:"})
    }

    def "Verify that lookupInvoice returns null if no related PaymentData was found."(){
        when:
        Invoice result = paymentHandler.lookupInvoice("unknown".bytes)
        then:
        result == null
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == "unknown".bytes
        0 * lightningHandler.lookupInvoice(_)
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Lookup of preImageHash:"})
    }

    def """Verify that checkSettlement returns a Settlement if related invoice was found and was settled and invoice
was returned with the includeInvoice flag."""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = "HXRCSettled".bytes
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, true)
        then:
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        1 * lightningHandler.lookupInvoice(preImageHash) >> invoice
        result.preImageHash == preImageHash
        result.invoice.bolt11Invoice == invoice.bolt11Invoice
        result.validFrom == null
        result.validUntil.toEpochMilli() == 1544917414514L
        !result.payPerRequest
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Check settlement of preImageHash:"})
    }

    def """Verify that checkSettlement returns a Settlement if related invoice was found and was settled and no invoice
was returned without the includeInvoice flag."""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = "HXRCSettled".toString()
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, false)
        then:
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        0 * lightningHandler.lookupInvoice(preImageHash)
        result.preImageHash == preImageHash
        result.invoice == null
        result.validFrom == null
        result.validUntil.toEpochMilli() == 1544917414514L
        !result.payPerRequest
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Check settlement of preImageHash:"})
    }

    def """Verify that checkSettlement returns null if related invoice was not settled."""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = Base58.decode("HXRC")
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, false)
        then:
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        0 * lightningHandler.lookupInvoice(preImageHash)
        result == null
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Check settlement of preImageHash:"})
    }

    def """Verify that checkSettlement returns null if no related invoice was found."""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = "unknown".bytes
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, false)
        then:
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        0 * lightningHandler.lookupInvoice(preImageHash)
        result == null
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Check settlement of preImageHash:"})
    }

    def """Verify that checkSettlement returns a Settlement if related invoice was found and was settled and is payPerRequest"""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = "PerReqSettled".bytes
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, false)
        then:
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
        0 * lightningHandler.lookupInvoice(preImageHash)
        result.preImageHash == preImageHash
        result.invoice == null
        result.validFrom == null
        result.validUntil.toEpochMilli() == 1544917414514L
        result.payPerRequest
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Check settlement of preImageHash:"})
    }

    def """Verify that checkSettlement throws IllegalArgumentException if related invoice is payPerRequest and already executed"""(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        byte[] preImageHash = "PerReqSettledExecuted".bytes
        invoice.preImageHash = preImageHash
        when:
        Settlement result = paymentHandler.checkSettlement(preImageHash, false)
        then:
        def e = thrown AlreadyExecutedException
        e.preImageHash == "PerReqSettledExecuted".bytes
        e.message == "Invalid request with preImageHash: 5wjafDXjRV2rvA8ox2WahTuBtLiFD, request have already been processed."
        paymentHandler.findPaymentDataCalls.size() == 1
        paymentHandler.findPaymentDataCalls[0].preImageHash == preImageHash
    }

    def "Verify that registerSettledInvoice throws IllegalArgumentException if payment is already settled in the system."(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        invoice.preImageHash = "HXRCSettled".bytes
        when:
        paymentHandler.registerSettledInvoice(invoice,false,  new OrderRequest(), context)
        then:
        def e = thrown IllegalArgumentException
        e.message == "Error trying to register settled invoice with preImageHash JwWJmahksHX4jYX. Payment is already settled."
    }

    def "Verify that registerSettledInvoice updates existing payment data if exists and not prior settled."(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        when:
        Settlement settlement = paymentHandler.registerSettledInvoice(invoice,false, new OrderRequest(),context)
        then:
        paymentHandler.updatePaymentDataCalls.size() == 1
        paymentHandler.updatePaymentDataCalls[0].type == PaymentEventType.INVOICE_SETTLED
        paymentHandler.updatePaymentDataCalls[0].paymentData.preImageHash == invoice.preImageHash
        paymentHandler.updatePaymentDataCalls[0].context == context
        settlement.preImageHash == invoice.preImageHash
        settlement.validFrom == null
        settlement.validUntil.toEpochMilli() == 1544917414514L
        settlement.invoice == invoice
        0 * lightningHandler.lookupInvoice(_)
    }

    def "Verify that registerSettledInvoice registers new payment data if not exists and registerNew flag set."(){
        setup:
        OrderRequest orderRequest = new OrderRequest()
        Invoice invoice = genFullInvoiceData(true)
        invoice.preImageHash = Base58.decode("akdamdns")
        when:
        Settlement settlement = paymentHandler.registerSettledInvoice(invoice,true,orderRequest,context)
        then:
        paymentHandler.newPaymentDataCalls.size() == 1
        paymentHandler.newPaymentDataCalls[0].preImageHash == invoice.preImageHash
        paymentHandler.newPaymentDataCalls[0].orderRequest == orderRequest
        paymentHandler.updatePaymentDataCalls.size() == 1
        paymentHandler.updatePaymentDataCalls[0].type == PaymentEventType.INVOICE_SETTLED
        paymentHandler.updatePaymentDataCalls[0].paymentData.preImageHash == invoice.preImageHash
        paymentHandler.updatePaymentDataCalls[0].context == context
        settlement.preImageHash == invoice.preImageHash
        settlement.validFrom == null
        settlement.validUntil.toEpochMilli() == 1544917414514L
        settlement.invoice == invoice
        0 * lightningHandler.lookupInvoice(_)
    }

    def """Verify that registerSettledInvoice throws InternalErrorExcpetion if new payment data doesn't implement
PerRequestPaymentData even though OrderRequest contains payPerRequest flag."""(){
        setup:
        OrderRequest orderRequest = new OrderRequest()
        orderRequest.payPerRequest = true
        Invoice invoice = genFullInvoiceData(true)
        invoice.preImageHash = Base58.decode("akdamdns")
        when:
        paymentHandler.registerSettledInvoice(invoice,true,orderRequest,context)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error, order request specified payPerRequest but generated PaymentData by PaymentHandler doesn't implement PerRequestPaymentData."
        0 * lightningHandler.lookupInvoice(_)
    }

    def "Verify that registerSettledInvoice throws IllegalArgumentException if no payment exists in system and registerNew flag is not set."(){
        setup:
        Invoice invoice = genFullInvoiceData(true)
        invoice.preImageHash = "unknown".bytes
        when:
        paymentHandler.registerSettledInvoice(invoice,false, new OrderRequest(), context)
        then:
        def e = thrown IllegalArgumentException
        e.message == "Error trying to register unknown settled invoice. Invoice preImageHash: 5T7D1EnDq7"
    }

    def "Verify that markAsExecuted updates a PerRequestPaymentData with executed flag if related payment exists"(){
        when:
        paymentHandler.markAsExecuted("PerReqSettled".bytes)
        then:
        paymentHandler.updatePaymentDataCalls.size() == 1
        paymentHandler.updatePaymentDataCalls[0].type == PaymentEventType.REQUEST_EXECUTED
        paymentHandler.updatePaymentDataCalls[0].paymentData.preImageHash == "PerReqSettled".bytes
        paymentHandler.updatePaymentDataCalls[0].paymentData.executed == true
        paymentHandler.updatePaymentDataCalls[0].context == null
    }

    def "Verify that markAsExecuted throws InternalErrorException if preImageHash couldn't be found."(){
        when:
        paymentHandler.markAsExecuted("unknown".bytes)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal Error marking payment with preImageHash 5T7D1EnDq7 as executed. Payment not found."
    }

    def "Verify that markAsExecuted throws InternalErrorException if related payment doesn't implement PerRequestPaymentData."(){
        when:
        paymentHandler.markAsExecuted("abc".bytes)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal Error marking payment with preImageHash ZiCa as executed. Related PaymentData doesn't implement PerRequestPaymentData."
    }

    def "Verify that registerListener calls register in event bus."(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        when:
        paymentHandler.registerListener(paymentEventListener)
        then:
        1 * paymentHandler.paymentEventBus.registerListener(paymentEventListener)
    }

    def "Verify that unregisterListener calls unregister in event bus."(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        when:
        paymentHandler.unregisterListener(paymentEventListener)
        then:
        1 * paymentHandler.paymentEventBus.unregisterListener(paymentEventListener)
    }


    def "Verify that getLightningHandlerContext returns an empty LNDLightningHandlerContext."(){
        when:
        LNDLightningHandlerContext ctx = paymentHandler.getLightningHandlerContext()
        then:
        ctx != null
        ctx.addIndex == null
        ctx.settleIndex == null
    }

    def "Verify that onLightningEvent calls updatePaymentData properly for PaymentEventType INVOICE_CREATED"(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        when:
        paymentHandler.onLightningEvent(new LightningEvent(LightningEventType.ADDED,genFullInvoiceData(false), context))
        then:
        paymentHandler.updatePaymentDataCalls.size() == 1
        paymentHandler.updatePaymentDataCalls[0].type == PaymentEventType.INVOICE_CREATED
        paymentHandler.updatePaymentDataCalls[0].paymentData != null
        paymentHandler.updatePaymentDataCalls[0].context == context
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Received lightningEvent:"})
        1 * paymentHandler.paymentEventBus.triggerEvent(PaymentEventType.INVOICE_CREATED, _ as Invoice)
    }

    def "Verify that onLightningEvent calls updatePaymentData properly for PaymentEventType INVOICE_SETTLED"(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        when:
        paymentHandler.onLightningEvent(new LightningEvent(LightningEventType.SETTLEMENT,genFullInvoiceData(true), context))
        then:
        paymentHandler.updatePaymentDataCalls.size() == 1
        paymentHandler.updatePaymentDataCalls[0].type == PaymentEventType.INVOICE_SETTLED
        paymentHandler.updatePaymentDataCalls[0].paymentData != null
        paymentHandler.updatePaymentDataCalls[0].context == context
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Received lightningEvent:"})
        1 * paymentHandler.paymentEventBus.triggerEvent(PaymentEventType.INVOICE_SETTLED, _ as Settlement) >> { def type, Settlement settlement ->
            assert settlement.preImageHash == Base58.decode("HXRC")
            assert settlement.validUntil.toEpochMilli() == 1544917414514
            assert settlement.invoice.bolt11Invoice == "fksjeoskajduakdfhaskdismensuduajseusdke"

        }
    }

    def "Verify that onLightningEvent logs info about skipping an invoice it hasn't created before hand."(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        Invoice invoice = genFullInvoiceData(false)
        invoice.preImageHash = "unknown".bytes
        when:
        paymentHandler.onLightningEvent(new LightningEvent(LightningEventType.SETTLEMENT,invoice, context))
        then:
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Received lightningEvent:"})
        1 * BasePaymentHandler.log.log(Level.INFO, """Received Lightning Invoice that does not exists as payment data, invoice preImageHash: 5T7D1EnDq7. Skipping.""")

    }

    def "Verify that onLightningEvent logs error if any exception occurs during processing"(){
        setup:
        paymentHandler.paymentEventBus = Mock(PaymentEventBus)
        paymentHandler.paymentEventBus.triggerEvent(_,_) >> { throw new InternalErrorException("Some Error")}
        when:
        paymentHandler.onLightningEvent(new LightningEvent(LightningEventType.SETTLEMENT,genFullInvoiceData(false), context))
        then:
        1 * BasePaymentHandler.log.log(Level.FINE, {it =~ "Received lightningEvent:"})
        1 * BasePaymentHandler.log.log(Level.SEVERE, "Error updating payment data on Lightning event of type SETTLEMENT, invoice preimage hash: HXRC, message: Some Error",_ as InternalErrorException)
    }

    static class TestPaymentHandler extends BasePaymentHandler{

        TestPaymentHandler(LightningHandler lightningHandler){
            this.lightningHandler = lightningHandler
        }
        List newPaymentDataCalls = []
        List findPaymentDataCalls = []
        List updatePaymentDataCalls = []
        LightningHandler lightningHandler

        @Override
        protected LightningHandler getLightningHandler() {
            return this.lightningHandler
        }

        @Override
        protected Duration getDefaultInvoiceValidity() {
            return Duration.ofMinutes(60)
        }

        @Override
        protected Duration getDefaultSettlementValidity() {
            return Duration.ofMinutes(5)
        }

        @Override
        protected PaymentData newPaymentData(byte[] preImageHash, OrderRequest orderRequest) throws IOException, InternalErrorException {
            newPaymentDataCalls << [preImageHash: preImageHash, orderRequest: orderRequest]
            return new TestMinimalData(preImageHash: preImageHash, orderAmount: new BTC(1000))
        }

        @Override
        protected PaymentData findPaymentData(byte[] preImageHash) throws IOException, InternalErrorException {
            findPaymentDataCalls << [preImageHash: preImageHash]
            if(preImageHash == "abc".bytes || preImageHash == Base58.decode("HXRC")) {
                return new TestMinimalData(preImageHash: preImageHash, orderAmount: new BTC(1000))
            }
            if(preImageHash == "HXRCSettled".bytes) {
                return new TestMinimalData(preImageHash: preImageHash, orderAmount: new BTC(1000), settled: true)
            }
            if(preImageHash == "PerReqSettled".bytes) {
                return new TestMinimalPayPerRequestData(preImageHash: preImageHash, orderAmount: new BTC(1000), settled: true, payPerRequest: true, executed: false)
            }
            if(preImageHash =="PerReqSettledExecuted".bytes) {
                return new TestMinimalPayPerRequestData(preImageHash: preImageHash, orderAmount: new BTC(1000), settled: true, payPerRequest: true, executed: true)
            }
            return null
        }

        @Override
        protected void updatePaymentData(PaymentEventType type, PaymentData paymentData, LightningHandlerContext context) throws IOException, InternalErrorException {
            updatePaymentDataCalls << [type: type, paymentData: paymentData, context: context]
        }
    }

}
