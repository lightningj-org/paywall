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

import org.lightningj.paywall.vo.OrderData
import spock.lang.Specification
import spock.lang.Unroll

import static org.lightningj.paywall.paymenthandler.PaymentEventType.*

/**
 * Unit tests for PaymentEventBus.
 *
 * Created by Philip Vendil on 2018-12-06.
 */
class PaymentEventBusSpec extends Specification {

    def listener_for_preHash1_OrderCreated_unregister = new TestPaymentEventListener("abc".bytes,PaymentEventType.ORDER_CREATED,true)
    def listener_for_preHash1_OrderCreated = new TestPaymentEventListener("abc".bytes,PaymentEventType.ORDER_CREATED,false)
    def listener_for_preHash2_OrderCreated = new TestPaymentEventListener("def".bytes,PaymentEventType.ORDER_CREATED,false)
    def listener_for_preHash1_InvoiceCreated_unregister = new TestPaymentEventListener("abc".bytes,PaymentEventType.INVOICE_CREATED,true)
    def listener_for_preHash1_InvoiceSettled = new TestPaymentEventListener("abc".bytes,PaymentEventType.INVOICE_SETTLED,false)

    PaymentEventBus eventBus = new PaymentEventBus()

    def "Verify that registerListener() adds listener to list and unregisterListener() removes it."(){
        expect:
        eventBus.listeners.size() == 0
        when:
        eventBus.registerListener(listener_for_preHash1_InvoiceCreated_unregister)
        eventBus.registerListener(listener_for_preHash1_InvoiceSettled)
        then:
        eventBus.listeners.size() == 2
        eventBus.listeners.contains(listener_for_preHash1_InvoiceCreated_unregister)
        eventBus.listeners.contains(listener_for_preHash1_InvoiceSettled)

        when:
        eventBus.unregisterListener(listener_for_preHash1_InvoiceSettled)
        then:
        eventBus.listeners.size() == 1
        eventBus.listeners.contains(listener_for_preHash1_InvoiceCreated_unregister)
        !eventBus.listeners.contains(listener_for_preHash1_InvoiceSettled)
    }

    def "Verify that listeners are notified for matching events and that listeners that are marked for unregistration is removed."(){
        setup:
        Payment payment = new OrderData()
        payment.preImageHash = "abc".bytes

        eventBus.registerListener(listener_for_preHash1_OrderCreated_unregister)
        eventBus.registerListener(listener_for_preHash2_OrderCreated)
        eventBus.registerListener(listener_for_preHash1_OrderCreated)
        expect:
        eventBus.listeners.size() == 3
        when:
        eventBus.triggerEvent(ORDER_CREATED, payment)
        then:
        listener_for_preHash1_OrderCreated_unregister.events.size() == 1
        listener_for_preHash1_OrderCreated_unregister.events[0].type == ORDER_CREATED
        listener_for_preHash1_OrderCreated_unregister.events[0].payment == payment
        listener_for_preHash1_OrderCreated.events.size() == 1
        listener_for_preHash1_OrderCreated.events[0].type == ORDER_CREATED
        listener_for_preHash1_OrderCreated.events[0].payment == payment
        listener_for_preHash2_OrderCreated.events.size() == 0
        eventBus.listeners.size() == 2
        eventBus.listeners.contains(listener_for_preHash2_OrderCreated)
        eventBus.listeners.contains(listener_for_preHash1_OrderCreated)
        when:
        eventBus.triggerEvent(ORDER_CREATED, payment)
        then:
        listener_for_preHash1_OrderCreated.events.size() == 2
        listener_for_preHash1_OrderCreated.events[1].type == ORDER_CREATED
        listener_for_preHash1_OrderCreated.events[1].payment == payment
        listener_for_preHash2_OrderCreated.events.size() == 0
        when:
        payment.preImageHash = "def".bytes
        eventBus.triggerEvent(ORDER_CREATED, payment)
        then:
        listener_for_preHash1_OrderCreated.events.size() == 2
        listener_for_preHash2_OrderCreated.events.size() == 1
        listener_for_preHash2_OrderCreated.events[0].type == ORDER_CREATED
        listener_for_preHash2_OrderCreated.events[0].payment == payment
    }
    
    @Unroll
    def "Verify that matches returns #expected if #description"(){
        setup:
        Payment payment = new OrderData()
        payment.preImageHash = eventPreImageHash
        expect:
        eventBus.matches(new TestPaymentEventListener(listenPreImageHash,listenType,false), new PaymentEvent(eventType, payment)) == expected
        where:
        listenPreImageHash | listenType      | eventPreImageHash | eventType     | expected | description
        "abc".bytes        | ORDER_CREATED   | "abc".bytes       | ORDER_CREATED | true     | "both preImageHash and type is the same in listener and event."
        null               | ORDER_CREATED   | "abc".bytes       | ORDER_CREATED | true     | "listener matches any preImageHash and specific event type."
        "abc".bytes        | ANY_TYPE        | "abc".bytes       | ORDER_CREATED | true     | "listener matches any event type and specific preImageHash."
        "abc".bytes        | ORDER_CREATED   | "def".bytes       | ORDER_CREATED | false    | "listener matches specific preImageHash which doesn't equal but event type does."
        "abc".bytes        | INVOICE_CREATED | "abc".bytes       | ORDER_CREATED | false    | "listener matches specific event type which doesn't equal but preImageHash does."
        "abc".bytes        | INVOICE_CREATED | "abc".bytes       | ORDER_CREATED | false    | "listener matches specific event type which doesn't equal but preImageHash does."
        null               | INVOICE_CREATED | "abc".bytes       | ORDER_CREATED | false    | "listener matches any preImageHash but event type doesn't equal."
        "abc".bytes        | ANY_TYPE        | "def".bytes       | ORDER_CREATED | false    | "listener matches any event type but preImageHash doesn't equal."
        null               | ANY_TYPE        | "def".bytes       | ORDER_CREATED | true     | "listener matches any preImageHash and any event type."
    }

    static class TestPaymentEventListener extends BasePaymentListener{

        List<PaymentEvent> events = []

        TestPaymentEventListener(byte[] preImageHash, PaymentEventType type, boolean unregisterAfterEvent) {
            super(preImageHash, type, unregisterAfterEvent)
        }

        @Override
        void onPaymentEvent(PaymentEvent event) {
            events << event
        }
    }
}
